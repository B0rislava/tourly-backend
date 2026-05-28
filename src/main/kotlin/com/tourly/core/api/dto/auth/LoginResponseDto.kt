package com.tourly.core.api.dto.auth

import com.tourly.core.api.dto.UserDto

data class LoginResponseDto(
    val token: String,
    val refreshToken: String,
    val user: UserDto
)