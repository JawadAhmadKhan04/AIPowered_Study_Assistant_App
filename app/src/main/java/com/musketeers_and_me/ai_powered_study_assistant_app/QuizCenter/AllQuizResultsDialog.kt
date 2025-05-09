package com.musketeers_and_me.ai_powered_study_assistant_app.QuizCenter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBReadOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.QuizResult
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
    private val databaseService = FBDataBaseService()
    private val fbReadOperations = FBReadOperations(databaseService)

    init {
        setupDialog()
    }

    @SuppressLint("SetTextI18n")
    private fun setupDialog() {
        val view = LayoutInflater.from(dialog.context).inflate(R.layout.dialog_all_quiz_results, null)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        topicNameTextView = view.findViewById(R.id.topicNameTextView)
        totalQuestionsValue = view.findViewById(R.id.totalQuestionsValue)
        attemptedQuestionsValue = view.findViewById(R.id.attemptedQuestionsValue)
        correctQuestionsValue = view.findViewById(R.id.correctQuestionsValue)
        incorrectQuestionsValue = view.findViewById(R.id.incorrectQuestionsValue)
        totalScoreValue = view.findViewById(R.id.totalScoreValue)
        closeButton = view.findViewById(R.id.closeButton)

        closeButton.setOnClickListener {
            dismiss()
        }

        topicNameTextView.text = quizResult.title
        totalQuestionsValue.text = ": ${quizResult.questionCount}"
        totalScoreValue.text = ": ${quizResult.score}%"

        fbReadOperations.getQuizQuestions(quizResult.quizId, dialog.context) { questions, questionKeys ->
            questions.forEachIndexed { index, question ->
            }
            val attemptedCount = questions.count { it.isAttempted }
            val correctCount = questions.count { it.isCorrect == true }
            attemptedQuestionsValue.text = ": $attemptedCount"
            correctQuestionsValue.text = ": $correctCount"
            incorrectQuestionsValue.text = ": ${attemptedCount - correctCount}"
        }
    }

    fun show() {
        dialog.show()
    }

    private fun dismiss() {
        dialog.dismiss()
    }
}