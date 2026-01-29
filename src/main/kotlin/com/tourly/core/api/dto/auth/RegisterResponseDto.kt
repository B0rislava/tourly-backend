package com.tourly.core.api.dto.auth

import com.tourly.core.api.dto.UserDto

data class RegisterResponseDto(
    val token: String? = null,
    val refreshToken: String? = null,
    val user: UserDto
)