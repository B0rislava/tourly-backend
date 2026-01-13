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

@Service
class TourService(
    private val tourRepository: TourRepository,
    private val userRepository: UserRepository,
) {

    @Transactional
    fun createTour(guideEmail: String, request: CreateTourRequestDto): CreateTourResponseDto {
        val guide = userRepository.findByEmail(guideEmail)
            ?: throw APIException(
                errorCode = ErrorCode.RESOURCE_NOT_FOUND,
                description = "Guide not found with email: $guideEmail"
            )

        if (guide.role != UserRole.GUIDE) throw APIException(ErrorCode.FORBIDDEN, "User is not a guide")

        val tour = TourEntity(
            guide = guide,
            title = request.title,
            description = request.description,
            location = request.location,
            duration = request.duration,
            maxGroupSize = request.maxGroupSize,
            pricePerPerson = request.pricePerPerson,
            whatsIncluded = request.whatsIncluded,
            scheduledDate = request.scheduledDate
        )

        val savedTour = tourRepository.save(tour)

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
}
