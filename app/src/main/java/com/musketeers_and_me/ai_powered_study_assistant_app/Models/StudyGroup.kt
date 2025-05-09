package com.musketeers_and_me.ai_powered_study_assistant_app.Models

data class StudyGroup(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val code: String = "",
    val memberCount: Int = 0,
    val userRole: String = "member" // "admin" or "member"
) {
    // No-argument constructor for Firebase
    constructor() : this(
        id = "",
        name = "",
        description = "",
        createdBy = "",
        createdAt = System.currentTimeMillis(),
        code = "",
        memberCount = 0,
        userRole = "member"
    )
} 