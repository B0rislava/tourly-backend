package com.tourly.core.service

import com.tourly.core.api.dto.User
import com.tourly.core.data.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class TestService(
    private val userRepository: UserRepository
) {

    /**
     * Get all users from database
     */
    fun getAllUsers(): List<User> {
        return userRepository.findAll().map { entity ->
            User(
                id = entity.id,
                email = entity.email,
                firstName = entity.firstName,
                lastName = entity.lastName,
                role = entity.role
            )
        }
    }

    /**
     * Get user by ID from database
     */
    fun getUserById(id: Long): User? {
        return userRepository.findById(id).map { entity ->
            User(
                id = entity.id,
                email = entity.email,
                firstName = entity.firstName,
                lastName = entity.lastName,
                role = entity.role
            )
        }.orElse(null)
    }

    /**
     * Get user by email from database
     */
    fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email).map { entity ->
            User(
                id = entity.id,
                email = entity.email,
                firstName = entity.firstName,
                lastName = entity.lastName,
                role = entity.role
            )
        }.orElse(null)
    }

    /**
     * Create user through test endpoint (only for testing)
     * Better to use /api/auth/register endpoint
     */
    fun createUser(user: User): User {
        throw UnsupportedOperationException(
            "Use /api/auth/register endpoint to create users"
        )
    }
}