package com.tourly.core.data.repository

import com.tourly.core.data.entity.MessageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository : JpaRepository<MessageEntity, Long> {
    fun findByTourIdOrderByTimestampAsc(tourId: Long): List<MessageEntity>
    fun deleteAllBySenderId(senderId: Long)
    fun deleteAllByTourIdIn(tourIds: List<Long>)
}
