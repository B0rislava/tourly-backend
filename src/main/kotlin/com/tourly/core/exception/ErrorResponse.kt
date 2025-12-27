package com.tourly.core.exception

import java.time.LocalDateTime

data class ErrorResponse(
    val code: String,
    val message: String,
    val description: String? = null,
    val errors: Map<String, String>? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
