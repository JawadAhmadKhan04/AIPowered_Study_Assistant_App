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
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBWriteOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Question

class QuizQuestionFragment : Fragment() {
    private lateinit var questionNumberText: TextView
    private lateinit var questionText: TextView
    private lateinit var selectAnswerText: TextView
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
    private val writeOperations = FBWriteOperations(databaseService)

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
        selectAnswerText = view.findViewById(R.id.selectAnswerText)
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
                // Find the option key corresponding to the selected text
                val selectedKey = questions[currentQuestionIndex].options.entries
                    .find { it.value == radioButton.text.toString() }?.key
                selectedKey?.let {
                    userAnswers[currentQuestionIndex] = it
                    Log.d("QuizQuestionFragment", "Selected answer key: $it for question $currentQuestionIndex")
                }
            }
        }

        // Initialize UI with placeholder text until quiz data is loaded
        questionNumberText.text = "Loading..."
        questionText.text = "Please wait while the quiz is loaded"
        selectAnswerText.visibility = View.GONE
        optionsRadioGroup.removeAllViews()
        prevQuestionButton.isEnabled = false
        nextQuestionButton.isEnabled = false
        seeResultsButton.visibility = View.GONE
    }

    fun setQuizData(quizId: String, questionCount: Int) {
        Log.d("QuizQuestionFragment", "setQuizData called with quizId: $quizId, questionCount: $questionCount")
        this.quizId = quizId
        this.totalQuestions = questionCount
        questions.clear()
        questionIds.clear()
        userAnswers.clear()
        currentQuestionIndex = 0

        readOperations.getQuizQuestions(quizId, requireContext()) { fetchedQuestions, questionKeys ->
            Log.d("QuizQuestionFragment", "Fetched ${fetchedQuestions.size} questions for quizId: $quizId, keys: $questionKeys")
            questions.addAll(fetchedQuestions)
            questionKeys.forEachIndexed { index, key ->
                questionIds[index] = key
            }
            requireActivity().runOnUiThread {
                if (questions.isNotEmpty()) {
                    Log.d("QuizQuestionFragment", "Questions loaded: $questions")
                    totalQuestions = questions.size // Update totalQuestions based on actual data
                    updateQuestion()
                } else {
                    Toast.makeText(requireContext(), "No questions loaded for quiz", Toast.LENGTH_SHORT).show()
                    Log.e("QuizQuestionFragment", "No questions found for quizId: $quizId")
                    questionNumberText.text = "No Questions"
                    selectAnswerText.visibility = View.VISIBLE
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
            // Find the option key corresponding to the selected text
            val selectedKey = questions[currentQuestionIndex].options.entries
                .find { it.value == radioButton.text.toString() }?.key
            if (selectedKey != null) {
                userAnswers[currentQuestionIndex] = selectedKey
                if (currentQuestionIndex < questions.size) {
                    val question = questions[currentQuestionIndex]
                    val questionId = questionIds[currentQuestionIndex] ?: return
                    val quizRef = databaseService.quizzesRef.child(quizId).child("questions").child(questionId)
                    val isCorrect = selectedKey == question.correctAnswer
                    Log.d("QuizQuestionFragment", "Saving answer for questionId: $questionId, selectedKey: $selectedKey, isCorrect: $isCorrect")
                    quizRef.updateChildren(
                        mapOf(
                            "isAttempted" to true,
                            "selectedAnswer" to selectedKey,
                            "isCorrect" to isCorrect
                        )
                    ).addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to save answer: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("QuizQuestionFragment", "Failed to save answer for questionId: $questionId, error: ${e.message}", e)
                    }
                }
            }
        }
    }

    private fun updateQuestion() {
        if (questions.isEmpty()) {
            Log.e("QuizQuestionFragment", "Questions list is empty")
            questionNumberText.text = "No Questions"
            selectAnswerText.visibility = View.VISIBLE
            questionText.text = "No questions available"
            optionsRadioGroup.removeAllViews()
            prevQuestionButton.isEnabled = false
            nextQuestionButton.isEnabled = false
            seeResultsButton.visibility = View.GONE
            return
        }

        val question = questions[currentQuestionIndex]
        Log.d("QuizQuestionFragment", "Displaying question $currentQuestionIndex: ${question.question}, options: ${question.options}")

        questionNumberText.text = "Question ${currentQuestionIndex + 1}/$totalQuestions"
        selectAnswerText.visibility = View.VISIBLE
        questionText.text = question.question
        prevQuestionButton.isEnabled = currentQuestionIndex > 0
        nextQuestionButton.isEnabled = currentQuestionIndex < totalQuestions - 1
        seeResultsButton.visibility = if (currentQuestionIndex == totalQuestions - 1) View.VISIBLE else View.GONE
        nextQuestionButton.visibility = if (currentQuestionIndex == totalQuestions - 1) View.GONE else View.VISIBLE

        optionsRadioGroup.removeAllViews()
        question.options.forEach { (key, optionText) ->
            Log.d("QuizQuestionFragment", "Adding option: key=$key, text=$optionText")
            val radioButton = RadioButton(requireContext()).apply {
                text = optionText
                id = View.generateViewId()
                textSize = 16f
                setPadding(8, 8, 8, 8)
                tag = key
            }
            optionsRadioGroup.addView(radioButton)
        }

        optionsRadioGroup.clearCheck()
        userAnswers[currentQuestionIndex]?.let { selectedKey ->
            for (i in 0 until optionsRadioGroup.childCount) {
                val radioButton = optionsRadioGroup.getChildAt(i) as RadioButton
                if (radioButton.tag == selectedKey) {
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
        saveCurrentAnswer()
        if (quizId.isEmpty()) {
            Toast.makeText(requireContext(), "Error: No quiz selected", Toast.LENGTH_SHORT).show()
            Log.e("QuizQuestionFragment", "Cannot start QuizResultsActivity: quizId is empty")
            return
        }
        // Calculate score
        readOperations.getQuizQuestions(quizId, requireContext()) { questions, _ ->
            val questionCount = questions.size
            val correctCount = questions.count { it.isCorrect == true }
            val score = if (questionCount > 0) (correctCount * 100) / questionCount else 0
            Log.d("QuizQuestionFragment", "Saving quiz results: quizId=$quizId, score=$score, correctCount=$correctCount, questionCount=$questionCount")
            // Save score to Firebase
            writeOperations.updateQuizResults(
                quizId = quizId,
                score = score,
                feedback = "", // Add feedback if needed
                onSuccess = {
                    Log.d("QuizQuestionFragment", "Quiz score saved successfully")
                    startQuizResultsActivity()
                },
                onFailure = { e ->
                    Toast.makeText(requireContext(), "Failed to save quiz score: ${e?.message}", Toast.LENGTH_SHORT).show()
                    Log.e("QuizQuestionFragment", "Failed to save quiz score: ${e?.message}", e)
                    startQuizResultsActivity() // Proceed anyway
                }
            )
        }
    }
    private fun startQuizResultsActivity() {
        val intent = Intent(requireContext(), QuizResultsActivity::class.java).apply {
            putExtra("quizId", quizId)
            putExtra("totalQuestions", totalQuestions)
        }
        startActivity(intent)
    }
}