package com.musketeers_and_me.ai_powered_study_assistant_app.QuizCenter

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBWriteOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.WebApis
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import org.json.JSONException
import org.json.JSONObject

class QuizSettingsFragment : Fragment() {
    private lateinit var questionCountSlider: Slider
    private lateinit var sliderValue: TextView
    private lateinit var generateQuizButton: MaterialButton
    private val webApis = WebApis()
    private val databaseService = FBDataBaseService()
    private val writeOperations = FBWriteOperations(databaseService)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.quiz_settings_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        questionCountSlider = view.findViewById(R.id.questionCountSlider)
        sliderValue = view.findViewById(R.id.sliderValue)
        generateQuizButton = view.findViewById(R.id.generateQuizButton)

        questionCountSlider.addOnChangeListener { _, value, _ ->
            sliderValue.text = "${value.toInt()} questions"
        }

        generateQuizButton.setOnClickListener {
            val questionCount = questionCountSlider.value.toInt()
            val noteId = requireActivity().intent.getStringExtra("note_id") ?: run {
                Toast.makeText(requireContext(), "Error: Note ID is missing", Toast.LENGTH_SHORT).show()
                Log.e("QuizSettingsFragment", "Note ID is null or empty")
                return@setOnClickListener
            }
            val noteContent = requireActivity().intent.getStringExtra("note_content") ?: run {
                Toast.makeText(requireContext(), "Error: Note content is missing", Toast.LENGTH_SHORT).show()
                Log.e("QuizSettingsFragment", "Note content is null or empty")
                return@setOnClickListener
            }
            val courseTitle = requireActivity().intent.getStringExtra("course_title") ?: run {
                Toast.makeText(requireContext(), "Error: Course title is missing", Toast.LENGTH_SHORT).show()
                Log.e("QuizSettingsFragment", "Course title is blank")
                return@setOnClickListener
            }

            if (questionCount <= 0) {
                Toast.makeText(requireContext(), "Error: Select at least 1 question", Toast.LENGTH_SHORT).show()
                Log.e("QuizSettingsFragment", "Invalid question count: $questionCount")
                return@setOnClickListener
            }
            if (noteContent.isBlank()) {
                Toast.makeText(requireContext(), "Error: Note content is empty", Toast.LENGTH_SHORT).show()
                Log.e("QuizSettingsFragment", "Note content is blank")
                return@setOnClickListener
            }
            if (courseTitle.isBlank()) {
                Toast.makeText(requireContext(), "Error: Course title is empty", Toast.LENGTH_SHORT).show()
                Log.e("QuizSettingsFragment", "Course title is blank")
                return@setOnClickListener
            }

            Log.d("QuizSettingsFragment", "Generating quiz: noteId=$noteId, questionCount=$questionCount, courseTitle=$courseTitle")
            Log.d("QuizSettingsFragment", "Note content: $noteContent")

            webApis.generateQuiz(requireContext(), noteContent, courseTitle, questionCount) { result ->
                if (result != null) {
                    Log.d("QuizSettingsFragment", "Backend response: $result")
                    try {
                        val jsonObject = JSONObject(result)
                        val quizArray = jsonObject.getJSONArray("quiz")
                        val questions = mutableListOf<Map<String, Any>>()
                        for (i in 0 until quizArray.length()) {
                            val q = quizArray.getJSONObject(i)
                            val options = mapOf(
                                "A" to q.getString("A"),
                                "B" to q.getString("B"),
                                "C" to q.getString("C"),
                                "D" to q.getString("D")
                            )
                            val questionData = mapOf(
                                "question" to q.getString("question"),
                                "options" to options,
                                "correctAnswer" to q.getString("answer"),
                                "explanation" to q.getString("explanation"),
                                "isAttempted" to false,
                                "isCorrect" to false,
                                "selectedAnswer" to ""
                            )
                            questions.add(questionData)
                        }

                        Log.d("QuizSettingsFragment", "Parsed questions: $questions")

                        writeOperations.saveQuiz(
                            noteId = noteId,
                            title = "$courseTitle Quiz",
                            questions = questions,
                            onSuccess = { quizId ->
                                requireActivity().runOnUiThread {
                                    Toast.makeText(requireContext(), "Quiz generated successfully!", Toast.LENGTH_SHORT).show()
                                    Log.d("QuizSettingsFragment", "Quiz saved with ID: $quizId")
                                    (requireActivity() as? QuizCenterActivity)?.let { activity ->
                                        activity.viewPager.setCurrentItem(1, false)
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            val adapter = activity.viewPager.adapter as? QuizCenterPagerAdapter
                                            val quizQuestionFragment = adapter?.createFragment(1) as? QuizQuestionFragment
                                            if (quizQuestionFragment != null) {
                                                Log.d("QuizSettingsFragment", "Calling setQuizData with quizId: $quizId, questionCount: $questionCount")
                                                quizQuestionFragment.setQuizData(quizId, questionCount)
                                                activity.viewPager.currentItem = 1
                                            } else {
                                                Log.e("QuizSettingsFragment", "QuizQuestionFragment could not be created")
                                                Toast.makeText(requireContext(), "Error: Unable to load quiz page", Toast.LENGTH_SHORT).show()
                                            }
                                        }, 500)
                                    }
                                }
                            },
                            onFailure = { e ->
                                requireActivity().runOnUiThread {
                                    Toast.makeText(requireContext(), "Failed to save quiz: ${e?.message}", Toast.LENGTH_SHORT).show()
                                    Log.e("QuizSettingsFragment", "Failed to save quiz: ${e?.message}", e)
                                }
                            }
                        )
                    } catch (e: JSONException) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Error parsing quiz data: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e("QuizSettingsFragment", "JSON parsing error: ${e.message}", e)
                        }
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Failed to generate quiz: No response from server", Toast.LENGTH_SHORT).show()
                        Log.e("QuizSettingsFragment", "Quiz generation failed: Result is null")
                    }
                }
            }
        }
    }
}