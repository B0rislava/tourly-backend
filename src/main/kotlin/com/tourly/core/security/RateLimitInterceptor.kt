package com.tourly.core.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.tourly.core.exception.ErrorCode
import com.tourly.core.exception.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class RateLimitInterceptor(
    private val rateLimitingService: RateLimitingService
) : HandlerInterceptor {

    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val ip = getClientIP(request)
        val bucket = rateLimitingService.resolveBucket(ip)

        val probe = bucket.tryConsumeAndReturnRemaining(1)
        if (probe.isConsumed) {
            response.addHeader("X-Rate-Limit-Remaining", probe.remainingTokens.toString())
            return true
        }

        val retryAfterSeconds = probe.nanosToWaitForRefill / 1_000_000_000
        val errorCode = ErrorCode.RATE_LIMIT_EXCEEDED
        val errorResponse = ErrorResponse(
            code = errorCode.code,
            message = errorCode.message,
            description = "You have exceeded the request limit. Please try again in $retryAfterSeconds second(s)."
        )

        response.status = HttpStatus.TOO_MANY_REQUESTS.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.addHeader("X-Rate-Limit-Retry-After-Seconds", retryAfterSeconds.toString())
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
        return false
    }

    private fun getClientIP(request: HttpServletRequest): String {
        val xfHeader = request.getHeader("X-Forwarded-For")
        if (xfHeader.isNullOrEmpty() || "unknown".equals(xfHeader, ignoreCase = true)) {
            return request.remoteAddr ?: "unknown"
        }
        return xfHeader.split(",").firstOrNull() ?: request.remoteAddr ?: "unknown"
    }
}
