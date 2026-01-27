package com.tourly.core.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class UpdateProfileRequestDto(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val firstName: String,
    @field:NotBlank val lastName: String,
    val bio: String? = null,
    val certifications: String? = null,
    val password: String? = null
)
