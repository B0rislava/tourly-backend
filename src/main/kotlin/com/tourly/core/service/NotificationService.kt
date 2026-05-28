package com.tourly.core.service

import com.tourly.core.api.dto.notification.NotificationDto
import com.tourly.core.data.entity.NotificationEntity
import com.tourly.core.data.entity.UserEntity
import com.tourly.core.data.repository.NotificationRepository
import com.tourly.core.data.mapper.NotificationMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository
) {
    @Transactional
    fun createNotification(user: UserEntity, title: String, message: String, type: String? = null, relatedId: Long? = null) {
        val notification = NotificationEntity(
            user = user,
            title = title,
            message = message,
            type = type,
            relatedId = relatedId
        )
        notificationRepository.save(notification)
    }

    @Transactional(readOnly = true)
    fun getNotificationsForUser(userId: Long): List<NotificationDto> {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
            .map { NotificationMapper.toDto(it) }
    }

    @Transactional(readOnly = true)
    fun getUnreadCount(userId: Long): Int {
        return notificationRepository.countByUserIdAndIsReadFalse(userId)
    }

    @Transactional
    fun markAsRead(notificationId: Long, userId: Long) {
        val notification = notificationRepository.findById(notificationId).orElse(null)
        if (notification != null && notification.user.id == userId) {
            notification.isRead = true
            notificationRepository.save(notification)
        }
    }

    @Transactional
    fun markAllAsRead(userId: Long) {
        val notifications = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
        notifications.forEach { 
            if (!it.isRead) {
                it.isRead = true
            }
        }
        notificationRepository.saveAll(notifications)
    }
}
