package com.tourly.core.api.controller

import com.tourly.core.api.dto.ChatMessageDto
import com.tourly.core.service.MessageService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody

@Tag(name = "Chat", description = "Endpoints for real-time and historical tour communication")
@Controller
class ChatController(
    private val messageService: MessageService
) {

    @MessageMapping("/chat/{tourId}")
    @SendTo("/topic/messages/{tourId}")
    fun sendMessage(
        @DestinationVariable tourId: Long,
        @Payload messageDto: ChatMessageDto
    ): ChatMessageDto {
        println("ChatController: Received message for tour $tourId: ${messageDto.content}")
        val saved = messageService.saveMessage(messageDto.copy(tourId = tourId))
        println("ChatController: Saved message with ID ${saved.id} for tour $tourId")
        return saved
    }

    @Operation(summary = "Get messages", description = "Fetches chat history for a specific tour")
    @GetMapping("/api/chat/{tourId}/messages")
    @ResponseBody
    fun getMessages(@PathVariable tourId: Long): List<ChatMessageDto> {
        println("ChatController: Fetching messages for tour $tourId")
        val messages = messageService.getMessagesForTour(tourId)
        println("ChatController: Returning ${messages.size} messages for tour $tourId")
        return messages
    }
}
