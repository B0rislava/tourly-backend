package com.tourly.core.api.controller

import jakarta.validation.Valid
import com.tourly.core.api.dto.auth.LoginRequestDto
import com.tourly.core.api.dto.auth.LoginResponseDto
import com.tourly.core.api.dto.auth.RegisterRequestDto
import com.tourly.core.api.dto.auth.RegisterResponseDto
import com.tourly.core.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.tourly.core.api.dto.auth.RefreshTokenRequestDto
import com.tourly.core.api.dto.auth.RefreshTokenResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag


@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @Operation(summary = "Login user", description = "Authenticates a user and returns access and refresh tokens")
    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequestDto: LoginRequestDto): ResponseEntity<LoginResponseDto> {
        val response = authService.login(loginRequestDto)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "Register user", description = "Creates a new user account")
    @PostMapping("/register")
    fun register(@Valid @RequestBody registerRequest: RegisterRequestDto): ResponseEntity<RegisterResponseDto> {
        val response = authService.register(registerRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @Operation(summary = "Refresh token", description = "Uses a refresh token to obtain a new access token")
    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequestDto): ResponseEntity<RefreshTokenResponseDto> {
        val response = authService.refreshAccessToken(request.refreshToken)
        return ResponseEntity.ok(response)
    }
}