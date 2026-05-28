package com.tourly.core.api.dto

import java.time.LocalDateTime

data class ReviewDto(
    val id: Long,
    val reviewerName: String,
    val reviewerProfilePicture: String?,
    val tourRating: Int,
    val guideRating: Int,
    val comment: String?,
    val createdAt: LocalDateTime,
    val tourTitle: String? = null
)
