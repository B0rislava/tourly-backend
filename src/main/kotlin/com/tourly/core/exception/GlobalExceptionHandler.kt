package com.tourly.core.exception

import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleAuthorizationDenied(ex: AuthorizationDeniedException): ResponseEntity<ErrorResponse> {
        val errorCode = ErrorCode.FORBIDDEN
        val response = ErrorResponse(
            code = errorCode.code,
            message = errorCode.message,
            description = "You don't have permission to perform this action. Check your user role."
        )
        return ResponseEntity.status(errorCode.httpStatus).body(response)
    }

    @ExceptionHandler(APIException::class)
    fun handleAPIException(ex: APIException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            code = ex.errorCode.code,
            message = ex.errorCode.message,
            description = ex.description
        )
        return ResponseEntity.status(ex.errorCode.httpStatus).body(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.associate {
            (it.field) to (it.defaultMessage ?: "Invalid value")
        }
        val errorCode = ErrorCode.VALIDATION_ERROR
        val response = ErrorResponse(
            code = errorCode.code,
            message = errorCode.message,
            description = "Validation failed for one or more fields",
            errors = errors
        )
        return ResponseEntity.status(errorCode.httpStatus).body(response)
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(ex: BadCredentialsException): ResponseEntity<ErrorResponse> {
        val errorCode = ErrorCode.UNAUTHORIZED
        val response = ErrorResponse(
            code = errorCode.code,
            message = errorCode.message,
            description = "Invalid email or password"
        )
        return ResponseEntity.status(errorCode.httpStatus).body(response)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val errorCode = ErrorCode.BAD_REQUEST
        val response = ErrorResponse(
            code = errorCode.code,
            message = errorCode.message,
            description = ex.message ?: "Invalid argument"
        )
        return ResponseEntity.status(errorCode.httpStatus).body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        ex.printStackTrace()
        
        val errorCode = ErrorCode.INTERNAL_SERVER_ERROR
        val response = ErrorResponse(
            code = errorCode.code,
            message = errorCode.message,
            description = "An unexpected error occurred"
        )
        return ResponseEntity.status(errorCode.httpStatus).body(response)
    }
}
