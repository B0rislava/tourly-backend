package com.tourly.core.data.mapper

import com.tourly.core.api.dto.ReviewDto
import com.tourly.core.data.entity.ReviewEntity
import org.springframework.stereotype.Component

@Component
object ReviewMapper {
    fun toDto(review: ReviewEntity): ReviewDto {
        return ReviewDto(
            id = review.id,
            reviewerName = "${review.reviewer.firstName} ${review.reviewer.lastName}",
            reviewerProfilePicture = review.reviewer.profilePictureUrl,
            tourRating = review.tourRating,
            guideRating = review.guideRating,
            comment = review.comment,
            createdAt = review.createdAt,
            tourTitle = review.tour.title
        )
    }
}
