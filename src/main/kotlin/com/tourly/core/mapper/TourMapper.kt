package com.tourly.core.mapper

import com.tourly.core.api.dto.tour.CreateTourResponseDto
import com.tourly.core.data.entity.TourEntity
import java.time.format.DateTimeFormatter

object TourMapper {
    fun toDto(tour: TourEntity): CreateTourResponseDto =
        CreateTourResponseDto(
            id = tour.id,
            tourGuideId = tour.guide.id!!,
            guideName = "${tour.guide.firstName} ${tour.guide.lastName}",
            title = tour.title,
            description = tour.description,
            location = tour.location,
            duration = tour.duration,
            maxGroupSize = tour.maxGroupSize,
            pricePerPerson = tour.pricePerPerson,
            scheduledDate = tour.scheduledDate?.toString(),
            createdAt = tour.createdAt.format(DateTimeFormatter.ISO_DATE_TIME),
            status = tour.status,
            rating = tour.rating,
            reviewsCount = tour.reviewsCount,
            meetingPoint = tour.meetingPoint,
            imageUrl = tour.imageUrl,
            cancellationPolicy = tour.cancellationPolicy,
            whatsIncluded = tour.whatsIncluded,
            guideBio = tour.guide.bio,
            guideRating = tour.guide.rating,
            guideToursCompleted = tour.guide.toursCompleted,
            guideImageUrl = tour.guide.profilePictureUrl
        )

}