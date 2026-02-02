package com.tourly.core.service

import com.tourly.core.api.dto.tour.CreateTourRequestDto
import com.tourly.core.api.dto.tour.CreateTourResponseDto
import com.tourly.core.data.entity.TagEntity
import com.tourly.core.data.enumeration.UserRole
import com.tourly.core.data.repository.TagRepository
import com.tourly.core.data.repository.TourRepository
import com.tourly.core.data.repository.UserRepository
import com.tourly.core.data.repository.BookingRepository
import com.tourly.core.data.specification.TourSpecification
import com.tourly.core.exception.APIException
import com.tourly.core.exception.ErrorCode
import com.tourly.core.data.mapper.TourMapper
import com.tourly.core.data.repository.FollowRepository
import com.tourly.core.config.Constants
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

@Service
class TourService(
    private val tourRepository: TourRepository,
    private val userRepository: UserRepository,
    private val cloudinaryService: CloudinaryService,
    private val tagRepository: TagRepository,
    private val bookingRepository: BookingRepository,
    private val notificationService: NotificationService,
    private val followRepository: FollowRepository
) {

    @Transactional
    fun createTour(guideEmail: String, request: CreateTourRequestDto, image: MultipartFile?): CreateTourResponseDto {
        val guide = userRepository.findByEmail(guideEmail)
            ?: throw APIException(
                errorCode = ErrorCode.RESOURCE_NOT_FOUND,
                description = "Guide not found with email: $guideEmail"
            )

        if (guide.role != UserRole.GUIDE) throw APIException(ErrorCode.FORBIDDEN, "User is not a guide")

        // Fetch tags if provided
        val tags = fetchTags(request.tagIds)

        // First, create and save the tour without image to get the ID
        val tour = TourMapper.toEntity(guide, request, tags)

        val savedTour = tourRepository.save(tour)

        // Now upload the image using the actual tour ID
        if (image != null) {
            val imageUrl = cloudinaryService.uploadImage(
                image,
                Constants.Cloudinary.FOLDER_TOUR_IMAGES,
                "${Constants.Cloudinary.PREFIX_TOUR}${savedTour.id}"
            )
            savedTour.imageUrl = imageUrl
            tourRepository.save(savedTour)
        }

        // Notify followers
        val followerIds = followRepository.findFollowerIdsByUserId(guide.id!!)
        if (followerIds.isNotEmpty()) {
            val followers = userRepository.findAllById(followerIds)
            followers.forEach { follower ->
                notificationService.createNotification(
                    user = follower,
                    title = "New Tour from ${guide.firstName}",
                    message = "${guide.firstName} ${guide.lastName}|${savedTour.title}",
                    type = "NEW_TOUR",
                    relatedId = savedTour.id
                )
            }
        }

        return TourMapper.toDto(savedTour)
    }

    @Transactional(readOnly = true)
    fun getToursByGuide(guideEmail: String, currentUserEmail: String? = null): List<CreateTourResponseDto> {
        val guide = userRepository.findByEmail(guideEmail)
            ?: throw APIException(
                errorCode = ErrorCode.RESOURCE_NOT_FOUND,
                description = "Guide not found with email: $guideEmail"
            )

        val guideId = guide.id
            ?: throw APIException(ErrorCode.INTERNAL_SERVER_ERROR, "Guide ID is null after DB fetch")

        val currentUser = currentUserEmail?.let { userRepository.findByEmail(it) }

        return tourRepository.findAllByGuideIdOrderByCreatedAtDesc(guideId)
            .filter { it.status != Constants.TourStatus.DELETED }
            .map { TourMapper.toDto(it, currentUser) }
    }

    @Transactional(readOnly = true)
    fun getToursByGuideId(guideId: Long, currentUserEmail: String? = null): List<CreateTourResponseDto> {
        val currentUser = currentUserEmail?.let { userRepository.findByEmail(it) }
        return tourRepository.findAllByGuideIdOrderByCreatedAtDesc(guideId)
            .filter { it.status != Constants.TourStatus.DELETED }
            .map { TourMapper.toDto(it, currentUser) }
    }

    @Transactional(readOnly = true)
    fun getAllActiveTours(
        currentUserEmail: String?,
        location: String?,
        tags: List<String>?,
        minPrice: Double?,
        maxPrice: Double?,
        minRating: Double?,
        scheduledAfter: LocalDate?,
        scheduledBefore: LocalDate?,
        maxGroupSize: Int?,
        sortBy: String?,
        sortOrder: String?
    ): List<CreateTourResponseDto> {
        val currentUser = currentUserEmail?.let { userRepository.findByEmail(it) }
        val spec = TourSpecification.buildSpecification(
            location = location,
            tagNames = tags,
            minPrice = minPrice,
            maxPrice = maxPrice,
            minRating = minRating,
            scheduledAfter = scheduledAfter,
            scheduledBefore = scheduledBefore,
            maxGroupSize = maxGroupSize
        )

        val sort = createSort(sortBy, sortOrder)

        return tourRepository.findAll(spec, sort)
            .map { TourMapper.toDto(it, currentUser) }
    }

    private fun createSort(sortBy: String?, sortOrder: String?): Sort {
        val field = when (sortBy?.lowercase()) {
            "price" -> "pricePerPerson"
            "rating" -> "rating"
            "date" -> "scheduledDate"
            "duration" -> "duration"
            else -> "createdAt"
        }

        val direction = if (sortOrder?.lowercase() == "asc")
            Sort.Direction.ASC
        else
            Sort.Direction.DESC

        return Sort.by(direction, field)
    }

    @Transactional
    fun updateTour(id: Long, guideEmail: String, request: CreateTourRequestDto, image: MultipartFile?): CreateTourResponseDto {
        val tour = tourRepository.findById(id)
            .orElseThrow { APIException(ErrorCode.RESOURCE_NOT_FOUND, "Tour not found with id: $id") }

        if (tour.guide.email != guideEmail) {
            throw APIException(ErrorCode.FORBIDDEN, "You are not authorized to update this tour")
        }

        val currentUser = userRepository.findByEmail(guideEmail)

        // Fetch tags if provided
        val tags = fetchTags(request.tagIds)

        val occupiedSpots = tour.maxGroupSize - tour.availableSpots
        if (request.maxGroupSize < occupiedSpots) {
            throw APIException(
                ErrorCode.BAD_REQUEST,
                "Maximum group size cannot be less than the number of current bookings ($occupiedSpots)"
            )
        }

        TourMapper.updateEntity(tour, request, tags)

        if (image != null) {
            val imageUrl = cloudinaryService.uploadImage(
                image,
                Constants.Cloudinary.FOLDER_TOUR_IMAGES,
                "${Constants.Cloudinary.PREFIX_TOUR}${tour.id}"
            )
            tour.imageUrl = imageUrl
        }

        val updatedTour = tourRepository.save(tour)
        return TourMapper.toDto(updatedTour, currentUser)
    }

    @Transactional
    fun deleteTour(id: Long, guideEmail: String) {
        val tour = tourRepository.findById(id)
            .orElseThrow { APIException(ErrorCode.RESOURCE_NOT_FOUND, "Tour not found with id: $id") }

        if (tour.guide.email != guideEmail) {
            throw APIException(ErrorCode.FORBIDDEN, "You are not authorized to delete this tour")
        }

        // Find all bookings to cancel and notify
        val bookings = bookingRepository.findAllByTourIdAndStatus(tour.id, Constants.BookingStatus.CONFIRMED)
        bookings.forEach { booking ->
            booking.status = Constants.BookingStatus.CANCELLED
            notificationService.createNotification(
                user = booking.user,
                title = "Tour Cancelled",
                message = tour.title,
                type = Constants.NotificationType.TOUR_CANCELLED,
                relatedId = tour.id
            )
        }
        bookingRepository.saveAll(bookings)

        tour.status = Constants.TourStatus.DELETED
        tourRepository.save(tour)
    }

    @Transactional(readOnly = true)
    fun getTour(id: Long, currentUserEmail: String?): CreateTourResponseDto {
        val tour = tourRepository.findById(id)
            .filter { it.status != Constants.TourStatus.DELETED }
            .orElseThrow {
                APIException(ErrorCode.RESOURCE_NOT_FOUND, "Tour not found with id: $id")
            }
        val currentUser = currentUserEmail?.let { userRepository.findByEmail(it) }
        return TourMapper.toDto(tour, currentUser)
    }

    @Transactional
    fun toggleSavedTour(tourId: Long, userEmail: String): Boolean {
        val user = userRepository.findByEmail(userEmail)
            ?: throw APIException(ErrorCode.RESOURCE_NOT_FOUND, "User not found")
        val tour = tourRepository.findById(tourId)
            .orElseThrow { APIException(ErrorCode.RESOURCE_NOT_FOUND, "Tour not found") }

        val isSaved = if (user.savedTours.any { it.id == tourId }) {
            user.savedTours.remove(tour)
            false
        } else {
            user.savedTours.add(tour)
            true
        }
        userRepository.save(user)
        return isSaved
    }

    @Transactional(readOnly = true)
    fun getSavedTours(userEmail: String): List<CreateTourResponseDto> {
        val user = userRepository.findByEmail(userEmail)
            ?: throw APIException(ErrorCode.RESOURCE_NOT_FOUND, "User not found")
        return user.savedTours.map { TourMapper.toDto(it, user) }
    }

    private fun fetchTags(tagIds: List<Long>?): MutableSet<TagEntity> {
        if (tagIds.isNullOrEmpty()) return mutableSetOf()

        val foundTags = tagRepository.findAllById(tagIds)
        if (foundTags.size != tagIds.size) {
            throw APIException(ErrorCode.BAD_REQUEST, "One or more tag IDs are invalid")
        }
        return foundTags.toMutableSet()
    }
}