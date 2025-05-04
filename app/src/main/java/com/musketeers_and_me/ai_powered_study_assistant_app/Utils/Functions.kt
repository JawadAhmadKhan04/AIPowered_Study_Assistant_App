package com.musketeers_and_me.ai_powered_study_assistant_app.Utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

object Functions {
    fun countWords(input: String): String {
        val words = input.trim().split("\\s+".toRegex())
        val wordCount = words.size
        println("Word count: $wordCount")
        return "$wordCount Words"
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun countPoints(conceptList: String): String {
        return conceptList.split("\n")
            .filter { it.trim().matches(Regex("^\\d+\\.\\s*.*")) }
            .count().toString() + " Points"
    }


}