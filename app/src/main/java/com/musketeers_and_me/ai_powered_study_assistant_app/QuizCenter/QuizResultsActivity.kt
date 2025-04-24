package com.musketeers_and_me.ai_powered_study_assistant_app.QuizCenter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
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

    private var currentQuestionIndex = 0
    private var totalQuestions = 20
    private lateinit var userAnswers: HashMap<Int, String>
    private lateinit var correctAnswers: HashMap<Int, String>
    private lateinit var explanations: HashMap<Int, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_results)
        enableEdgeToEdge()

        // Setup toolbar
        ToolbarUtils.setupToolbar(this, "Quiz Results", R.drawable.home_logo_top_bar, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Quiz Results"
        // Get data from intent
        @Suppress("UNCHECKED_CAST")
        userAnswers = intent.getSerializableExtra("userAnswers") as HashMap<Int, String>
        @Suppress("UNCHECKED_CAST")
        correctAnswers = intent.getSerializableExtra("correctAnswers") as HashMap<Int, String>
        @Suppress("UNCHECKED_CAST")
        explanations = intent.getSerializableExtra("explanations") as? HashMap<Int, String> ?: hashMapOf()
        totalQuestions = intent.getIntExtra("totalQuestions", 20)

        // Initialize views
        initializeViews()
        setupClickListeners()
        updateQuestion()
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
        // Update question number
        questionNumber.text = "Question ${currentQuestionIndex + 1}/$totalQuestions"
        
        // Update navigation buttons
        prevButton.isEnabled = currentQuestionIndex > 0
        nextButton.isEnabled = currentQuestionIndex < totalQuestions - 1

        // Update question text
        questionText.text = "What is the worst case time complexity of binary search?"

        // Get user's answer and correct answer
        val userAnswer = userAnswers[currentQuestionIndex] ?: ""
        val correctAnswerText = correctAnswers[currentQuestionIndex] ?: "O(log n)"

        // Show the wrong answer with red X
        wrongAnswer.apply {
            text = "O(1)"
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.cross_circle, 0)
            visibility = if (userAnswer == "O(1)") View.VISIBLE else View.GONE
        }

        // Show the correct answer with green check
        correctAnswer.apply {
            text = correctAnswerText
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_fill, 0, 0, 0)
        }

        // Show other options
        option3.visibility = View.VISIBLE
        option4.visibility = View.VISIBLE
    }

    private fun showExplanationDialog() {
        val explanation = explanations[currentQuestionIndex] ?: "No explanation available for this question."
        val dialog = ExplanationDialog(this)
        dialog.setTitle(getString(R.string.explanation_title))
        dialog.setContent(explanation)
        dialog.show()
    }
} 