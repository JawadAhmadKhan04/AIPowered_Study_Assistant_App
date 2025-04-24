package com.musketeers_and_me.ai_powered_study_assistant_app.Models

data class Course(
    val title: String,
    val noteCount: Int,
    val daysAgo: Int,
    val buttonColorResId: Int,
    val bookmarked: Boolean
)
