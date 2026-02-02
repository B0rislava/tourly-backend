package com.tourly.core.api.dto

data class ChatMessageDto(
    val id: Long? = null,
    val tourId: Long,
    val senderId: Long,
    val senderName: String,
    val content: String,
    val timestamp: String
)
