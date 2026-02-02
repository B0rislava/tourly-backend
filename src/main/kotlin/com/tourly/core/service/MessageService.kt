package com.tourly.core.service

import com.tourly.core.api.dto.ChatMessageDto
import com.tourly.core.data.entity.MessageEntity
import com.tourly.core.data.repository.MessageRepository
import com.tourly.core.data.repository.TourRepository
import com.tourly.core.data.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class MessageService(
    private val messageRepository: MessageRepository,
    private val tourRepository: TourRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun saveMessage(dto: ChatMessageDto): ChatMessageDto {
        val tour = tourRepository.findById(dto.tourId)
            .orElseThrow { IllegalArgumentException("Tour not found") }
        val sender = userRepository.findById(dto.senderId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val entity = MessageEntity(
            tour = tour,
            sender = sender,
            content = dto.content,
            timestamp = LocalDateTime.now()
        )

        val saved = messageRepository.save(entity)
        return ChatMessageDto(
            id = saved.id,
            tourId = saved.tour.id,
            senderId = saved.sender.id!!,
            senderName = "${saved.sender.firstName} ${saved.sender.lastName}",
            content = saved.content,
            timestamp = saved.timestamp.toString()
        )
    }

    @Transactional(readOnly = true)
    fun getMessagesForTour(tourId: Long): List<ChatMessageDto> {
        return messageRepository.findByTourIdOrderByTimestampAsc(tourId).map {
            ChatMessageDto(
                id = it.id,
                tourId = it.tour.id,
                senderId = it.sender.id!!,
                senderName = "${it.sender.firstName} ${it.sender.lastName}",
                content = it.content,
                timestamp = it.timestamp.toString()
            )
        }
    }
}
