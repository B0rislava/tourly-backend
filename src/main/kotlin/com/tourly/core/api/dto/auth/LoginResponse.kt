package com.tourly.core.api.dto.auth

import com.tourly.core.api.dto.User

data class LoginResponse(
    val token: String,
    val user: User
)