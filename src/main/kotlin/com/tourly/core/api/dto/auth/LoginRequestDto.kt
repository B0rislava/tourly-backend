package com.tourly.core.api.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequestDto(

    @field:Email(message = "Email must be valid")
    @field:NotBlank(message = "Email is required")
    @field:Size(min = 5, max = 50, message = "Email must be between 5 and 50 characters")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
    val password: String
)