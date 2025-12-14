package com.tourly.core.service

import com.tourly.core.api.dto.auth.LoginRequest
import com.tourly.core.api.dto.auth.LoginResponse
import com.tourly.core.api.dto.auth.RegisterRequest
import com.tourly.core.api.dto.auth.RegisterResponse
import com.tourly.core.data.entity.UserEntity
import com.tourly.core.data.repository.UserRepository
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

    fun register(request: RegisterRequest): RegisterResponse {
        // Validate email doesn't exist
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already exists")
        }

        // Create new user entity
        val user = UserEntity(
            id = null,
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            password = request.password.let { passwordEncoder.encode(it) } ?: throw IllegalArgumentException("Password cannot be null"),
            role = request.role
        )

        // Save to database
        userRepository.save(user)

        return RegisterResponse(
            message = "User registered successfully",
            email = user.email
        )
    }

    fun login(request: LoginRequest): LoginResponse {
        // Authenticate the user
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                request.email,
                request.password
            )
        )

        // Load user details from database
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { IllegalArgumentException("User not found") }

        // Generate JWT token with username and roles
        val token = jwtUtil.generateToken(
            username = user.email,
            roles = listOf(user.role.name)
        )

        return LoginResponse(
            token = token,
            email = user.email,
            role = user.role
        )
    }
}