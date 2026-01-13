package com.tourly.core.api.dto.tour

data class CreateTourResponseDto(
    val id: Long,
    val tourGuideId: Long,
    val title: String,
    val description: String,
    val location: String,
    val duration: String,
    val pricePerPerson: Double,
    val scheduledDate: String?,
    val createdAt: String,
    val status: String
)