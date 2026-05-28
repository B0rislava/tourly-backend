package com.tourly.core.api.dto.auth

data class RefreshTokenResponseDto(
    val accessToken: String,
    val refreshToken: String
)
