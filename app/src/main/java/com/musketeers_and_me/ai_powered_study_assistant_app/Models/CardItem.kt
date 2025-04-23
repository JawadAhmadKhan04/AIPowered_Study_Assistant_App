package com.musketeers_and_me.ai_powered_study_assistant_app.Models

data class CardItem(
    val title: String,
    val iconResId: Int,
    val value: String = "" // Optional: default empty for screens that don't need it
)
