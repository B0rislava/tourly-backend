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
import com.tourly.core.mapper.TourMapper
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
    private val bookingRepository: BookingRepository
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
                "tour_images",
                "tour_${savedTour.id}"
            )
            savedTour.imageUrl = imageUrl
            tourRepository.save(savedTour)
        }

        return TourMapper.toDto(savedTour)
    }

    @Transactional(readOnly = true)
    fun getToursByGuide(guideEmail: String): List<CreateTourResponseDto> {
        val guide = userRepository.findByEmail(guideEmail)
            ?: throw APIException(
                errorCode = ErrorCode.RESOURCE_NOT_FOUND,
                description = "Guide not found with email: $guideEmail"
            )

        val guideId = guide.id
            ?: throw APIException(ErrorCode.INTERNAL_SERVER_ERROR, "Guide ID is null after DB fetch")

        return tourRepository.findAllByGuideIdOrderByCreatedAtDesc(guideId)
            .filter { it.status != "DELETED" }
            .map(TourMapper::toDto)
    }

    @Transactional(readOnly = true)
    fun getAllActiveTours(
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
            .map(TourMapper::toDto)
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
                "tour_images",
                "tour_${tour.id}"
            )
            tour.imageUrl = imageUrl
        }

        val updatedTour = tourRepository.save(tour)
        return TourMapper.toDto(updatedTour)
    }

    @Transactional
    fun deleteTour(id: Long, guideEmail: String) {
        val tour = tourRepository.findById(id)
            .orElseThrow { APIException(ErrorCode.RESOURCE_NOT_FOUND, "Tour not found with id: $id") }

        if (tour.guide.email != guideEmail) {
            throw APIException(ErrorCode.FORBIDDEN, "You are not authorized to delete this tour")
        }

        // Check for active bookings
        if (bookingRepository.existsByTourIdAndStatus(tour.id, "CONFIRMED")) {
            throw APIException(ErrorCode.BAD_REQUEST, "Cannot delete tour with active bookings.")
        }

        tour.status = "DELETED"
        tourRepository.save(tour)
    }

    @Transactional(readOnly = true)
    fun getTour(id: Long): CreateTourResponseDto {
        val tour = tourRepository.findById(id)
            .filter { it.status != "DELETED" }
            .orElseThrow {
                APIException(ErrorCode.RESOURCE_NOT_FOUND, "Tour not found with id: $id")
            }
        return TourMapper.toDto(tour)
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