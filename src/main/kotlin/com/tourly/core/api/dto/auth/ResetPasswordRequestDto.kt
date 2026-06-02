package com.tourly.core.api.dto.auth

data class ResetPasswordRequestDto(
    val email: String,
    val resetCode: String,
    val newPassword: String
)
