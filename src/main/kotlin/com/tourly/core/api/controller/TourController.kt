package com.tourly.core.api.controller

import com.tourly.core.api.dto.tour.CreateTourRequestDto
import com.tourly.core.api.dto.tour.CreateTourResponseDto
import com.tourly.core.service.TourService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

@RestController
@RequestMapping("/api/tours")
class TourController(
    private val tourService: TourService
) {
    @PostMapping(consumes = ["multipart/form-data"])
    @PreAuthorize("hasRole('GUIDE')")
    fun createTour(
        authentication: Authentication,
        @Valid @RequestPart("data") request: CreateTourRequestDto,
        @RequestPart("image", required = false) image: MultipartFile?
    ): ResponseEntity<CreateTourResponseDto> {
        val email = authentication.name
        val response = tourService.createTour(email, request, image)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('GUIDE')")
    fun getMyTours(authentication: Authentication): ResponseEntity<List<CreateTourResponseDto>> {
        val email = authentication.name
        val tours = tourService.getToursByGuide(email)
        return ResponseEntity.ok(tours)
    }

    @GetMapping
    fun getAllTours(
        @RequestParam(required = false) location: String?,
        @RequestParam(required = false) tags: List<String>?,
        @RequestParam(required = false) minPrice: Double?,
        @RequestParam(required = false) maxPrice: Double?,
        @RequestParam(required = false) minRating: Double?,
        @RequestParam(required = false) scheduledAfter: LocalDate?,
        @RequestParam(required = false) scheduledBefore: LocalDate?,
        @RequestParam(required = false) maxGroupSize: Int?,
        @RequestParam(required = false) sortBy: String?,
        @RequestParam(required = false) sortOrder: String?
    ): ResponseEntity<List<CreateTourResponseDto>> {
        val tours = tourService.getAllActiveTours(
            location = location,
            tags = tags,
            minPrice = minPrice,
            maxPrice = maxPrice,
            minRating = minRating,
            scheduledAfter = scheduledAfter,
            scheduledBefore = scheduledBefore,
            maxGroupSize = maxGroupSize,
            sortBy = sortBy,
            sortOrder = sortOrder
        )
        return ResponseEntity.ok(tours)
    }

    @GetMapping("/{id}")
    fun getTour(@PathVariable id: Long): ResponseEntity<CreateTourResponseDto> {
        return ResponseEntity.ok(tourService.getTour(id))
    }
}