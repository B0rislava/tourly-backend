package com.tourly.core.api.dto.tour

import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import java.time.LocalDate
import java.time.LocalTime

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CreateTourRequestDto(
    @field:NotBlank(message = "Title is required")
    val title: String,
    
    @field:NotBlank(message = "Description is required")
    val description: String,
    
    @field:NotBlank(message = "Location is required")
    val location: String,

    @field:NotBlank(message = "Duration is required")
    @field:Pattern(
        regexp = "^([01]\\d|2[0-3]):[0-5]\\d$",
        message = "Duration must be in HH:mm format"
    )
    val duration: String,
    
    @field:Min(value = 1, message = "Max group size must be at least 1")
    val maxGroupSize: Int,
    
    @field:Positive(message = "Price must be positive")
    val pricePerPerson: Double,

    val whatsIncluded: String?,
    
    @field:FutureOrPresent(message = "Scheduled date must be in the present or future")
    val scheduledDate: LocalDate?,

    val startTime: LocalTime?,

    val tagIds: List<Long>? = null,

    val latitude: Double? = null,
    val longitude: Double? = null,
    val meetingPoint: String? = null
)