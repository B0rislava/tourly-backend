package com.tourly.core.api.controller

import com.tourly.core.api.dto.CreateReviewRequest
import com.tourly.core.api.dto.ReviewDto
import com.tourly.core.data.mapper.ReviewMapper
import com.tourly.core.security.CustomUserDetails
import com.tourly.core.service.ReviewService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Reviews", description = "Endpoints for managing tour reviews")
@RestController
@RequestMapping("/api/reviews")
class ReviewController(
    private val reviewService: ReviewService
) {

    @Operation(summary = "Create review", description = "Allows a traveler to leave a review for a completed booking")
    @PostMapping
    fun createReview(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestBody request: CreateReviewRequest
    ): ResponseEntity<ReviewDto> {
        val review = reviewService.createReview(
            bookingId = request.bookingId,
            reviewerId = userDetails.getUserId(),
            tourRating = request.tourRating,
            guideRating = request.guideRating,
            comment = request.comment
        )

        return ResponseEntity.ok(ReviewMapper.toDto(review))
    }

    @Operation(summary = "Get reviews for tour", description = "Fetches all reviews for a specific tour")
    @GetMapping("/tours/{tourId}")
    fun getReviewsForTour(@PathVariable tourId: Long): ResponseEntity<List<ReviewDto>> {
        val reviews = reviewService.getReviewsForTour(tourId)
        val reviewDtos = reviews.map { ReviewMapper.toDto(it) }
        return ResponseEntity.ok(reviewDtos)
    }

    @Operation(summary = "Get reviews for guide", description = "Fetches all reviews for a specific guide")
    @GetMapping("/guides/{guideId}")
    fun getReviewsForGuide(@PathVariable guideId: Long): ResponseEntity<List<ReviewDto>> {
        val reviews = reviewService.getReviewsForGuide(guideId)
        val reviewDtos = reviews.map { ReviewMapper.toDto(it) }
        return ResponseEntity.ok(reviewDtos)
    }

    @Operation(summary = "Get my reviews", description = "Fetches all reviews for the currently authenticated guide")
    @GetMapping("/my")
    fun getMyReviews(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<List<ReviewDto>> {
        val reviews = reviewService.getReviewsForGuide(userDetails.getUserId())
        val reviewDtos = reviews.map { ReviewMapper.toDto(it) }
        return ResponseEntity.ok(reviewDtos)
    }
}
