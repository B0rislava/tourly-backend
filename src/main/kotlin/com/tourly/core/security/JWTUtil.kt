package com.tourly.core.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JWTUtil {

    @Value($$"${jwt.secret}")
    private lateinit var jwtSecret: String

    @Value($$"${jwt.expirationMs}")
    private var jwtExpirationMs: Long = 86400000 // Default 24 hours

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    /**
     * Generate JWT token with username and roles
     */
    fun generateToken(username: String, roles: Collection<String>): String {
        val claims = mutableMapOf<String, Any>()
        val mappedRoles = roles.map { "ROLE_$it" }
        claims[ROLES_CLAIM] = mappedRoles
        claims[USERNAME_CLAIM] = username
        return createToken(claims, username)
    }

    /**
     * Create JWT token with claims
     */
    private fun createToken(claims: Map<String, Any>, subject: String): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationMs)

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }

    /**
     * Extract username from token
     */
    fun extractUsername(token: String): String {
        return extractAllClaims(token).subject
    }

    /**
     * Extract roles from token
     */
    fun extractRoles(token: String): List<String> {
        return when (val rolesClaim = extractAllClaims(token)[ROLES_CLAIM]) {
            is List<*> -> rolesClaim.filterIsInstance<String>()
            else -> emptyList()
        }
    }

    /**
     * Validate token against username
     */
    fun isTokenValid(token: String, username: String): Boolean {
        return try {
            val extractedUsername = extractUsername(token)
            extractedUsername == username && !isTokenExpired(token)
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Check if token is expired
     */
    private fun isTokenExpired(token: String): Boolean {
        return extractAllClaims(token).expiration.before(Date())
    }

    /**
     * Extract all claims from token
     */
    private fun extractAllClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
    }

    companion object {
        const val ROLES_CLAIM = "roles"
        const val USERNAME_CLAIM = "username"
    }
}