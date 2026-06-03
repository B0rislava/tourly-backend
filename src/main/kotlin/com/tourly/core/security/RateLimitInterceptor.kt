package com.tourly.core.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class RateLimitInterceptor(
    private val rateLimitingService: RateLimitingService
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val ip = getClientIP(request)
        val bucket = rateLimitingService.resolveBucket(ip)

        val probe = bucket.tryConsumeAndReturnRemaining(1)
        if (probe.isConsumed) {
            response.addHeader("X-Rate-Limit-Remaining", probe.remainingTokens.toString())
            return true
        }

        response.status = HttpStatus.TOO_MANY_REQUESTS.value()
        response.addHeader("X-Rate-Limit-Retry-After-Seconds", (probe.nanosToWaitForRefill / 1_000_000_000).toString())
        response.writer.write("Too many requests. Please try again later.")
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
