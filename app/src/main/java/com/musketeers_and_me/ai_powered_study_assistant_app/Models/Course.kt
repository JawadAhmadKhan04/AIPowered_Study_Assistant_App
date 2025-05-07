package com.musketeers_and_me.ai_powered_study_assistant_app.Models

data class Course(
    val title: String = "",
    val noteCount: Int = 0,
    val daysAgo: Int = 0,
    val buttonColorResId: Int = 2131100436,
    var bookmarked: Boolean = false,
    val courseId: String = "",
    val description: String = ""
) {
    // No-argument constructor for Firebase
    constructor() : this(
        title = "",
        noteCount = 0,
        daysAgo = 0,
        buttonColorResId = 2131100436,
        bookmarked = false,
        courseId = "",
        description = ""
    )
}
