package com.tourly.core.api.dto.auth

import com.tourly.core.data.enumeration.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequestDto(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    @field:Size(min = 5, max = 50, message = "Email must be between 5 and 50 characters")
    val email: String,

    @field:NotBlank(message = "First name is required")
    @field:Size(min = 2, max = 30, message = "First name must be between 2 and 30 characters")
    val firstName: String,

    @field:NotBlank(message = "Last name is required")
    @field:Size(min = 2, max = 30, message = "Last name must be between 2 and 30 characters")
    val lastName: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
    val password: String,

    val role: UserRole
)