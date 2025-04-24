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

class AllQuizResultsDialog(
    context: Context,
    private val quizResult: QuizResult
) {
    private val dialog: Dialog = Dialog(context)
    private lateinit var topicNameTextView: TextView
    private lateinit var totalQuestionsValue: TextView
    private lateinit var attemptedQuestionsValue: TextView
    private lateinit var correctQuestionsValue: TextView
    private lateinit var incorrectQuestionsValue: TextView
    private lateinit var totalScoreValue: TextView
    private lateinit var closeButton: ImageButton

    init {
        setupDialog()
    }

    private fun setupDialog() {
        val view = LayoutInflater.from(dialog.context).inflate(R.layout.dialog_all_quiz_results, null)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Initialize views
        topicNameTextView = view.findViewById(R.id.topicNameTextView)
        totalQuestionsValue = view.findViewById(R.id.totalQuestionsValue)
        attemptedQuestionsValue = view.findViewById(R.id.attemptedQuestionsValue)
        correctQuestionsValue = view.findViewById(R.id.correctQuestionsValue)
        incorrectQuestionsValue = view.findViewById(R.id.incorrectQuestionsValue)
        totalScoreValue = view.findViewById(R.id.totalScoreValue)
        closeButton = view.findViewById(R.id.closeButton)

        // Set click listener for close button
        closeButton.setOnClickListener {
            dismiss()
        }

        // Set quiz details
        topicNameTextView.text = quizResult.title
        totalQuestionsValue.text = ": 40"
        attemptedQuestionsValue.text = ": 20"
        correctQuestionsValue.text = ": 10"
        incorrectQuestionsValue.text = ": 10"
        totalScoreValue.text = ": ${quizResult.score}%"
    }

    fun show() {
        dialog.show()
    }

    private fun dismiss() {
        dialog.dismiss()
    }
}