package com.musketeers_and_me.ai_powered_study_assistant_app.QuizCenter

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.ImageButton
import android.widget.TextView
import com.musketeers_and_me.ai_powered_study_assistant_app.R

class ExplanationDialog(context: Context) {
    private val dialog: Dialog = Dialog(context)
    private lateinit var titleTextView: TextView
    private lateinit var contentTextView: TextView
    private lateinit var closeButton: ImageButton

    init {
        setupDialog()
    }

    private fun setupDialog() {
        val view = LayoutInflater.from(dialog.context).inflate(R.layout.dialog_explanation, null)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Initialize views
        titleTextView = view.findViewById(R.id.explanationTitleTextView)
        contentTextView = view.findViewById(R.id.explanationContentTextView)
        closeButton = view.findViewById(R.id.closeButton)

        // Set click listener for close button
        closeButton.setOnClickListener {
            dismiss()
        }
    }

    fun setTitle(title: String) {
        titleTextView.text = title
    }

    fun setContent(content: String) {
        contentTextView.text = content
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }
} 