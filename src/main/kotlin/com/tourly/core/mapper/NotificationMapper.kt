package com.tourly.core.mapper

import com.tourly.core.api.dto.notification.NotificationDto
import com.tourly.core.data.entity.NotificationEntity

object NotificationMapper {
    fun toDto(entity: NotificationEntity): NotificationDto {
        return NotificationDto(
            id = entity.id,
            title = entity.title,
            message = entity.message,
            isRead = entity.isRead,
            createdAt = entity.createdAt,
            type = entity.type,
            relatedId = entity.relatedId
        )
    }
}
