package com.tourly.core.service

import com.tourly.core.api.dto.booking.BookTourRequestDto
import com.tourly.core.api.dto.booking.BookingResponseDto
import com.tourly.core.config.Constants
import com.tourly.core.data.entity.BookingEntity
import com.tourly.core.data.entity.TourEntity
import com.tourly.core.data.enumeration.UserRole
import com.tourly.core.data.repository.BookingRepository
import com.tourly.core.data.repository.ReviewRepository
import com.tourly.core.data.repository.TourRepository
import com.tourly.core.data.repository.UserRepository
import com.tourly.core.exception.APIException
import com.tourly.core.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class BookingService(
    private val bookingRepository: BookingRepository,
    private val tourRepository: TourRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService,
    private val reviewRepository: ReviewRepository
) {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    @Transactional
    fun bookTour(userEmail: String, request: BookTourRequestDto): BookingResponseDto {
        val user = userRepository.findByEmail(userEmail)
            ?: throw APIException(ErrorCode.RESOURCE_NOT_FOUND, "User not found")

        // Validate user is a traveler
        if (user.role != UserRole.TRAVELER) {
            throw APIException(ErrorCode.FORBIDDEN, "Only travelers can book tours")
        }

        // Check for double booking
        if (bookingRepository.existsByUserIdAndTourIdAndStatus(user.id!!, request.tourId, Constants.BookingStatus.CONFIRMED)) {
            throw APIException(ErrorCode.CONFLICT, "You've already booked this tour")
        }

        // Find tour
        val tour = tourRepository.findById(request.tourId).orElseThrow {
            APIException(ErrorCode.RESOURCE_NOT_FOUND, "Tour not found")
        }

        // Validate tour is active
        if (tour.status != "ACTIVE") {
            throw APIException(ErrorCode.BAD_REQUEST, "Tour is not available for booking")
        }

        // Validate tour has valid date/time for booking
        if (tour.scheduledDate == null || tour.startTime == null) {
             throw APIException(ErrorCode.BAD_REQUEST, "Tour does not have a scheduled date or time")
        }

        val userId = user.id ?: throw APIException(ErrorCode.INTERNAL_SERVER_ERROR, "User ID is missing")

        // Check for time overlap with existing bookings
        val userBookings = bookingRepository.findAllByUserIdOrderByBookingDateDesc(userId)
        val conflictingBooking = userBookings.find { existingBooking ->
            existingBooking.status == Constants.BookingStatus.CONFIRMED &&
            existingBooking.tour.id != tour.id &&
            doToursOverlap(tour, existingBooking.tour)
        }

        if (conflictingBooking != null) {
            throw APIException(
                ErrorCode.CONFLICT, 
                "This tour overlaps with your existing booking: '${conflictingBooking.tour.title}'"
            )
        }

        // Check if enough spots are available
        if (tour.availableSpots <= 0) {
            throw APIException(ErrorCode.BAD_REQUEST, "No available spots for this tour")
        }

        if (request.numberOfParticipants > tour.availableSpots) {
            throw APIException(
                ErrorCode.BAD_REQUEST,
                "Requested participants (${request.numberOfParticipants}) exceed available spots (${tour.availableSpots})"
            )
        }

        // Decrease available spots
        tour.availableSpots -= request.numberOfParticipants
        tourRepository.save(tour)

        val booking = BookingEntity(
            user = user,
            tour = tour,
            numberOfParticipants = request.numberOfParticipants
        )
        val savedBooking = bookingRepository.save(booking)

        // Notify the guide
        notificationService.createNotification(
            user = tour.guide,
            title = "New Booking",
            message = "${user.firstName} ${user.lastName}|${request.numberOfParticipants}|${tour.title}",
            type = Constants.NotificationType.NEW_BOOKING,
            relatedId = tour.id
        )

        return mapToResponseDto(savedBooking)
    }

    private fun doToursOverlap(tour1: TourEntity, tour2: TourEntity): Boolean {
        if (tour1.scheduledDate == null || tour1.startTime == null ||
            tour2.scheduledDate == null || tour2.startTime == null) {
            return false
        }

        val start1 = LocalDateTime.of(tour1.scheduledDate, tour1.startTime)
        val end1 = calculateEndDateTime(start1, tour1.duration)
        
        val start2 = LocalDateTime.of(tour2.scheduledDate, tour2.startTime)
        val end2 = calculateEndDateTime(start2, tour2.duration)

        // Overlap logic: Start1 < End2 && Start2 < End1
        return start1.isBefore(end2) && start2.isBefore(end1)
    }

    private fun calculateEndDateTime(startDateTime: LocalDateTime, duration: String): LocalDateTime {
        // Duration format expected: HH:mm
        return try {
            val parts = duration.split(":")
            val hours = parts[0].toLong()
            val minutes = parts[1].toLong()
            startDateTime.plusHours(hours).plusMinutes(minutes)
        } catch (e: Exception) {
             startDateTime.plusHours(1)
        }
    }

    @Transactional(readOnly = true)
    fun getUserBookings(userEmail: String): List<BookingResponseDto> {
        val user = userRepository.findByEmail(userEmail)
            ?: throw APIException(ErrorCode.RESOURCE_NOT_FOUND, "User not found")

        val bookings = bookingRepository.findAllByUserIdOrderByBookingDateDesc(user.id!!)
        return bookings
            .filter { it.tour.status != "DELETED" }
            .map { mapToResponseDto(it) }
    }

    @Transactional(readOnly = true)
    fun getGuideBookings(guideEmail: String): List<BookingResponseDto> {
        val bookings = bookingRepository.findAllByTourGuideEmailOrderByBookingDateDesc(guideEmail)
        return bookings
            .filter { it.tour.status != "DELETED" }
            .map { mapToResponseDto(it) }
    }

    @Transactional
    fun cancelBooking(userEmail: String, bookingId: Long) {
        val user = userRepository.findByEmail(userEmail)
            ?: throw APIException(ErrorCode.RESOURCE_NOT_FOUND, "User not found")

        val booking = bookingRepository.findById(bookingId).orElseThrow {
            APIException(ErrorCode.RESOURCE_NOT_FOUND, "Booking not found")
        }

        if (booking.user.id != user.id) {
            throw APIException(ErrorCode.FORBIDDEN, "You can only cancel your own bookings")
        }

        if (booking.status == Constants.BookingStatus.CANCELLED) {
            throw APIException(ErrorCode.BAD_REQUEST, "Booking is already cancelled")
        }

        // Return spots to the tour
        val tour = booking.tour
        tour.availableSpots += booking.numberOfParticipants
        tourRepository.save(tour)

        // Update booking status
        booking.status = Constants.BookingStatus.CANCELLED
        bookingRepository.save(booking)

        // Notify the user (traveler)
        notificationService.createNotification(
            user = booking.user,
            title = "Booking Cancelled",
            message = tour.title,
            type = Constants.NotificationType.BOOKING_CANCELLED_TRAVELER,
            relatedId = tour.id
        )

        // Notify the guide
        notificationService.createNotification(
            user = tour.guide,
            title = "Booking Cancelled",
            message = "${user.firstName} ${user.lastName}|${tour.title}",
            type = Constants.NotificationType.BOOKING_CANCELLED_GUIDE,
            relatedId = tour.id
        )
    }

    @Transactional
    fun completeBooking(bookingId: Long) {
        val booking = bookingRepository.findById(bookingId).orElseThrow {
            APIException(ErrorCode.RESOURCE_NOT_FOUND, "Booking not found")
        }
        booking.status = Constants.BookingStatus.COMPLETED
        bookingRepository.save(booking)
    }

    private fun mapToResponseDto(booking: BookingEntity): BookingResponseDto {
        val tour = booking.tour
        val totalPrice = tour.pricePerPerson * booking.numberOfParticipants

        return BookingResponseDto(
            id = booking.id,
            tourId = tour.id,
            tourTitle = tour.title,
            tourLocation = tour.location,
            tourImageUrl = tour.imageUrl,
            tourScheduledDate = tour.scheduledDate?.format(dateFormatter),
            numberOfParticipants = booking.numberOfParticipants,
            bookingDate = booking.bookingDate.format(dateTimeFormatter),
            status = booking.status,
            pricePerPerson = tour.pricePerPerson,
            totalPrice = totalPrice,
            hasReview = reviewRepository.existsByBookingId(booking.id),
            customerName = "${booking.user.firstName} ${booking.user.lastName}",
            customerImageUrl = booking.user.profilePictureUrl
        )
    }
}
