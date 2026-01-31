package com.tourly.core.data.mapper

import com.tourly.core.api.dto.UserDto
import com.tourly.core.data.entity.UserEntity
import org.springframework.stereotype.Component

@Component
object UserMapper {

    fun toDto(user: UserEntity): UserDto =
        UserDto(
            id = user.id,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            role = user.role,
            profilePictureUrl = user.profilePictureUrl,
            bio = user.bio,
            rating = user.rating ?: 0.0,
            reviewsCount = user.reviewsCount ?: 0,
            followerCount = user.followerCount ?: 0,
            certifications = user.certifications,
            toursCompleted = user.toursCompleted ?: 0,
            isVerified = user.isVerified
        )
}
