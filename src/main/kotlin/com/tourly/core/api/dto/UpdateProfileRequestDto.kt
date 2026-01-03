package com.tourly.core.api.dto

data class UpdateProfileRequestDto(
    val email: String,
    val firstName: String,
    val lastName: String,
    val password: String? = null
)
