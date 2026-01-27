package com.tourly.core.service

import com.tourly.core.api.dto.UpdateProfileRequestDto
import com.tourly.core.api.dto.UserDto
import com.tourly.core.data.repository.BookingRepository
import com.tourly.core.data.repository.RefreshTokenRepository
import com.tourly.core.data.repository.TourRepository
import com.tourly.core.data.repository.UserRepository
import com.tourly.core.data.enumeration.UserRole
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
    private val refreshTokenRepository: RefreshTokenRepository,
    private val bookingRepository: BookingRepository,
    private val tourRepository: TourRepository
) {

    @Transactional(readOnly = true)
    fun getCurrentUserProfile(userId: Long): UserDto {
        val user = findUser(userId)
        return UserMapper.toDto(user)
    }

    @Transactional
    fun updateProfile(userId: Long, request: UpdateProfileRequestDto): UserDto {
        val user = findUser(userId)

        if (request.email != user.email) {
            if (userRepository.existsByEmail(request.email)) {
                throw APIException(
                    errorCode = ErrorCode.CONFLICT,
                    description = "Email already in use: ${request.email}"
                )
            }
            user.email = request.email
        }
        
        user.firstName = request.firstName
        user.lastName = request.lastName
        user.bio = request.bio
        user.certifications = request.certifications

        if (!request.password.isNullOrBlank()) {
            user.password = passwordEncoder.encode(request.password).toString()
        }

        return UserMapper.toDto(user)
    }

    @Transactional
    fun updateProfilePicture(userId: Long, file: MultipartFile): UserDto {
        val imageUrl = cloudinaryService.uploadImage(file, "avatars", "user_$userId")

        val user = findUser(userId)
        user.profilePictureUrl = imageUrl

        return UserMapper.toDto(user)
    }

    @Transactional
    fun deleteUser(userId: Long) {
        val user = findUser(userId)
        
        // 1. Delete Refresh Tokens
        refreshTokenRepository.deleteAllByUserId(userId)
        
        // 3. Handle Bookings and Tours
        if (user.role == UserRole.TRAVELER) {
            bookingRepository.deleteAllByUserId(userId)
        } else if (user.role == UserRole.GUIDE) {
            // Delete bookings for all tours created by this guide
            bookingRepository.deleteAllByTourGuideId(userId)
            // Delete the tours themselves
            tourRepository.deleteAllByGuideId(userId)
        }
        
        // 4. Delete the User
        userRepository.delete(user)
    }

    @Transactional(readOnly = true)
    fun getUserByEmail(email: String) =
        userRepository.findByEmail(email)
            ?: throw APIException(
                errorCode = ErrorCode.RESOURCE_NOT_FOUND,
                description = "User not found with email: $email"
            )

    private fun findUser(userId: Long) =
        userRepository.findByIdOrNull(userId)
            ?: throw APIException(
                errorCode = ErrorCode.RESOURCE_NOT_FOUND,
                description = "User not found: $userId"
            )
}
