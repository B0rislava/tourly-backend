package com.tourly.core.service

import com.tourly.core.config.Constants
import com.tourly.core.api.dto.UpdateProfileRequestDto
import com.tourly.core.api.dto.UserDto
import com.tourly.core.data.repository.BookingRepository
import com.tourly.core.data.repository.RefreshTokenRepository
import com.tourly.core.data.repository.TourRepository
import com.tourly.core.data.repository.UserRepository
import com.tourly.core.data.enumeration.UserRole
import com.tourly.core.exception.APIException
import com.tourly.core.exception.ErrorCode
import com.tourly.core.data.mapper.UserMapper
import com.tourly.core.data.repository.FollowRepository
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
    private val tourRepository: TourRepository,
    private val followRepository: FollowRepository,
    private val notificationService: NotificationService
) {

    @Transactional(readOnly = true)
    fun getCurrentUserProfile(userId: Long): UserDto {
        val user = findUser(userId)
        val followerCount = followRepository.countFollowersByUserId(userId).toInt()
        val followingCount = followRepository.countFollowingByUserId(userId).toInt()
        return UserMapper.toDto(user, followerCount = followerCount, followingCount = followingCount)
    }

    @Transactional(readOnly = true)
    fun getUserProfileById(userId: Long, currentUserId: Long? = null): UserDto {
        val user = findUser(userId)
        val isFollowing = currentUserId?.let { 
            followRepository.existsByFollowerIdAndFollowingId(it, userId)
        } ?: false
        val followerCount = followRepository.countFollowersByUserId(userId).toInt()
        val followingCount = followRepository.countFollowingByUserId(userId).toInt()
        return UserMapper.toDto(user, isFollowing, followerCount, followingCount)
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

        val updatedUser = userRepository.save(user)
        val followerCount = followRepository.countFollowersByUserId(userId).toInt()
        val followingCount = followRepository.countFollowingByUserId(userId).toInt()
        return UserMapper.toDto(updatedUser, followerCount = followerCount, followingCount = followingCount)
    }

    @Transactional
    fun updateProfilePicture(userId: Long, file: MultipartFile): UserDto {
        val imageUrl = cloudinaryService.uploadImage(
            file, 
            Constants.Cloudinary.FOLDER_AVATARS, 
            "${Constants.Cloudinary.PREFIX_USER}$userId"
        )

        val user = findUser(userId)
        user.profilePictureUrl = imageUrl
        val updatedUser = userRepository.save(user)

        val followerCount = followRepository.countFollowersByUserId(userId).toInt()
        val followingCount = followRepository.countFollowingByUserId(userId).toInt()
        return UserMapper.toDto(updatedUser, followerCount = followerCount, followingCount = followingCount)
    }

    @Transactional
    fun deleteUser(userId: Long) {
        val user = findUser(userId)
        
        // 1. Delete Refresh Tokens
        refreshTokenRepository.deleteAllByUserId(userId)
        
        // 2. Delete Follow Relationships
        followRepository.deleteAllByUserId(userId)
        
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

    @Transactional(readOnly = true)
    fun getUserIdByEmail(email: String): Long {
        return getUserByEmail(email).id 
            ?: throw APIException(ErrorCode.INTERNAL_SERVER_ERROR, "User ID is null for $email")
    }

    @Transactional
    fun followUser(followerId: Long, followingId: Long) {
        if (followerId == followingId) {
            throw APIException(
                errorCode = ErrorCode.BAD_REQUEST,
                description = "Cannot follow yourself"
            )
        }

        // Check if both users exist
        val follower = findUser(followerId)
        val userToFollow = findUser(followingId)

        // Check if already following
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw APIException(
                errorCode = ErrorCode.CONFLICT,
                description = "Already following this user"
            )
        }

        // Create follow relationship
        followRepository.save(
            com.tourly.core.data.entity.FollowEntity(
                followerId = followerId,
                followingId = followingId
            )
        )

        // Send notification
        notificationService.createNotification(
            user = userToFollow,
            title = "New Follower",
            message = "${follower.firstName} ${follower.lastName}",
            type = "FOLLOW",
            relatedId = follower.id
        )
    }

    @Transactional
    fun unfollowUser(followerId: Long, followingId: Long) {
        if (followerId == followingId) {
            throw APIException(
                errorCode = ErrorCode.BAD_REQUEST,
                description = "Cannot unfollow yourself"
            )
        }

        // Check if both users exist
        findUser(followerId)
        findUser(followingId)

        // Delete follow relationship
        val deletedCount = followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId)
        
        if (deletedCount == 0) {
            throw APIException(
                errorCode = ErrorCode.RESOURCE_NOT_FOUND,
                description = "Not following this user"
            )
        }
    }

    private fun findUser(userId: Long) =
        userRepository.findByIdOrNull(userId)
            ?: throw APIException(
                errorCode = ErrorCode.RESOURCE_NOT_FOUND,
                description = "User not found: $userId"
            )
}
