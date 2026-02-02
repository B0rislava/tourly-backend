package com.tourly.core.api.controller

import com.tourly.core.api.dto.booking.BookTourRequestDto
import com.tourly.core.api.dto.booking.BookingResponseDto
import com.tourly.core.service.BookingService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "Bookings", description = "Endpoints for managing tour bookings")
@RestController
@RequestMapping("/api/bookings")
class BookingController(
    private val bookingService: BookingService
) {
    @Operation(summary = "Book a tour", description = "Allows a traveler to book a specific tour")
    @PostMapping
    @PreAuthorize("hasRole('TRAVELER')")
    fun bookTour(
        authentication: Authentication,
        @Valid @RequestBody request: BookTourRequestDto
    ): ResponseEntity<BookingResponseDto> {
        val email = authentication.name
        val response = bookingService.bookTour(email, request)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "Get my bookings", description = "Fetches all bookings made by the currently authenticated user")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    fun getMyBookings(authentication: Authentication): ResponseEntity<List<BookingResponseDto>> {
        val email = authentication.name
        val bookings = bookingService.getUserBookings(email)
        return ResponseEntity.ok(bookings)
    }

    @Operation(summary = "Cancel booking", description = "Allows a traveler to cancel one of their existing bookings")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('TRAVELER')")
    fun cancelBooking(
        authentication: Authentication,
        @PathVariable id: Long
    ): ResponseEntity<Unit> {
        val email = authentication.name
        bookingService.cancelBooking(email, id)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "Complete booking (Dev Tool)", description = "Forces a booking status to COMPLETED for testing purposes")
    @PostMapping("/{id}/complete")
    @PreAuthorize("isAuthenticated()")
    fun completeBooking(@PathVariable id: Long): ResponseEntity<Unit> {
        bookingService.completeBooking(id)
        return ResponseEntity.ok().build()
    }
}
