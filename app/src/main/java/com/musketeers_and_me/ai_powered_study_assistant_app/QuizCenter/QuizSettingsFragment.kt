package com.musketeers_and_me.ai_powered_study_assistant_app.QuizCenter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.musketeers_and_me.ai_powered_study_assistant_app.R


class QuizSettingsFragment : Fragment() {
    private lateinit var questionCountSlider: Slider
    private lateinit var sliderValue: TextView
    private lateinit var generateQuizButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.quiz_settings_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        questionCountSlider = view.findViewById(R.id.questionCountSlider)
        sliderValue = view.findViewById(R.id.sliderValue)
        generateQuizButton = view.findViewById(R.id.generateQuizButton)

        // Setup slider listener
        questionCountSlider.addOnChangeListener { _, value, _ ->
            sliderValue.text = "${value.toInt()} questions"
        }

        // Setup generate quiz button
        generateQuizButton.setOnClickListener {
            val questionCount = questionCountSlider.value.toInt()
            // TODO: Generate quiz with selected number of questions
            (requireActivity() as? QuizCenterActivity)?.let { activity ->
                activity.viewPager.currentItem = 1 // Switch to Take Quiz tab
            }
        }
    }
} 