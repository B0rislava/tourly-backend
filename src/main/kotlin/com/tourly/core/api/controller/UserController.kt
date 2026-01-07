package com.tourly.core.api.controller

import com.tourly.core.api.dto.UpdateProfileRequestDto
import com.tourly.core.api.dto.UserDto
import com.tourly.core.api.dto.auth.LoginResponseDto
import com.tourly.core.service.UserService
import com.tourly.core.data.enumeration.UserRole
import com.tourly.core.exception.APIException
import com.tourly.core.exception.ErrorCode
import com.tourly.core.security.CustomUserDetails
import com.tourly.core.security.JWTUtil
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val jwtUtil: JWTUtil
) {

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

    @PutMapping("/me")
    fun updateProfile(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestBody request: UpdateProfileRequestDto
    ): ResponseEntity<LoginResponseDto> {
        val userId = userDetails.getUserId() ?: throw APIException(
            ErrorCode.INTERNAL_SERVER_ERROR,
            "User ID not found in principal"
        )
        val updatedUser = userService.updateProfile(userId, request)
        
        val token = jwtUtil.generateToken(
            username = updatedUser.email,
            roles = listOf(updatedUser.role.name)
        )
        
        return ResponseEntity.ok(
            LoginResponseDto(
                token = token,
                user = updatedUser
            )
        )
    }

    @PostMapping("/me/picture", consumes = ["multipart/form-data"])
    fun uploadProfilePicture(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @org.springframework.web.bind.annotation.RequestParam("file") file: org.springframework.web.multipart.MultipartFile
    ): ResponseEntity<UserDto> {
        val userId = userDetails.getUserId() ?: throw APIException(
            ErrorCode.INTERNAL_SERVER_ERROR,
            "User ID not found in principal"
        )
        val updatedUser = userService.updateProfilePicture(userId, file)
        return ResponseEntity.ok(updatedUser)
    }
}
