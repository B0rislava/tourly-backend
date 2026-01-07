package com.tourly.core.service

import com.tourly.core.api.dto.auth.LoginRequestDto
import com.tourly.core.api.dto.auth.LoginResponseDto
import com.tourly.core.api.dto.auth.RegisterRequestDto
import com.tourly.core.api.dto.auth.RegisterResponseDto
import com.tourly.core.api.dto.UserDto
import com.tourly.core.data.entity.UserEntity
import com.tourly.core.data.repository.UserRepository
import com.tourly.core.exception.APIException
import com.tourly.core.exception.ErrorCode
import com.tourly.core.security.JWTUtil
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
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

        return RegisterResponseDto(
            message = "User registered successfully",
            email = user.email
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
        } catch (e: Exception) {
            throw APIException(
                errorCode = ErrorCode.UNAUTHORIZED,
                description = "Invalid email or password"
            )
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

        return LoginResponseDto(
            token = token,
            user = UserDto(
                id = user.id,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                role = user.role,
                profilePictureUrl = user.profilePictureUrl
            )
        )
    }
}