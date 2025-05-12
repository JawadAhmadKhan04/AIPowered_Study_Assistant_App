package com.musketeers_and_me.ai_powered_study_assistant_app.Models

data class GroupMessage(
    val id: String = "",
    val groupId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isCurrentUser: Boolean = false,
    val messageType: MessageType = MessageType.REGULAR,
    val noteId: String = "",
    val noteType: String = "",
    val wasOffline: Boolean = false
) {
    // No-argument constructor for Firebase
    constructor() : this(
        id = "",
        groupId = "",
        senderId = "",
        senderName = "",
        content = "",
        timestamp = System.currentTimeMillis(),
        isCurrentUser = false,
        messageType = MessageType.REGULAR,
        noteId = "",
        noteType = "",
        wasOffline = false
    )
} 