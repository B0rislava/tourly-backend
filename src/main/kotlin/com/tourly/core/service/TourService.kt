package com.tourly.core.service

import com.tourly.core.api.dto.tour.CreateTourRequestDto
import com.tourly.core.api.dto.tour.CreateTourResponseDto
import com.tourly.core.data.entity.TourEntity
import com.tourly.core.data.enumeration.UserRole
import com.tourly.core.data.repository.TourRepository
import com.tourly.core.data.repository.UserRepository
import com.tourly.core.exception.APIException
import com.tourly.core.exception.ErrorCode
import com.tourly.core.mapper.TourMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class TourService(
    private val tourRepository: TourRepository,
    private val userRepository: UserRepository,
    private val cloudinaryService: CloudinaryService
) {

    @Transactional
    fun createTour(guideEmail: String, request: CreateTourRequestDto, image: MultipartFile?): CreateTourResponseDto {
        val guide = userRepository.findByEmail(guideEmail)
            ?: throw APIException(
                errorCode = ErrorCode.RESOURCE_NOT_FOUND,
                description = "Guide not found with email: $guideEmail"
            )

        if (guide.role != UserRole.GUIDE) throw APIException(ErrorCode.FORBIDDEN, "User is not a guide")

        // First, create and save the tour without image to get the ID
        val tour = TourEntity(
            guide = guide,
            title = request.title,
            description = request.description,
            location = request.location,
            duration = request.duration,
            maxGroupSize = request.maxGroupSize,
            pricePerPerson = request.pricePerPerson,
            whatsIncluded = request.whatsIncluded ?: "",
            scheduledDate = request.scheduledDate,
            imageUrl = null
        )

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
            .map(TourMapper::toDto)
    }

    @Transactional(readOnly = true)
    fun getAllActiveTours(): List<CreateTourResponseDto> {
        return tourRepository.findAllByStatusOrderByCreatedAtDesc("ACTIVE")
            .map(TourMapper::toDto)
    }
    @Transactional(readOnly = true)
    fun getTour(id: Long): CreateTourResponseDto {
        val tour = tourRepository.findById(id)
            .orElseThrow {
                APIException(ErrorCode.RESOURCE_NOT_FOUND, "Tour not found with id: $id")
            }
        return TourMapper.toDto(tour)
    }
}
