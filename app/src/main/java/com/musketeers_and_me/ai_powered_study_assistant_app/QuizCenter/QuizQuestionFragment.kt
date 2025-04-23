package com.musketeers_and_me.ai_powered_study_assistant_app.QuizCenter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    
    private var currentQuestionIndex = 0
    private var totalQuestions = 20 // Default value, should be updated when quiz is generated

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

        // Setup navigation buttons
        prevQuestionButton.setOnClickListener {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                updateQuestion()
            }
        }

        nextQuestionButton.setOnClickListener {
            if (currentQuestionIndex < totalQuestions - 1) {
                currentQuestionIndex++
                updateQuestion()
            }
        }

        // Initial question setup
        updateQuestion()
    }

    private fun updateQuestion() {
        // Update question number
        questionNumberText.text = "Question ${currentQuestionIndex + 1}/$totalQuestions"
        
        prevQuestionButton.isEnabled = currentQuestionIndex > 0
        nextQuestionButton.isEnabled = currentQuestionIndex < totalQuestions - 1

        questionText.text = "What is the worst case time complexity of binary search?"
        // Reset radio group selection
        optionsRadioGroup.clearCheck()
    }

    fun setTotalQuestions(count: Int) {
        totalQuestions = count
        currentQuestionIndex = 0
        updateQuestion()
    }
} 