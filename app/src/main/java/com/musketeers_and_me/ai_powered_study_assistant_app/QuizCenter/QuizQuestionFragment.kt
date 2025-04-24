package com.musketeers_and_me.ai_powered_study_assistant_app.QuizCenter

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.musketeers_and_me.ai_powered_study_assistant_app.R

class QuizQuestionFragment : Fragment() {
    private lateinit var questionNumberText: TextView
    private lateinit var questionText: TextView
    private lateinit var optionsRadioGroup: RadioGroup
    private lateinit var prevQuestionButton: MaterialButton
    private lateinit var nextQuestionButton: MaterialButton
    private lateinit var seeResultsButton: MaterialButton
    
    private var currentQuestionIndex = 0
    private var totalQuestions = 3 // Default value, should be updated when quiz is generated
    private val userAnswers = mutableMapOf<Int, String>() // Store user answers
    private val correctAnswers = mutableMapOf<Int, String>() // Store correct answers

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.quiz_question_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        questionNumberText = view.findViewById(R.id.questionNumberText)
        questionText = view.findViewById(R.id.questionText)
        optionsRadioGroup = view.findViewById(R.id.optionsRadioGroup)
        prevQuestionButton = view.findViewById(R.id.prevQuestionButton)
        nextQuestionButton = view.findViewById(R.id.nextQuestionButton)
        seeResultsButton = view.findViewById(R.id.seeResultsButton)

        // Setup navigation buttons
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

        // Setup radio group listener
        optionsRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId != -1) {
                val radioButton = group.findViewById<RadioButton>(checkedId)
                userAnswers[currentQuestionIndex] = radioButton.text.toString()
            }
        }

        // Initial question setup
        updateQuestion()
        
        // For demo purposes, set some correct answers
        correctAnswers[0] = "O(log n)"
    }

    private fun saveCurrentAnswer() {
        val selectedId = optionsRadioGroup.checkedRadioButtonId
        if (selectedId != -1) {
            val radioButton = optionsRadioGroup.findViewById<RadioButton>(selectedId)
            userAnswers[currentQuestionIndex] = radioButton.text.toString()
        }
    }

    private fun updateQuestion() {
        // Update question number
        questionNumberText.text = "Question ${currentQuestionIndex + 1}/$totalQuestions"
        
        // Update navigation buttons state
        prevQuestionButton.isEnabled = currentQuestionIndex > 0
        nextQuestionButton.isEnabled = currentQuestionIndex < totalQuestions - 1
        
        // Show/hide See Results button on last question
        seeResultsButton.visibility = if (currentQuestionIndex == totalQuestions - 1) View.VISIBLE else View.GONE
        nextQuestionButton.visibility = if (currentQuestionIndex == totalQuestions - 1) View.GONE else View.VISIBLE

        // Set question text
        questionText.text = "What is the worst case time complexity of binary search?"
        
        // Restore user's previous answer if it exists
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
    }

    private fun startQuizResults() {
        val intent = Intent(requireContext(), QuizResultsActivity::class.java).apply {
            putExtra("userAnswers", HashMap(userAnswers))
            putExtra("correctAnswers", HashMap(correctAnswers))
            putExtra("totalQuestions", totalQuestions)
        }
        startActivity(intent)
    }

    fun setTotalQuestions(count: Int) {
        totalQuestions = count
        currentQuestionIndex = 0
        updateQuestion()
    }
} 