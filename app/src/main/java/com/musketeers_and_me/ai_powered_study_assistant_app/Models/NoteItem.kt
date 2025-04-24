package com.musketeers_and_me.ai_powered_study_assistant_app.LectureAndNotes

data class NoteItem(
    val title: String,
    val age: String,
    val type: NoteType
)

enum class NoteType {
    TEXT,
    VOICE
}
