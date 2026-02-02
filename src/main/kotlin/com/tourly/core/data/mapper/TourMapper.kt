package com.tourly.core.data.mapper

import com.tourly.core.api.dto.tour.CreateTourRequestDto
import com.tourly.core.api.dto.tour.CreateTourResponseDto
import com.tourly.core.data.entity.TagEntity
import com.tourly.core.data.entity.TourEntity
import com.tourly.core.data.entity.UserEntity
import java.time.format.DateTimeFormatter

object TourMapper {
    fun toDto(tour: TourEntity, currentUser: UserEntity? = null): CreateTourResponseDto =
        CreateTourResponseDto(
            id = tour.id,
            tourGuideId = tour.guide.id!!,
            guideName = "${tour.guide.firstName} ${tour.guide.lastName}",
            title = tour.title,
            description = tour.description,
            location = tour.location,
            duration = tour.duration,
            maxGroupSize = tour.maxGroupSize,
            availableSpots = tour.availableSpots,
            pricePerPerson = tour.pricePerPerson,
            scheduledDate = tour.scheduledDate?.toString(),
            startTime = tour.startTime?.toString(),
            createdAt = tour.createdAt.format(DateTimeFormatter.ISO_DATE_TIME),
            status = tour.status,
            rating = tour.rating ?: 0.0,
            reviewsCount = tour.reviewsCount ?: 0,
            meetingPoint = tour.meetingPoint,
            imageUrl = tour.imageUrl,
            whatsIncluded = tour.whatsIncluded,
            guideBio = tour.guide.bio,
            guideRating = tour.guide.rating ?: 0.0,
            guideImageUrl = tour.guide.profilePictureUrl,
            latitude = tour.latitude,
            longitude = tour.longitude,
            tags = tour.tags.map { TagMapper.toDto(it) },
            isSaved = currentUser?.savedTours?.any { it.id == tour.id } ?: false
        )
    fun toEntity(guide: UserEntity, request: CreateTourRequestDto, tags: Set<TagEntity>): TourEntity =
        TourEntity(
            guide = guide,
            title = request.title,
            description = request.description,
            location = request.location,
            duration = request.duration,
            maxGroupSize = request.maxGroupSize,
            availableSpots = request.maxGroupSize,
            pricePerPerson = request.pricePerPerson,
            whatsIncluded = request.whatsIncluded ?: "",
            scheduledDate = request.scheduledDate,
            startTime = request.startTime,
            latitude = request.latitude,
            longitude = request.longitude,
            meetingPoint = request.meetingPoint,
            imageUrl = null,
            tags = tags.toMutableSet()
        )

    fun updateEntity(tour: TourEntity, request: CreateTourRequestDto, tags: Set<TagEntity>) {
        val occupiedSpots = tour.maxGroupSize - tour.availableSpots
        
        tour.title = request.title
        tour.description = request.description
        tour.location = request.location
        tour.duration = request.duration
        tour.maxGroupSize = request.maxGroupSize
        tour.availableSpots = request.maxGroupSize - occupiedSpots
        tour.pricePerPerson = request.pricePerPerson
        tour.whatsIncluded = request.whatsIncluded ?: ""
        tour.scheduledDate = request.scheduledDate
        tour.startTime = request.startTime
        tour.latitude = request.latitude
        tour.longitude = request.longitude
        tour.meetingPoint = request.meetingPoint
        tour.tags = tags.toMutableSet()
    }

}