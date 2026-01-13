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

    @Value($$"${jwt.accessTokenExpirationMs}")
    private var accessTokenExpirationMs: Long = 900000 // Default 15 minutes

    @Value($$"${jwt.refreshTokenExpirationMs}")
    var refreshTokenExpirationMs: Long = 604800000 // Default 7 days

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    // Generate JWT token with username and roles
    fun generateToken(username: String, roles: Collection<String>): String {
        val claims = mutableMapOf<String, Any>()
        val mappedRoles = roles.map { "ROLE_$it" }
        claims[ROLES_CLAIM] = mappedRoles
        claims[USERNAME_CLAIM] = username
        return createAccessToken(claims, username)
    }

    // Generate refresh token with username
    fun generateRefreshToken(username: String): String {
        val claims = mutableMapOf<String, Any>()
        claims[USERNAME_CLAIM] = username
        claims[TOKEN_TYPE_CLAIM] = "refresh"
        return createRefreshToken(claims, username)
    }

    // Validate refresh token
    fun isRefreshTokenValid(token: String): Boolean {
        return try {
            val claims = extractAllClaims(token)
            claims[TOKEN_TYPE_CLAIM] == "refresh" && !isTokenExpired(token)
        } catch (_: Exception) {
            false
        }
    }

    // Create JWT token with claims
    private fun createAccessToken(claims: Map<String, Any>, subject: String): String {
        val now = Date()
        val expiryDate = Date(now.time + accessTokenExpirationMs)

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }

    // Create refresh token with claims
    private fun createRefreshToken(claims: Map<String, Any>, subject: String): String {
        val now = Date()
        val expiryDate = Date(now.time + refreshTokenExpirationMs)

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }

    // Extract username from token
    fun extractUsername(token: String): String {
        return extractAllClaims(token).subject
    }


    // Validate token against username
    fun isTokenValid(token: String, username: String): Boolean {
        return try {
            val extractedUsername = extractUsername(token)
            extractedUsername == username && !isTokenExpired(token)
        } catch (_: Exception) {
            false
        }
    }

    // Check if token is expired
    private fun isTokenExpired(token: String): Boolean {
        return extractAllClaims(token).expiration.before(Date())
    }
    
    // Extract all claims from token
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
        const val TOKEN_TYPE_CLAIM = "tokenType"
    }
}