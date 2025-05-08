package com.musketeers_and_me.ai_powered_study_assistant_app.QuizCenter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBReadOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Question

class QuizQuestionFragment : Fragment() {
    private lateinit var questionNumberText: TextView
    private lateinit var questionText: TextView
    private lateinit var optionsRadioGroup: RadioGroup
    private lateinit var prevQuestionButton: MaterialButton
    private lateinit var nextQuestionButton: MaterialButton
    private lateinit var seeResultsButton: MaterialButton
    private var currentQuestionIndex = 0
    private var totalQuestions = 0
    private var quizId = ""
    private val userAnswers = mutableMapOf<Int, String>()
    private val questionIds = mutableMapOf<Int, String>()
    private val questions = mutableListOf<Question>()
    private val databaseService = FBDataBaseService()
    private val readOperations = FBReadOperations(databaseService)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("QuizQuestionFragment", "Fragment created or recreated")
        return inflater.inflate(R.layout.quiz_question_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        questionNumberText = view.findViewById(R.id.questionNumberText)
        questionText = view.findViewById(R.id.questionText)
        optionsRadioGroup = view.findViewById(R.id.optionsRadioGroup)
        prevQuestionButton = view.findViewById(R.id.prevQuestionButton)
        nextQuestionButton = view.findViewById(R.id.nextQuestionButton)
        seeResultsButton = view.findViewById(R.id.seeResultsButton)

        prevQuestionButton.setOnClickListener {
            saveCurrentAnswer()
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                updateQuestion()
            }
        }

        nextQuestionButton.setOnClickListener {
            saveCurrentAnswer()
            if (currentQuestionIndex < totalQuestions - 1) {
                currentQuestionIndex++
                updateQuestion()
            }
        }

        seeResultsButton.setOnClickListener {
            saveCurrentAnswer()
            startQuizResults()
        }

        optionsRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId != -1) {
                val radioButton = group.findViewById<RadioButton>(checkedId)
                userAnswers[currentQuestionIndex] = radioButton.text.toString()
                Log.d("QuizQuestionFragment", "Selected answer: ${radioButton.text} for question $currentQuestionIndex")
            }
        }
    }

    fun setQuizData(quizId: String, questionCount: Int) {
        Log.d("QuizQuestionFragment", "setQuizData called with quizId: $quizId, questionCount: $questionCount")
        this.quizId = quizId
        this.totalQuestions = questionCount
        questions.clear()
        questionIds.clear()
        userAnswers.clear()
        currentQuestionIndex = 0

        readOperations.getQuizQuestions(quizId) { fetchedQuestions, questionKeys ->
            Log.d("QuizQuestionFragment", "Fetched ${fetchedQuestions.size} questions for quizId: $quizId, keys: $questionKeys")
            questions.addAll(fetchedQuestions)
            questionKeys.forEachIndexed { index, key ->
                questionIds[index] = key
            }
            requireActivity().runOnUiThread {
                if (questions.isNotEmpty()) {
                    Log.d("QuizQuestionFragment", "Questions loaded: $questions")
                    updateQuestion()
                } else {
                    Toast.makeText(requireContext(), "No questions loaded for quiz", Toast.LENGTH_SHORT).show()
                    Log.e("QuizQuestionFragment", "No questions found for quizId: $quizId")
                    questionNumberText.text = "No Questions"
                    questionText.text = "No questions available"
                    optionsRadioGroup.removeAllViews()
                    prevQuestionButton.isEnabled = false
                    nextQuestionButton.isEnabled = false
                    seeResultsButton.visibility = View.GONE
                }
            }
        }
    }

    private fun saveCurrentAnswer() {
        val selectedId = optionsRadioGroup.checkedRadioButtonId
        if (selectedId != -1) {
            val radioButton = optionsRadioGroup.findViewById<RadioButton>(selectedId)
            val selectedAnswer = radioButton.text.toString()
            userAnswers[currentQuestionIndex] = selectedAnswer
            val question = questions[currentQuestionIndex]
            val questionId = questionIds[currentQuestionIndex] ?: return
            val quizRef = databaseService.quizzesRef.child(quizId).child("questions").child(questionId)
            Log.d("QuizQuestionFragment", "Attempting to save answer for questionId: $questionId, selectedAnswer: $selectedAnswer")
            quizRef.updateChildren(
                mapOf(
                    "isAttempted" to true,
                    "selectedAnswer" to selectedAnswer,
                    "isCorrect" to (selectedAnswer == question.correctAnswer)
                )
            ).addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to save answer: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("QuizQuestionFragment", "Failed to save answer for questionId: $questionId, error: ${e.message}", e)
            }
        }
    }

    private fun updateQuestion() {
        if (questions.isEmpty()) {
            Log.e("QuizQuestionFragment", "Questions list is empty")
            questionNumberText.text = "No Questions"
            questionText.text = "No questions available"
            optionsRadioGroup.removeAllViews()
            prevQuestionButton.isEnabled = false
            nextQuestionButton.isEnabled = false
            seeResultsButton.visibility = View.GONE
            return
        }

        val question = questions[currentQuestionIndex]
        Log.d("QuizQuestionFragment", "Displaying question $currentQuestionIndex: ${question.question}")

        questionNumberText.text = "Question ${currentQuestionIndex + 1}/$totalQuestions"
        questionText.text = question.question
        prevQuestionButton.isEnabled = currentQuestionIndex > 0
        nextQuestionButton.isEnabled = currentQuestionIndex < totalQuestions - 1
        seeResultsButton.visibility = if (currentQuestionIndex == totalQuestions - 1) View.VISIBLE else View.GONE
        nextQuestionButton.visibility = if (currentQuestionIndex == totalQuestions - 1) View.GONE else View.VISIBLE

        optionsRadioGroup.removeAllViews()
        question.options.forEach { (key, value) ->
            val radioButton = RadioButton(requireContext()).apply {
                text = value
                id = View.generateViewId()
                textSize = 16f
                setPadding(8, 8, 8, 8)
            }
            optionsRadioGroup.addView(radioButton)
        }

        optionsRadioGroup.clearCheck()
        userAnswers[currentQuestionIndex]?.let { answer ->
            for (i in 0 until optionsRadioGroup.childCount) {
                val radioButton = optionsRadioGroup.getChildAt(i) as RadioButton
                if (radioButton.text == answer) {
                    radioButton.isChecked = true
                    break
                }
            }
        }

        optionsRadioGroup.requestLayout()
        questionText.requestLayout()
        questionNumberText.requestLayout()
    }

    private fun startQuizResults() {
        val intent = Intent(requireContext(), QuizResultsActivity::class.java).apply {
            putExtra("quizId", quizId)
            putExtra("totalQuestions", totalQuestions)
        }
        startActivity(intent)
    }
}