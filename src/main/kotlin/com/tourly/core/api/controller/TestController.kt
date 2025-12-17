package com.tourly.core.api.controller

import com.tourly.core.api.dto.User
import com.tourly.core.service.TestService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test")
class TestController(
    private val testService: TestService
) {
    // Public endpoint
    @GetMapping("/public")
    fun publicEnpoint(): ResponseEntity<Map<String, String>>{
        return ResponseEntity.ok(
            mapOf("message" to "This is a public endpoint - no authentication required")
        )
    }

    // Secured endpoint - required JWT token
    @GetMapping("/secured")
    fun securedEnpoint(authentication: Authentication): ResponseEntity<Map<String, String>>{
        return ResponseEntity.ok(
            mapOf(
                "message" to "This is a secured endpoint",
                "user" to authentication.name
            )
        )
    }

    // Get all users - secured endpoint
    @GetMapping("/users")
    fun getAllUsers(): ResponseEntity<List<User>> {
        return ResponseEntity.ok(testService.getAllUsers())
    }

    // Get user by ID - secured endpoint
    @GetMapping("/users/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<User> {
        val user = testService.getUserById(id)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    // Create new user - secured endpoint
    @PostMapping("/users")
    fun createUser(@RequestBody user: User): ResponseEntity<User> {
        val createdUser = testService.createUser(user)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser)

    }

    // Create new user - secured endpoint
    @GetMapping("/me")
    fun getUserByEmail(authentication: Authentication): ResponseEntity<User> {
        val user = testService.getUserByEmail(authentication.name)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    // Admin only endpoint - requires GUIDE role
    @PreAuthorize("hasRole('GUIDE')")
    @GetMapping("/admin")
    fun adminEndpoint(authentication: Authentication): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(
            mapOf(
                "message" to "This is a admin endpoint - only for guides",
                "user" to authentication.name
            )
        )
    }

}