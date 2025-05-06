package com.musketeers_and_me.ai_powered_study_assistant_app.Models

/**
 * Data model representing a user profile.
 * Contains both local and remote data properties.
 */
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val profileImage: String? = null,
    val fcmToken: String? = null,
    val status: String? = "offline",
    val pendingSync: Boolean = false
) 