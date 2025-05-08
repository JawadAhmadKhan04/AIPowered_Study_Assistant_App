package com.musketeers_and_me.ai_powered_study_assistant_app.Models

data class Question(
    val question: String = "",
    val options: Map<String, String> = emptyMap(),
    val correctAnswer: String = "",
    val explanation: String = "",
    val isAttempted: Boolean = false,
    val isCorrect: Boolean = false,
    val selectedAnswer: String = ""
) {
    constructor() : this("", emptyMap(), "", "", false, false, "")
}