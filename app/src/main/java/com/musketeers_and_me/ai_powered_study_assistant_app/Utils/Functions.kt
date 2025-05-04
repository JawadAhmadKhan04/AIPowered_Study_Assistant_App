package com.musketeers_and_me.ai_powered_study_assistant_app.Utils

object Functions {
    fun countWords(input: String): String {
        val words = input.trim().split("\\s+".toRegex())
        val wordCount = words.size
        println("Word count: $wordCount")
        return "$wordCount Words"
    }


}