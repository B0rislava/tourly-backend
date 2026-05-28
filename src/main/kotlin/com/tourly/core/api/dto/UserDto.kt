package com.tourly.core.api.dto

import com.tourly.core.data.enumeration.UserRole

data class UserDto(
    val id: Long? = null,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val profilePictureUrl: String? = null,
    val bio: String? = null,
    val rating: Double = 0.0,
    val reviewsCount: Int = 0,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val certifications: String? = null,
    val toursCompleted: Int = 0,
    val isVerified: Boolean = false,
    val isFollowing: Boolean = false
)