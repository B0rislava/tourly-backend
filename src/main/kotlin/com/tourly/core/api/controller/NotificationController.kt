package com.tourly.core.api.controller

import com.tourly.core.api.dto.notification.NotificationDto
import com.tourly.core.service.NotificationService
import com.tourly.core.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Notifications", description = "Endpoints for managing user notifications")
@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val userService: UserService
) {
    @Operation(summary = "Get notifications", description = "Fetches all notifications for the currently authenticated user")
    @GetMapping
    fun getNotifications(authentication: Authentication): ResponseEntity<List<NotificationDto>> {
        val userId = userService.getUserIdByEmail(authentication.name)
        val notifications = notificationService.getNotificationsForUser(userId)
        return ResponseEntity.ok(notifications)
    }

    @Operation(summary = "Get unread count", description = "Returns the number of unread notifications for the user")
    @GetMapping("/unread-count")
    fun getUnreadCount(authentication: Authentication): ResponseEntity<Int> {
        val userId = userService.getUserIdByEmail(authentication.name)
        return ResponseEntity.ok(notificationService.getUnreadCount(userId))
    }

    @Operation(summary = "Mark as read", description = "Marks a specific notification as read")
    @PostMapping("/{id}/read")
    fun markAsRead(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<Unit> {
        val userId = userService.getUserIdByEmail(authentication.name)
        notificationService.markAsRead(id, userId)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "Mark all as read", description = "Marks all notifications for the current user as read")
    @PostMapping("/read-all")
    fun markAllAsRead(authentication: Authentication): ResponseEntity<Unit> {
        val userId = userService.getUserIdByEmail(authentication.name)
        notificationService.markAllAsRead(userId)
        return ResponseEntity.ok().build()
    }
}
