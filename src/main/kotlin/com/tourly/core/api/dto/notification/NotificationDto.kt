package com.tourly.core.api.dto.notification

import java.time.LocalDateTime

data class NotificationDto(
    val id: Long,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val createdAt: LocalDateTime,
    val type: String?,
    val relatedId: Long?
)
