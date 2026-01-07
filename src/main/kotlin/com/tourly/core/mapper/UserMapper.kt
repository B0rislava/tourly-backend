package com.tourly.core.mapper

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
            profilePictureUrl = user.profilePictureUrl
        )
}
