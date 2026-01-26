package com.tourly.core.api.dto.booking

data class BookingResponseDto(
    val id: Long,
    val tourId: Long,
    val tourTitle: String,
    val tourLocation: String,
    val tourImageUrl: String?,
    val tourScheduledDate: String?,
    val numberOfParticipants: Int,
    val bookingDate: String,
    val status: String,
    val pricePerPerson: Double,
    val totalPrice: Double
)
