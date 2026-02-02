package com.tourly.core.api.dto

data class CreateReviewRequest(
    val bookingId: Long,
    val tourRating: Int,
    val guideRating: Int,
    val comment: String?
)
