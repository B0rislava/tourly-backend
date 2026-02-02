package com.tourly.core.api.controller

import com.tourly.core.api.dto.UpdateProfileRequestDto
import com.tourly.core.api.dto.UserDto
import com.tourly.core.api.dto.auth.LoginResponseDto
import com.tourly.core.security.CustomUserDetails
import com.tourly.core.security.JWTUtil
import com.tourly.core.service.AuthService
import com.tourly.core.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Tag(name = "Users", description = "Endpoints for user profile management")
@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val jwtUtil: JWTUtil,
    private val authService: AuthService
) {

    @Operation(summary = "Get current profile", description = "Fetches the profile details of the currently authenticated user")
    @GetMapping("/me")
    fun me(@AuthenticationPrincipal principal: CustomUserDetails): ResponseEntity<UserDto> {
        return ResponseEntity.ok(
            userService.getCurrentUserProfile(principal.getUserId())
        )
    }

    @Operation(summary = "Get user profile by ID", description = "Fetches the profile details of a user by their ID")
    @GetMapping("/{id}")
    fun getUserProfile(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: CustomUserDetails?
    ): ResponseEntity<UserDto> {
        val currentUserId = principal?.getUserId()
        return ResponseEntity.ok(userService.getUserProfileById(id, currentUserId))
    }

    @Operation(summary = "Update profile", description = "Updates the profile details of the currently authenticated user")
    @PutMapping("/me")
    fun updateProfile(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: UpdateProfileRequestDto
    ): ResponseEntity<LoginResponseDto> {
        val userId = userDetails.getUserId()
        val updatedUser = userService.updateProfile(userId, request)
        
        val token = jwtUtil.generateAccessToken(
            username = updatedUser.email,
            roles = listOf(updatedUser.role.name)
        )
        
        val refreshToken = authService.createAndSaveRefreshToken(updatedUser.id!!, updatedUser.email)
        
        return ResponseEntity.ok(
            LoginResponseDto(
                token = token,
                refreshToken = refreshToken,
                user = updatedUser
            )
        )
    }

    @Operation(summary = "Upload profile picture", description = "Updates the profile picture of the currently authenticated user")
    @PostMapping("/me/picture", consumes = ["multipart/form-data"])
    fun uploadProfilePicture(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<UserDto> {
        val userId = userDetails.getUserId()
        val updatedUser = userService.updateProfilePicture(userId, file)
        return ResponseEntity.ok(updatedUser)
    }

    @Operation(summary = "Follow user", description = "Follow another user")
    @PostMapping("/{id}/follow")
    fun followUser(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable id: Long
    ): ResponseEntity<Unit> {
        userService.followUser(userDetails.getUserId(), id)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "Unfollow user", description = "Unfollow a user")
    @DeleteMapping("/{id}/follow")
    fun unfollowUser(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable id: Long
    ): ResponseEntity<Unit> {
        userService.unfollowUser(userDetails.getUserId(), id)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "Delete profile", description = "Deletes the currently authenticated user's account")
    @DeleteMapping("/me")
    fun deleteProfile(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<Unit> {
        userService.deleteUser(userDetails.getUserId())
        return ResponseEntity.noContent().build()
    }
}
