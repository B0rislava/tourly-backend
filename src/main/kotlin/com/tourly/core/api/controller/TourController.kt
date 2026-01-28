package com.tourly.core.api.controller

import com.tourly.core.api.dto.tour.CreateTourRequestDto
import com.tourly.core.api.dto.tour.CreateTourResponseDto
import com.tourly.core.service.TourService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Tours", description = "Endpoints for managing tours")
@RestController
@RequestMapping("/api/tours")
class TourController(
    private val tourService: TourService
) {
    @Operation(
        summary = "Create a new tour",
        description = "Allows a guide to create a new tour with an optional image"
    )
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

    @Operation(summary = "Get my tours", description = "Fetches all tours created by the currently authenticated guide")
    @GetMapping("/my")
    @PreAuthorize("hasRole('GUIDE')")
    fun getMyTours(authentication: Authentication): ResponseEntity<List<CreateTourResponseDto>> {
        val email = authentication.name
        val tours = tourService.getToursByGuide(email)
        return ResponseEntity.ok(tours)
    }

    @Operation(summary = "Search tours", description = "Fetches all active tours based on various filter criteria")
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

    @Operation(summary = "Get tour by ID", description = "Fetches details of a specific tour")
    @GetMapping("/{id}")
    fun getTour(@PathVariable id: Long): ResponseEntity<CreateTourResponseDto> {
        return ResponseEntity.ok(tourService.getTour(id))
    }

    @Operation(summary = "Update tour", description = "Allows a guide to update an existing tour")
    @PostMapping(value = ["/{id}"], consumes = ["multipart/form-data"])
    @PreAuthorize("hasRole('GUIDE')")
    fun updateTour(
        authentication: Authentication,
        @PathVariable id: Long,
        @Valid @RequestPart("data") request: CreateTourRequestDto,
        @RequestPart("image", required = false) image: MultipartFile?
    ): ResponseEntity<CreateTourResponseDto> {
        val email = authentication.name
        val response = tourService.updateTour(id, email, request, image)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "Delete tour", description = "Allows a guide to delete one of their tours")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GUIDE')")
    fun deleteTour(
        authentication: Authentication,
        @PathVariable id: Long
    ): ResponseEntity<Unit> {
        val email = authentication.name
        tourService.deleteTour(id, email)
        return ResponseEntity.noContent().build()
    }
}