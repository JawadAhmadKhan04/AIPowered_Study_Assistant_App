package com.musketeers_and_me.ai_powered_study_assistant_app.QuizCenter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBReadOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Question
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils

class QuizResultsActivity : AppCompatActivity() {
    private lateinit var questionNumber: TextView
    private lateinit var questionText: TextView
    private lateinit var wrongAnswer: TextView
    private lateinit var correctAnswer: TextView
    private lateinit var option3: TextView
    private lateinit var option4: TextView
    private lateinit var prevButton: MaterialButton
    private lateinit var nextButton: MaterialButton
    private lateinit var explanationButton: TextView
    private lateinit var fbReadOperations: FBReadOperations

    private var currentQuestionIndex = 0
    private var totalQuestions = 0
    private var quizId: String = ""
    private var questions: List<Question> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_results)
        enableEdgeToEdge()

        // Initialize FBDataBaseService correctly
        val databaseService = FBDataBaseService()
        fbReadOperations = FBReadOperations(databaseService)

        ToolbarUtils.setupToolbar(this, "Quiz Results", R.drawable.home_logo_top_bar, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Quiz Results"

        quizId = intent.getStringExtra("quizId") ?: ""
        totalQuestions = intent.getIntExtra("totalQuestions", 0)

        initializeViews()
        setupClickListeners()

        if (quizId.isEmpty()) {
            Log.e("QuizResultsActivity", "Quiz ID is empty")
            showErrorState("Invalid quiz ID")
            return
        }

        fbReadOperations.getQuizQuestions(quizId, this) { fetchedQuestions, questionKeys ->
            questions = fetchedQuestions
            totalQuestions = questions.size
            if (questions.isEmpty()) {
                showErrorState("No questions available")
            } else {
                updateQuestion()
            }
        }

        findViewById<FrameLayout>(R.id.home_button_container).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        navigateToQuizCenter()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateToQuizCenter()
    }

    private fun navigateToQuizCenter() {
        val intent = Intent(this, QuizCenterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun initializeViews() {
        questionNumber = findViewById(R.id.questionNumber)
        questionText = findViewById(R.id.questionText)
        wrongAnswer = findViewById(R.id.wrongAnswer)
        correctAnswer = findViewById(R.id.correctAnswer)
        option3 = findViewById(R.id.option3)
        option4 = findViewById(R.id.option4)
        prevButton = findViewById(R.id.prevQuestionButton)
        nextButton = findViewById(R.id.nextQuestionButton)
        explanationButton = findViewById(R.id.explanationButton)
    }

    private fun setupClickListeners() {
        prevButton.setOnClickListener {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                updateQuestion()
            }
        }

        nextButton.setOnClickListener {
            if (currentQuestionIndex < totalQuestions - 1) {
                currentQuestionIndex++
                updateQuestion()
            }
        }

        explanationButton.setOnClickListener {
            showExplanationDialog()
        }
    }

    private fun updateQuestion() {
        if (questions.isEmpty()) {
            showErrorState("No questions available")
            return
        }

        val question = questions[currentQuestionIndex]
        Log.d("QuizResultsActivity", "Displaying question $currentQuestionIndex: ${question.question}")

        questionNumber.text = "Question ${currentQuestionIndex + 1}/$totalQuestions"
        prevButton.isEnabled = currentQuestionIndex > 0
        nextButton.isEnabled = currentQuestionIndex < totalQuestions - 1
        questionText.text = question.question

        val userAnswer = question.selectedAnswer
        val correctAnswerKey = question.correctAnswer

        wrongAnswer.apply {
            text = if (userAnswer.isNotEmpty()) question.options[userAnswer] ?: "" else ""
            setCompoundDrawablesWithIntrinsicBounds(0, 0, if (userAnswer != correctAnswerKey && userAnswer.isNotEmpty()) R.drawable.cross_circle else 0, 0)
            visibility = if (userAnswer.isNotEmpty() && userAnswer != correctAnswerKey) View.VISIBLE else View.GONE
        }

        correctAnswer.apply {
            text = question.options[correctAnswerKey] ?: ""
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_fill, 0, 0, 0)
            visibility = if (question.options[correctAnswerKey] != null) View.VISIBLE else View.GONE
        }

        val otherOptions = question.options.filter { it.key != userAnswer && it.key != correctAnswerKey }.values.toList()
        option3.apply {
            text = otherOptions.getOrNull(0) ?: ""
            visibility = if (otherOptions.isNotEmpty()) View.VISIBLE else View.GONE
        }
        option4.apply {
            text = otherOptions.getOrNull(1) ?: ""
            visibility = if (otherOptions.size > 1) View.VISIBLE else View.GONE
        }
    }

    private fun showExplanationDialog() {
        if (questions.isEmpty()) return
        val explanation = questions[currentQuestionIndex].explanation
        Log.d("QuizResultsActivity", "Showing explanation: $explanation")
        val dialog = ExplanationDialog(this)
        dialog.setTitle(getString(R.string.explanation_title))
        dialog.setContent(if (explanation.isEmpty()) "No explanation provided" else explanation)
        dialog.show()
    }

    private fun showErrorState(message: String) {
        questionNumber.text = "Error"
        questionText.text = message
        wrongAnswer.visibility = View.GONE
        correctAnswer.visibility = View.GONE
        option3.visibility = View.GONE
        option4.visibility = View.GONE
        prevButton.isEnabled = false
        nextButton.isEnabled = false
        explanationButton.visibility = View.GONE
    }
}