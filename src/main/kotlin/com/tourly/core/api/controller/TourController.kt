package com.tourly.core.api.controller

import com.tourly.core.api.dto.tour.CreateTourRequestDto
import com.tourly.core.api.dto.tour.CreateTourResponseDto
import com.tourly.core.service.TourService
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
@RequestMapping("/api/tours")
class TourController(
    private val tourService: TourService
) {

    @PostMapping
    @PreAuthorize("hasRole('GUIDE')")
    fun createTour(
        authentication: Authentication,
        @Valid @RequestBody request: CreateTourRequestDto
    ): ResponseEntity<CreateTourResponseDto> {
        val email = authentication.name
        val response = tourService.createTour(email, request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('GUIDE')")
    fun getMyTours(authentication: Authentication): ResponseEntity<List<CreateTourResponseDto>> {
        val email = authentication.name
        val tours = tourService.getToursByGuide(email)
        return ResponseEntity.ok(tours)
    }
}