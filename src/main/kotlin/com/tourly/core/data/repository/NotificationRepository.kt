package com.tourly.core.data.repository

import com.tourly.core.data.entity.NotificationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : JpaRepository<NotificationEntity, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long): List<NotificationEntity>
    fun countByUserIdAndIsReadFalse(userId: Long): Int
    fun deleteAllByUserId(userId: Long)
}
