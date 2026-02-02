package com.tourly.core.data.repository

import com.tourly.core.data.entity.ReviewEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReviewRepository : JpaRepository<ReviewEntity, Long> {
    fun findByTourId(tourId: Long): List<ReviewEntity>
    fun findByGuideId(guideId: Long): List<ReviewEntity>
    fun existsByBookingId(bookingId: Long): Boolean
}
