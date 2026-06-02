package com.tourly.core.api.controller

import com.tourly.core.api.dto.auth.LoginRequestDto
import com.tourly.core.api.dto.auth.LoginResponseDto
import com.tourly.core.api.dto.auth.RefreshTokenRequestDto
import com.tourly.core.api.dto.auth.RefreshTokenResponseDto
import com.tourly.core.api.dto.auth.RegisterRequestDto
import com.tourly.core.api.dto.auth.RegisterResponseDto
import com.tourly.core.api.dto.auth.ResetPasswordRequestDto
import com.tourly.core.data.enumeration.UserRole
import com.tourly.core.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


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

    @Operation(summary = "Verify email code", description = "Verifies a user's email address and returns auth tokens")
    @PostMapping("/verify-code")
    fun verifyCode(
        @RequestParam email: String,
        @RequestParam code: String
    ): ResponseEntity<LoginResponseDto> {
        val response = authService.verifyEmailByCode(email, code)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "Resend verification code", description = "Generates and sends a new verification code to the user's email")
    @PostMapping("/resend-code")
    fun resendCode(@RequestParam email: String): ResponseEntity<Unit> {
        authService.resendVerificationCode(email)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "Google Login", description = "Authenticates a user via Google ID Token")
    @PostMapping("/google")
    fun googleLogin(
        @RequestParam idToken: String,
        @RequestParam(required = false) role: UserRole?
    ): ResponseEntity<LoginResponseDto> {
        val response = authService.googleLogin(idToken, role)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "Forgot password", description = "Sends a 6-digit OTP to the user's email for password reset")
    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestParam email: String): ResponseEntity<Unit> {
        authService.sendPasswordResetCode(email)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "Verify reset code", description = "Validates the 6-digit password reset OTP")
    @PostMapping("/verify-reset-code")
    fun verifyResetCode(
        @RequestParam email: String,
        @RequestParam code: String
    ): ResponseEntity<Unit> {
        authService.verifyPasswordResetCode(email, code)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "Reset password", description = "Verifies the OTP and updates the user's password")
    @PostMapping("/reset-password")
    fun resetPassword(@RequestBody request: ResetPasswordRequestDto): ResponseEntity<Unit> {
        authService.resetPassword(request.email, request.resetCode, request.newPassword)
        return ResponseEntity.ok().build()
    }
}