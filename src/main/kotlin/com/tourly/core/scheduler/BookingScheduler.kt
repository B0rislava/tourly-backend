package com.tourly.core.scheduler

import com.tourly.core.config.Constants
import com.tourly.core.data.repository.BookingRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Component
class BookingScheduler(
    private val bookingRepository: BookingRepository
) {
    private val logger = LoggerFactory.getLogger(BookingScheduler::class.java)
    private val durationFormatter = DateTimeFormatter.ofPattern("HH:mm")

    @Scheduled(fixedRate = 1800000) // Every 30 minutes
    @Transactional
    fun completePastBookings() {
        logger.info("Starting scheduled task: completePastBookings")
        val now = LocalDateTime.now()
        val confirmedBookings = bookingRepository.findAllByStatus(Constants.BookingStatus.CONFIRMED)

        var completedCount = 0
        confirmedBookings.forEach { booking ->
            val tour = booking.tour
            val date = tour.scheduledDate
            val startTime = tour.startTime

            if (date != null && startTime != null) {
                var tourEndDateTime = LocalDateTime.of(date, startTime)
                
                // Parse duration and add to start time
                try {
                    val durationTime = LocalTime.parse(tour.duration, durationFormatter)
                    tourEndDateTime = tourEndDateTime
                        .plusHours(durationTime.hour.toLong())
                        .plusMinutes(durationTime.minute.toLong())
                } catch (e: Exception) {
                    // Fallback if duration format is unexpected: assume 2 hours
                    tourEndDateTime = tourEndDateTime.plusHours(2)
                }

                if (now.isAfter(tourEndDateTime)) {
                    booking.status = Constants.BookingStatus.COMPLETED
                    bookingRepository.save(booking)
                    completedCount++
                }
            }
        }
        
        if (completedCount > 0) {
            logger.info("Completed $completedCount bookings")
        }
    }
}
