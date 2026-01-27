package com.tourly.core.api.controller

import com.tourly.core.api.dto.notification.NotificationDto
import com.tourly.core.mapper.NotificationMapper
import com.tourly.core.service.NotificationService
import com.tourly.core.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val userService: UserService
) {
    @GetMapping
    fun getNotifications(authentication: Authentication): ResponseEntity<List<NotificationDto>> {
        val user = userService.getUserByEmail(authentication.name)
        val notifications = notificationService.getNotificationsForUser(user.id!!)
        return ResponseEntity.ok(notifications.map { NotificationMapper.toDto(it) })
    }

    @GetMapping("/unread-count")
    fun getUnreadCount(authentication: Authentication): ResponseEntity<Int> {
        val user = userService.getUserByEmail(authentication.name)
        return ResponseEntity.ok(notificationService.getUnreadCount(user.id!!))
    }

    @PostMapping("/{id}/read")
    fun markAsRead(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<Unit> {
        val user = userService.getUserByEmail(authentication.name)
        notificationService.markAsRead(id, user.id!!)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/read-all")
    fun markAllAsRead(authentication: Authentication): ResponseEntity<Unit> {
        val user = userService.getUserByEmail(authentication.name)
        notificationService.markAllAsRead(user.id!!)
        return ResponseEntity.ok().build()
    }
}
