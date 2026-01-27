package com.tourly.core.security

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean

@Component
class JWTAuthFilter(
    private val jwtUtil: JWTUtil,
    private val userDetailsService: UserDetailsService
) : GenericFilterBean() {

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val authHeader = httpRequest.getHeader(AUTHORIZATION_HEADER)

        if (authHeader != null && authHeader.startsWith(ACCESS_TOKEN_PREFIX)) {
            try {
                val token = authHeader.removePrefix(ACCESS_TOKEN_PREFIX).trim()
                val username = jwtUtil.extractUsername(token)
                logger.debug("Processing token for user: $username")

                // Only authenticate if not already authenticated
                if (SecurityContextHolder.getContext().authentication == null) {
                    val userDetails = userDetailsService.loadUserByUsername(username)

                    if (jwtUtil.isTokenValid(token, userDetails.username)) {
                        val authToken = UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.authorities
                        )
                        authToken.details = WebAuthenticationDetailsSource().buildDetails(httpRequest)
                        SecurityContextHolder.getContext().authentication = authToken
                        logger.debug("Successfully authenticated user: $username")
                    } else {
                        logger.warn("Token invalid for user: $username")
                    }
                }
            } catch (e: Exception) {
                logger.error("Error processing JWT token: ${e.message}", e)
                (response as HttpServletResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED, e.message)
                return
            }
        } else {
            logger.debug("No Bearer token found in request to: ${httpRequest.requestURI}")
        }

        chain.doFilter(request, response)
    }

    companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val ACCESS_TOKEN_PREFIX = "Bearer "
    }
}