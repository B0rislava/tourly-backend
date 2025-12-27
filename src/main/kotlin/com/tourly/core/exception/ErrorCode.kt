package com.tourly.core.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val code: String,
    val message: String,
    val httpStatus: HttpStatus
) {
    INTERNAL_SERVER_ERROR("TY-0", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR("TY-1", "Validation Error", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("TY-2", "Resource Not Found", HttpStatus.NOT_FOUND),
    BAD_REQUEST("TY-3", "Bad Request", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("TY-4", "Unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("TY-5", "Forbidden", HttpStatus.FORBIDDEN)
}
