package com.tourly.core.service

import java.time.LocalDateTime
import org.springframework.stereotype.Service
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.transaction.annotation.Transactional
import com.tourly.core.data.entity.UserEntity
import com.tourly.core.data.entity.RefreshTokenEntity
import com.tourly.core.data.repository.RefreshTokenRepository
import com.tourly.core.api.dto.auth.LoginRequestDto
import com.tourly.core.api.dto.auth.LoginResponseDto
import com.tourly.core.api.dto.auth.RegisterRequestDto
import com.tourly.core.api.dto.auth.RegisterResponseDto
import com.tourly.core.api.dto.auth.RefreshTokenResponseDto
import com.tourly.core.data.repository.UserRepository
import com.tourly.core.exception.APIException
import com.tourly.core.exception.ErrorCode
import com.tourly.core.mapper.UserMapper
import com.tourly.core.security.JWTUtil
import org.springframework.security.authentication.BadCredentialsException

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtil: JWTUtil
) {

    fun register(request: RegisterRequestDto): RegisterResponseDto {
        // Validate email doesn't exist
        if (userRepository.existsByEmail(request.email)) {
            throw APIException(
                errorCode = ErrorCode.BAD_REQUEST,
                description = "Email already exists."
            )
        }

        val password = request.password.takeIf { it.isNotBlank() }
            ?: throw APIException(
                errorCode = ErrorCode.BAD_REQUEST,
                description = "Password cannot be null or blank"
            )

        val encodedPassword = passwordEncoder.encode(password)
            ?: throw APIException(ErrorCode.INTERNAL_SERVER_ERROR, "Password encoding failed")

        // Create new user entity
        val user = UserEntity(
            id = null,
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            password = encodedPassword,
            role = request.role,
            profilePictureUrl = null
        )

        // Save to database
        userRepository.save(user)

        // Generate tokens
        val accessToken = jwtUtil.generateToken(user.email, listOf(user.role.name))
        val refreshToken = createAndSaveRefreshToken(user.id!!, user.email)

        return RegisterResponseDto(
            token = accessToken,
            refreshToken = refreshToken,
            user = UserMapper.toDto(user)
        )
    }

    fun login(request: LoginRequestDto): LoginResponseDto {
        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    request.email,
                    request.password
                )
            )
        } catch (e: BadCredentialsException) {
            throw APIException(
                errorCode = ErrorCode.UNAUTHORIZED,
                description = "Invalid email or password"
            )
        } catch (e: Exception) {
            throw e
        }

        // Load user details from database
        val user = userRepository.findByEmail(request.email)
            ?: throw APIException(
                errorCode = ErrorCode.RESOURCE_NOT_FOUND,
                description = "User not found with email: ${request.email}"
            )

        // Generate JWT token with username and roles
        val token = jwtUtil.generateToken(
            username = user.email,
            roles = listOf(user.role.name)
        )
        
        val refreshToken = createAndSaveRefreshToken(user.id!!, user.email)

        return LoginResponseDto(
            token = token,
            refreshToken = refreshToken,
            user = UserMapper.toDto(user)
        )
    }

    @Transactional
    fun refreshAccessToken(refreshToken: String): RefreshTokenResponseDto {
        // Validate token format and expiration
        if (!jwtUtil.isRefreshTokenValid(refreshToken)) {
            throw APIException(ErrorCode.UNAUTHORIZED, "Invalid refresh token")
        }

        // Check if token exists in DB
        val tokenEntity = refreshTokenRepository.findByToken(refreshToken)
            ?: throw APIException(ErrorCode.UNAUTHORIZED, "Refresh token not found or revoked")
            
        // Setup for rotation: cleanup old token
        refreshTokenRepository.delete(tokenEntity)
        
        // Check DB expiration (double check)
        if (tokenEntity.expiresAt.isBefore(LocalDateTime.now())) {
            throw APIException(ErrorCode.UNAUTHORIZED, "Refresh token expired")
        }

        // Get user
        val user = userRepository.findById(tokenEntity.userId).orElseThrow {
            APIException(ErrorCode.RESOURCE_NOT_FOUND, "User not found")
        }

        // Generate new tokens
        val newAccessToken = jwtUtil.generateToken(user.email, listOf(user.role.name))
        val newRefreshToken = createAndSaveRefreshToken(user.id!!, user.email)

        return RefreshTokenResponseDto(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    fun createAndSaveRefreshToken(userId: Long, email: String): String {
        val refreshToken = jwtUtil.generateRefreshToken(email)
        val refreshTokenEntity = RefreshTokenEntity(
            userId = userId,
            token = refreshToken,
            expiresAt = LocalDateTime.now().plusNanos(jwtUtil.refreshTokenExpirationMs * 1_000_000) // Synced with JWTUtil
        )
        refreshTokenRepository.save(refreshTokenEntity)
        return refreshToken
    }
}