package com.tourly.core.service

import com.tourly.core.api.dto.UpdateProfileRequestDto
import com.tourly.core.api.dto.UserDto
import com.tourly.core.data.repository.UserRepository
import com.tourly.core.exception.APIException
import com.tourly.core.exception.ErrorCode
import com.tourly.core.mapper.UserMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val cloudinaryService: CloudinaryService,
) {

    @Transactional(readOnly = true)
    fun getCurrentUserProfile(userId: Long): UserDto {
        val user = findUser(userId)
        return UserMapper.toDto(user)
    }

    @Transactional
    fun updateProfile(userId: Long, request: UpdateProfileRequestDto): UserDto {
        val user = findUser(userId)

        user.firstName = request.firstName
        user.lastName = request.lastName
        user.email = request.email

        if (!request.password.isNullOrBlank()) {
            user.password = passwordEncoder.encode(request.password).toString()
        }

        return UserMapper.toDto(user)
    }

    @Transactional
    fun updateProfilePicture(userId: Long, file: MultipartFile): UserDto {
        val imageUrl = cloudinaryService.uploadImage(file, userId)

        val user = findUser(userId)
        user.profilePictureUrl = imageUrl

        return UserMapper.toDto(user)
    }

    private fun findUser(userId: Long) =
        userRepository.findByIdOrNull(userId)
            ?: throw APIException(
                errorCode = ErrorCode.RESOURCE_NOT_FOUND,
                description = "User not found: $userId"
                )
            }
