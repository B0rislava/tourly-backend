package com.tourly.core.service

import com.tourly.core.api.dto.booking.BookTourRequestDto
import com.tourly.core.api.dto.booking.BookingResponseDto
import com.tourly.core.config.Constants
import com.tourly.core.data.entity.BookingEntity
import com.tourly.core.data.enumeration.UserRole
import com.tourly.core.data.repository.BookingRepository
import com.tourly.core.data.repository.ReviewRepository
import com.tourly.core.data.repository.TourRepository
import com.tourly.core.data.repository.UserRepository
import com.tourly.core.exception.APIException
import com.tourly.core.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
            type = "NEW_BOOKING",
            relatedId = tour.id
        )

        return mapToResponseDto(savedBooking)
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
            type = "BOOKING_CANCELLED_TRAVELER",
            relatedId = tour.id
        )

        // Notify the guide
        notificationService.createNotification(
            user = tour.guide,
            title = "Booking Cancelled",
            message = "${user.firstName} ${user.lastName}|${tour.title}",
            type = "BOOKING_CANCELLED_GUIDE",
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
