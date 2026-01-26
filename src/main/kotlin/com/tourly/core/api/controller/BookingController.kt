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

@RestController
@RequestMapping("/api/bookings")
class BookingController(
    private val bookingService: BookingService
) {
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

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    fun getMyBookings(authentication: Authentication): ResponseEntity<List<BookingResponseDto>> {
        val email = authentication.name
        val bookings = bookingService.getUserBookings(email)
        return ResponseEntity.ok(bookings)
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('TRAVELER')")
    fun cancelBooking(
        authentication: Authentication,
        @org.springframework.web.bind.annotation.PathVariable id: Long
    ): ResponseEntity<Unit> {
        val email = authentication.name
        bookingService.cancelBooking(email, id)
        return ResponseEntity.ok().build()
    }
}
