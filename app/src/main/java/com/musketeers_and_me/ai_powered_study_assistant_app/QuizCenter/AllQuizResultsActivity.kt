package com.musketeers_and_me.ai_powered_study_assistant_app.QuizCenter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBReadOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Question
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.QuizResult
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AllQuizResultsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: QuizResultsAdapter
    private val databaseService = FBDataBaseService()
    private val readOperations = FBReadOperations(databaseService)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_quiz_results)
        enableEdgeToEdge()

        ToolbarUtils.setupToolbar(this, "All Results", R.drawable.home_logo_top_bar, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "All Results"

        recyclerView = findViewById(R.id.quizResultsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        databaseService.quizzesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val quizResults = mutableListOf<QuizResult>()
                for (quizSnapshot in snapshot.children) {
                    val quizId = quizSnapshot.key ?: continue
                    val title = quizSnapshot.child("title").getValue(String::class.java) ?: "Untitled Quiz"
                    // Fetch questions using FBReadOperations for consistency
                    readOperations.getQuizQuestions(quizId, this@AllQuizResultsActivity) { questions, _ ->
                        val questionCount = questions.size
                        val correctCount = questions.count { it.isCorrect == true }
                        val score = if (questionCount > 0) (correctCount * 100) / questionCount else 0
                        Log.d("AllQuizResultsActivity", "QuizId: $quizId, Questions: $questionCount, Correct: $correctCount, Score: $score%")
                        val quizResult = QuizResult(quizId, title, questionCount, score)
                        quizResults.add(quizResult)
                        // Update adapter only after all quizzes are processed
                        if (quizResults.size == snapshot.childrenCount.toInt()) {
                            adapter = QuizResultsAdapter(quizResults)
                            recyclerView.adapter = adapter
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AllQuizResultsActivity", "Failed to fetch quizzes: ${error.message}")
            }
        })

        findViewById<FrameLayout>(R.id.home_button_container).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}