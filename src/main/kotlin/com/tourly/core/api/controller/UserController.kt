package com.tourly.core.api.controller

import com.tourly.core.api.dto.UserDto
import com.tourly.core.data.enumeration.UserRole
import com.tourly.core.security.CustomUserDetails
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController {

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<*> {
        try {
            val roleName = userDetails.getRole()
            println("DEBUG: Fetching profile for user: ${userDetails.username}, role: $roleName")
            
            val user = UserDto(
                id = userDetails.getUserId(),
                email = userDetails.username,
                firstName = userDetails.getFirstName(),
                lastName = userDetails.getLastName(),
                role = UserRole.valueOf(roleName),
                profilePictureUrl = userDetails.getProfilePictureUrl()
            )
            return ResponseEntity.ok(user)
        } catch (e: Exception) {
            e.printStackTrace() // Print stack trace to backend console
            println("DEBUG: Error in /me endpoint: ${e.message}")
            return ResponseEntity.status(500).body("Error fetching profile: ${e.message}")
        }
    }
}
