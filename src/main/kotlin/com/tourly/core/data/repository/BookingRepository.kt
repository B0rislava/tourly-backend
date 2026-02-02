package com.tourly.core.data.repository

import com.tourly.core.data.entity.BookingEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.stereotype.Repository

@Repository
interface BookingRepository : JpaRepository<BookingEntity, Long> {
    @EntityGraph(attributePaths = ["tour", "tour.guide", "tour.tags"])
    fun findAllByUserIdOrderByBookingDateDesc(userId: Long): List<BookingEntity>

    @EntityGraph(attributePaths = ["tour", "tour.guide", "user"])
    fun findAllByTourGuideEmailOrderByBookingDateDesc(email: String): List<BookingEntity>
    
    fun findAllByTourIdAndStatus(tourId: Long, status: String): List<BookingEntity>
    
    fun existsByUserIdAndTourId(userId: Long, tourId: Long): Boolean
    fun existsByUserIdAndTourIdAndStatus(userId: Long, tourId: Long, status: String): Boolean

    fun existsByTourIdAndStatus(tourId: Long, status: String): Boolean

    fun deleteAllByUserId(userId: Long)
    
    fun deleteAllByTourGuideId(guideId: Long)
}
