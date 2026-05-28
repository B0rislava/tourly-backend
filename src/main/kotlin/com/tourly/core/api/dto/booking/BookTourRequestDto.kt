package com.tourly.core.api.dto.booking

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class BookTourRequestDto(
    @field:NotNull(message = "Tour ID is required")
    var tourId: Long,

    @field:Min(value = 1, message = "Number of participants must be at least 1")
    val numberOfParticipants: Int = 1
)
