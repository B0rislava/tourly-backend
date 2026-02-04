package com.tourly.core.service

import com.tourly.core.config.Constants
import com.tourly.core.data.entity.ReviewEntity
import com.tourly.core.data.repository.BookingRepository
import com.tourly.core.data.repository.ReviewRepository
import com.tourly.core.data.repository.TourRepository
import com.tourly.core.data.repository.UserRepository
import com.tourly.core.exception.APIException
import com.tourly.core.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val bookingRepository: BookingRepository,
    private val tourRepository: TourRepository,
    private val userRepository: UserRepository,
) {

    @Transactional
    fun createReview(
        bookingId: Long,
        reviewerId: Long,
        tourRating: Int,
        guideRating: Int,
        comment: String?
    ): ReviewEntity {
        // 1. Validation
        val booking = bookingRepository.findById(bookingId)
            .orElseThrow { APIException(ErrorCode.RESOURCE_NOT_FOUND, "Booking not found") }

        if (booking.user.id != reviewerId) {
            throw APIException(ErrorCode.FORBIDDEN, "You can only review your own bookings")
        }

        if (booking.status != Constants.BookingStatus.COMPLETED) {
            throw APIException(ErrorCode.BAD_REQUEST, "You can only review completed bookings")
        }

        if (reviewRepository.existsByBookingId(bookingId)) {
            throw APIException(ErrorCode.CONFLICT, "You have already reviewed this booking")
        }

        // 2. Create Review Entity
        val review = ReviewEntity(
            reviewer = booking.user,
            guide = booking.tour.guide,
            tour = booking.tour,
            bookingId = bookingId,
            tourRating = tourRating,
            guideRating = guideRating,
            comment = comment
        )

        val savedReview = reviewRepository.save(review)

        // 3. Update Stats
        updateGuideStats(booking.tour.guide.id!!)
        updateTourStats(booking.tour.id)

        return savedReview
    }

    private fun updateGuideStats(guideId: Long) {
        val guideReviews = reviewRepository.findByGuideId(guideId)
        val count = guideReviews.size
        val avg = if (count > 0) guideReviews.map { it.guideRating }.average() else 0.0
        
        val guide = userRepository.findById(guideId).orElseThrow { 
            APIException(ErrorCode.RESOURCE_NOT_FOUND, "Guide not found") 
        }
        guide.rating = avg
        guide.reviewsCount = count
        userRepository.save(guide)
    }

    private fun updateTourStats(tourId: Long) {
        val tourReviews = reviewRepository.findByTourId(tourId)
        val count = tourReviews.size
        val avg = if (count > 0) tourReviews.map { it.tourRating }.average() else 0.0

        val tour = tourRepository.findById(tourId).orElseThrow { 
            APIException(ErrorCode.RESOURCE_NOT_FOUND, "Tour not found") 
        }
        tour.rating = avg
        tour.reviewsCount = count
        tourRepository.save(tour)
    }
    
    fun getReviewsForTour(tourId: Long): List<ReviewEntity> {
        return reviewRepository.findByTourId(tourId)
    }

    fun getReviewsForGuide(guideId: Long): List<ReviewEntity> {
        return reviewRepository.findByGuideId(guideId)
    }
}
