package com.musketeers_and_me.ai_powered_study_assistant_app.QuizCenter

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils

class AllQuizResultsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: QuizResultsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_quiz_results)
        enableEdgeToEdge()

        // Setup toolbar
        ToolbarUtils.setupToolbar(this, "All Results", R.drawable.home_logo_top_bar, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "All Results"

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.quizResultsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Create dummy data
        val dummyResults = listOf(
            QuizResult("Data Structures Quiz", 20, 45),
            QuizResult("Algorithms Quiz", 15, 80),
            QuizResult("Programming Basics", 25, 95)
        )

        // Set up adapter with dummy data
        adapter = QuizResultsAdapter(dummyResults)
        recyclerView.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 