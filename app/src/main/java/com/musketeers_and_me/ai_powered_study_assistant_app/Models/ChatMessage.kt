package com.musketeers_and_me.ai_powered_study_assistant_app.Models

data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val message: String,
    val topicName: String? = null,  // For notes/topics
    val timestamp: Long = System.currentTimeMillis(),
    val messageType: MessageType = MessageType.REGULAR
)

// ... existing code ... 