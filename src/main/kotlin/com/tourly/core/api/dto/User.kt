package com.tourly.core.api.dto

import com.tourly.core.data.enumeration.UserRole

data class User(
    val id: Long? = null,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole
)