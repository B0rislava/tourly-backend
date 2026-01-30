package com.tourly.core.service

import com.tourly.core.api.dto.auth.LoginRequestDto
import com.tourly.core.api.dto.auth.LoginResponseDto
import com.tourly.core.api.dto.auth.RefreshTokenResponseDto
import com.tourly.core.api.dto.auth.RegisterRequestDto
import com.tourly.core.api.dto.auth.RegisterResponseDto
import com.tourly.core.data.entity.RefreshTokenEntity
import com.tourly.core.data.entity.UserEntity
import com.tourly.core.data.entity.VerificationTokenEntity
import com.tourly.core.data.repository.RefreshTokenRepository
import com.tourly.core.data.repository.UserRepository
import com.tourly.core.data.repository.VerificationTokenRepository
import com.tourly.core.exception.APIException
import com.tourly.core.exception.ErrorCode
import com.tourly.core.mapper.UserMapper
import com.tourly.core.security.JWTUtil
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val verificationTokenRepository: VerificationTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtil: JWTUtil,
    private val emailService: EmailService
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
        val savedUser = userRepository.save(user)

        // Generate verification code (6 digits)
        val verificationCode = (100000..999999).random().toString()
        val verificationTokenEntity = VerificationTokenEntity(
            token = verificationCode,
            userId = savedUser.id!!,
            expiresAt = LocalDateTime.now().plusMinutes(15)
        )
        verificationTokenRepository.save(verificationTokenEntity)

        // Send verification email
        try {
            emailService.sendVerificationCode(savedUser.email, verificationCode)
        } catch (e: Exception) {
            println("Failed to send verification email: ${e.message}")
        }

        // No token returned on register
        return RegisterResponseDto(
            token = null,
            refreshToken = null,
            user = UserMapper.toDto(user)
        )
    }

    fun login(request: LoginRequestDto): LoginResponseDto {
        // 1. Check if user exists first to provide better feedback
        val user = userRepository.findByEmail(request.email)
            ?: throw APIException(
                errorCode = ErrorCode.RESOURCE_NOT_FOUND,
                description = "The account you entered does not exist"
            )

        // 2. Attempt authentication
        if (!user.isVerified) {
            throw APIException(
                errorCode = ErrorCode.UNAUTHORIZED,
                description = "Please verify your email address before logging in."
            )
        }

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
                description = "Invalid password"
            )
        }

        val token = jwtUtil.generateAccessToken(
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
        val newAccessToken = jwtUtil.generateAccessToken(user.email, listOf(user.role.name))
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
            expiresAt = LocalDateTime.now().plusNanos(jwtUtil.refreshTokenExpirationMs * 1_000_000)
        )
        refreshTokenRepository.save(refreshTokenEntity)
        return refreshToken
    }

    @Transactional
    fun verifyEmailByCode(email: String, code: String): LoginResponseDto {
        val user = userRepository.findByEmail(email) 
            ?: throw APIException(ErrorCode.RESOURCE_NOT_FOUND, "User not found")
        
        val tokenEntity = verificationTokenRepository.findByToken(code)
            ?: throw APIException(ErrorCode.BAD_REQUEST, "Invalid or expired verification code.")

        if (tokenEntity.userId != user.id) {
            throw APIException(ErrorCode.BAD_REQUEST, "Invalid verification code.")
        }

        if (tokenEntity.expiresAt.isBefore(LocalDateTime.now())) {
            verificationTokenRepository.delete(tokenEntity)
            throw APIException(ErrorCode.BAD_REQUEST, "Verification code has expired.")
        }

        user.isVerified = true
        userRepository.save(user)
        verificationTokenRepository.delete(tokenEntity)

        // Generate tokens ONLY after successful verification
        val accessToken = jwtUtil.generateAccessToken(user.email, listOf(user.role.name))
        val refreshToken = createAndSaveRefreshToken(user.id!!, user.email)

        return LoginResponseDto(
            token = accessToken,
            refreshToken = refreshToken,
            user = UserMapper.toDto(user)
        )
    }

    @Transactional
    fun resendVerificationCode(email: String) {
        val user = userRepository.findByEmail(email)
            ?: throw APIException(ErrorCode.RESOURCE_NOT_FOUND, "User not found")

        // Rate limiting: Check if a code was sent recently (e.g., within the last 60 seconds)
        val lastToken = verificationTokenRepository.findTopByUserIdOrderByExpiresAtDesc(user.id!!)
        if (lastToken != null) {
            val sentAt = lastToken.expiresAt.minusMinutes(15) // We set expiresAt = now + 15m
            if (sentAt.isAfter(LocalDateTime.now().minusSeconds(60))) {
                throw APIException(ErrorCode.BAD_REQUEST, "Please wait 60 seconds before requesting a new code.")
            }
        }

        // 1. Delete any existing codes for this user
        verificationTokenRepository.deleteByUserId(user.id!!)

        // 2. Generate new 6-digit code
        val verificationCode = (100000..999999).random().toString()
        val verificationTokenEntity = VerificationTokenEntity(
            token = verificationCode,
            userId = user.id!!,
            expiresAt = LocalDateTime.now().plusMinutes(15)
        )
        verificationTokenRepository.save(verificationTokenEntity)

        // 3. Send new verification email
        try {
            emailService.sendVerificationCode(user.email, verificationCode)
        } catch (e: Exception) {
            println("Failed to send verification email: ${e.message}")
            throw APIException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to send email")
        }
    }

    fun googleLogin(idToken: String): LoginResponseDto {
        // TODO: Verify idToken with Google
        throw APIException(ErrorCode.INTERNAL_SERVER_ERROR, "Google Login logic is being implemented.")
    }
}