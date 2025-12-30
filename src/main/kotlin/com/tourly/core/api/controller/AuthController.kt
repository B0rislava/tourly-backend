package com.tourly.core.api.controller

import com.tourly.core.api.dto.auth.LoginRequestDto
import com.tourly.core.api.dto.auth.LoginResponseDto
import com.tourly.core.api.dto.auth.RegisterRequestDto
import com.tourly.core.api.dto.auth.RegisterResponseDto
import com.tourly.core.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequestDto: LoginRequestDto): ResponseEntity<LoginResponseDto> {
        val response = authService.login(loginRequestDto)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody registerRequest: RegisterRequestDto): ResponseEntity<RegisterResponseDto> {
        val response = authService.register(registerRequest)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}