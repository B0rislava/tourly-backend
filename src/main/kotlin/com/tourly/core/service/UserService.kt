package com.tourly.core.service

import com.tourly.core.api.dto.UpdateProfileRequestDto
import com.tourly.core.api.dto.UserDto
import com.tourly.core.data.entity.UserEntity
import com.tourly.core.data.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val cloudinaryService: CloudinaryService
) {

    @Transactional
    fun updateProfile(id: Long, request: UpdateProfileRequestDto): UserDto {
        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("User not found") }

        user.firstName = request.firstName
        user.lastName = request.lastName
        user.email = request.email

        if (!request.password.isNullOrBlank()) {
            user.password = passwordEncoder.encode(request.password).toString()
        }

        val updatedUser = userRepository.save(user)

        return mapToDto(updatedUser)
    }

    @Transactional
    fun updateProfilePicture(userId: Long, file: MultipartFile): UserDto {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val imageUrl = cloudinaryService.uploadImage(file, userId)
        user.profilePictureUrl = imageUrl

        val updatedUser = userRepository.save(user)
        return mapToDto(updatedUser)
    }

    private fun mapToDto(user: UserEntity): UserDto {
        return UserDto(
            id = user.id,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            role = user.role,
            profilePictureUrl = user.profilePictureUrl
        )
    }
}
