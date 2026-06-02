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
import com.tourly.core.data.mapper.UserMapper
import com.tourly.core.config.Constants
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.tourly.core.data.enumeration.UserRole
import com.tourly.core.security.JWTUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.Collections

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val verificationTokenRepository: VerificationTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtil: JWTUtil,
    private val emailService: EmailService,
    @Value($$"${google.clientId}")
    private val googleClientId: String
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

        // Generate verification code and send email
        val verificationCode = generateAndSaveOtp(savedUser.id!!)
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
                errorCode = ErrorCode.EMAIL_NOT_VERIFIED,
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

        val hashedToken = hashToken(refreshToken)

        // Check if token exists in DB
        val tokenEntity = refreshTokenRepository.findByToken(hashedToken)
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
            token = hashToken(refreshToken),
            expiresAt = LocalDateTime.now().plusNanos(jwtUtil.refreshTokenExpirationMs * 1_000_000)
        )
        refreshTokenRepository.save(refreshTokenEntity)
        return refreshToken
    }

    private fun hashToken(token: String): String {
        val bytes = java.security.MessageDigest
            .getInstance("SHA-256")
            .digest(token.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
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
        checkResendRateLimit(user.id!!)

        // 1. Delete any existing codes for this user
        verificationTokenRepository.deleteByUserId(user.id!!)

        // 2. Generate new code and send email
        val verificationCode = generateAndSaveOtp(user.id!!)
        try {
            emailService.sendVerificationCode(user.email, verificationCode)
        } catch (e: Exception) {
            println("Failed to send verification email: ${e.message}")
            throw APIException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to send email")
        }
    }

    fun googleLogin(idToken: String, role: UserRole? = null): LoginResponseDto {
        val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
            .setAudience(Collections.singletonList(googleClientId))
            .build()

        val idTokenObj: GoogleIdToken = verifier.verify(idToken)
            ?: throw APIException(ErrorCode.UNAUTHORIZED, "Invalid Google ID Token")

        val payload = idTokenObj.payload
        val email = payload.email

        var user = userRepository.findByEmail(email)

        if (user == null) {
            if (role == null) {
                throw APIException(ErrorCode.GOOGLE_USER_NOT_FOUND, "Google user not registered")
            }
            // Auto-register if user doesn't exist and role is provided
            user = UserEntity(
                id = null,
                email = email,
                firstName = payload["given_name"] as String? ?: "",
                lastName = payload["family_name"] as String? ?: "",
                password = "", // No password for Google users
                role = role,
                isVerified = true, // Google emails are verified
                profilePictureUrl = payload["picture"] as String?
            )
            user = userRepository.save(user)
        }

        val token = jwtUtil.generateAccessToken(user.email, listOf(user.role.name))
        val refreshToken = createAndSaveRefreshToken(user.id!!, user.email)

        return LoginResponseDto(
            token = token,
            refreshToken = refreshToken,
            user = UserMapper.toDto(user)
        )
    }

    @Transactional
    fun sendPasswordResetCode(email: String) {
        val user = userRepository.findByEmail(email)
            ?: throw APIException(ErrorCode.RESOURCE_NOT_FOUND, "No account found with that email address.")


        // Rate-limit
        checkResendRateLimit(user.id!!)

        // Delete any existing tokens for this user and generate a fresh OTP
        verificationTokenRepository.deleteByUserId(user.id!!)
        val resetCode = generateAndSaveOtp(user.id!!)
        try {
            emailService.sendPasswordResetCode(user.email, resetCode)
        } catch (e: Exception) {
            throw APIException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to send password reset email.")
        }
    }

    fun verifyPasswordResetCode(email: String, code: String) {
        val user = userRepository.findByEmail(email)
            ?: throw APIException(ErrorCode.RESOURCE_NOT_FOUND, "User not found.")
        // Token is intentionally NOT deleted here so it can be used in the subsequent resetPassword call
        validateResetToken(user.id!!, code)
    }

    @Transactional
    fun resetPassword(email: String, resetCode: String, newPassword: String) {
        val user = userRepository.findByEmail(email)
            ?: throw APIException(ErrorCode.RESOURCE_NOT_FOUND, "User not found.")

        if (newPassword.length < 6) {
            throw APIException(ErrorCode.BAD_REQUEST, "Password must be at least 6 characters.")
        }

        if (newPassword.none { it.isDigit() }) {
            throw APIException(ErrorCode.BAD_REQUEST, "Password must contain at least one digit.")
        }

        val tokenEntity = validateResetToken(user.id!!, resetCode)

        // Update password and clean up token
        user.password = passwordEncoder.encode(newPassword)
            ?: throw APIException(ErrorCode.INTERNAL_SERVER_ERROR, "Password encoding failed")
        userRepository.save(user)
        verificationTokenRepository.delete(tokenEntity)
    }


    /**
     * Throws [APIException] if a verification/reset code was already sent for [userId]
     * within the configured rate-limit window.
     */
    private fun checkResendRateLimit(userId: Long) {
        val lastToken = verificationTokenRepository.findTopByUserIdOrderByExpiresAtDesc(userId)
        if (lastToken != null) {
            val sentAt = lastToken.expiresAt.minusMinutes(Constants.Auth.VERIFICATION_TOKEN_EXPIRATION_MINUTES)
            if (sentAt.isAfter(LocalDateTime.now().minusSeconds(Constants.Auth.RESEND_CODE_RATE_LIMIT_SECONDS))) {
                throw APIException(
                    ErrorCode.BAD_REQUEST,
                    "Please wait ${Constants.Auth.RESEND_CODE_RATE_LIMIT_SECONDS} seconds before requesting a new code."
                )
            }
        }
    }

    /** Generates a random 6-digit OTP, persists it, and returns the code string. */
    private fun generateAndSaveOtp(userId: Long): String {
        val code = (Constants.Auth.VERIFICATION_CODE_MIN..Constants.Auth.VERIFICATION_CODE_MAX).random().toString()
        val tokenEntity = VerificationTokenEntity(
            token = code,
            userId = userId,
            expiresAt = LocalDateTime.now().plusMinutes(Constants.Auth.VERIFICATION_TOKEN_EXPIRATION_MINUTES)
        )
        verificationTokenRepository.save(tokenEntity)
        return code
    }

    /**
     * Looks up [code] in the token table, verifies it belongs to [userId], and checks it
     * hasn't expired. Returns the entity so callers can delete it if required.
     */
    private fun validateResetToken(userId: Long, code: String): VerificationTokenEntity {
        val tokenEntity = verificationTokenRepository.findByToken(code)
            ?: throw APIException(ErrorCode.BAD_REQUEST, "Invalid or expired reset code.")

        if (tokenEntity.userId != userId) {
            throw APIException(ErrorCode.BAD_REQUEST, "Invalid reset code.")
        }

        if (tokenEntity.expiresAt.isBefore(LocalDateTime.now())) {
            verificationTokenRepository.delete(tokenEntity)
            throw APIException(ErrorCode.BAD_REQUEST, "Reset code has expired.")
        }

        return tokenEntity
    }
}