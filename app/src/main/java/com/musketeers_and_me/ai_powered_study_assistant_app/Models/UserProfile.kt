package com.musketeers_and_me.ai_powered_study_assistant_app.Models

/**
 * Data model representing a user profile.
 * Contains both local and remote data properties.
 */
data class UserProfile(
    val id: String = "",
    val email: String = "",
    val username: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastLogin: Long = System.currentTimeMillis(),
    val fcmToken: String? = null,
    val pendingSync: Boolean = false
) {
    // No-argument constructor for Firebase
    constructor() : this(
        id = "",
        email = "",
        username = "",
        createdAt = System.currentTimeMillis(),
        lastLogin = System.currentTimeMillis(),
        fcmToken = null,
        pendingSync = false
    )
} 