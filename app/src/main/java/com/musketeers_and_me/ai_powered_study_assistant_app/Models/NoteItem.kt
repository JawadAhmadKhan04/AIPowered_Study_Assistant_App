package com.musketeers_and_me.ai_powered_study_assistant_app.Models

data class NoteItem(
    val title: String = "",
    val age: String = "0",
    val type: String = "text",
    val createdAt: Long = 0,
    val note_id: String = ""
)