package com.musketeers_and_me.ai_powered_study_assistant_app.QuizCenter

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.QuizResult
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBReadOperations
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
                    val questions = quizSnapshot.child("questions").children
                    val questionCount = questions.count()
                    var correctCount = 0
                    for (question in questions) {
                        val isCorrect = question.child("isCorrect").getValue(Boolean::class.java) ?: false
                        if (isCorrect) correctCount++
                    }
                    val score = if (questionCount > 0) (correctCount * 100) / questionCount else 0
                    // Include quizId in QuizResult
                    quizResults.add(QuizResult(quizId, title, questionCount, score))
                }
                adapter = QuizResultsAdapter(quizResults)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
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