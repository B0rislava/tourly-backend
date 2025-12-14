package com.tourly.core.api.dto.auth

import com.tourly.core.data.enumeration.UserRole

data class LoginResponse(
    val token: String,
    val type: String = "Bearer",
    val email: String,
    val role: UserRole
)