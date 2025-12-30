package com.tourly.core.service

import com.tourly.core.api.dto.UserDto
import com.tourly.core.data.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class TestService(
    private val userRepository: UserRepository
) {

    /**
     * Get all users from database
     */
    fun getAllUsers(): List<UserDto> {
        return userRepository.findAll().map { entity ->
            UserDto(
                id = entity.id,
                email = entity.email,
                firstName = entity.firstName,
                lastName = entity.lastName,
                role = entity.role,
                profilePictureUrl = entity.profilePictureUrl
            )
        }
    }

    /**
     * Get user by ID from database
     */
    fun getUserById(id: Long): UserDto? {
        return userRepository.findById(id).map { entity ->
            UserDto(
                id = entity.id,
                email = entity.email,
                firstName = entity.firstName,
                lastName = entity.lastName,
                role = entity.role,
                profilePictureUrl = entity.profilePictureUrl
            )
        }.orElse(null)
    }

    /**
     * Get user by email from database
     */
    fun getUserByEmail(email: String): UserDto? {
        return userRepository.findByEmail(email).map { entity ->
            UserDto(
                id = entity.id,
                email = entity.email,
                firstName = entity.firstName,
                lastName = entity.lastName,
                role = entity.role,
                profilePictureUrl = entity.profilePictureUrl
            )
        }.orElse(null)
    }

    /**
     * Create user through test endpoint (only for testing)
     * Better to use /api/auth/register endpoint
     */
    fun createUser(user: UserDto): UserDto {
        throw UnsupportedOperationException(
            "Use /api/auth/register endpoint to create users"
        )
    }
}