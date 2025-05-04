package com.musketeers_and_me.ai_powered_study_assistant_app.LectureAndNotes

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.QuizCenter.QuizCenterActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.SmartDigest.ConceptListActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.SmartDigest.ExtractKeyPointsActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.SmartDigest.SummaryActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils

class TextNoteActivity : AppCompatActivity() {
    private lateinit var scrollView: ScrollView
    private lateinit var contentLayout: LinearLayout
    private lateinit var summaryLayout: LinearLayout
    private lateinit var courseTitle: TextView
    private lateinit var keyPointsLayout: LinearLayout
    private lateinit var conceptListLayout: LinearLayout
    private lateinit var noteContent: EditText
    private lateinit var wordCount: TextView
    private lateinit var quizLayout: LinearLayout
    private lateinit var saveButton: MaterialButton
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_text_note)
        // Setup Toolbar
        ToolbarUtils.setupToolbar(this, "Text Note", R.drawable.back, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Text Note"

        // Initialize views
        scrollView = findViewById(R.id.scroll_view)
        contentLayout = findViewById(R.id.content_layout)
        summaryLayout = findViewById(R.id.summary_linear_layout)
        courseTitle = findViewById(R.id.course_title)
        courseTitle.text = intent.getStringExtra("course_title").toString()
        keyPointsLayout = findViewById(R.id.key_points_layout)
        conceptListLayout = findViewById(R.id.concept_list_layout)
        noteContent = findViewById(R.id.note_content)
        wordCount = findViewById(R.id.word_count)
        quizLayout = findViewById(R.id.quiz_layout)
        saveButton = findViewById(R.id.save_button)
        bottomNavigation = findViewById(R.id.bottom_navigation)


        summaryLayout.setOnClickListener {
            val intent = Intent(this, SummaryActivity::class.java)
            intent.putExtra("course_title", courseTitle.text.toString())
            intent.putExtra("note_content", noteContent.text.toString())
            startActivity(intent)
        }


        keyPointsLayout.setOnClickListener {
            val intent = Intent(this, ExtractKeyPointsActivity::class.java)
            intent.putExtra("course_title", courseTitle.text.toString())
            intent.putExtra("note_content", noteContent.text.toString())
            startActivity(intent)
//            startActivity(Intent(this,  ExtractKeyPointsActivity::class.java))
        }
        conceptListLayout.setOnClickListener {
            val intent = Intent(this, ConceptListActivity::class.java)
            intent.putExtra("course_title", courseTitle.text.toString())
            intent.putExtra("note_content", noteContent.text.toString())
            startActivity(intent)
//            startActivity(Intent(this,  ConceptListActivity::class.java))
        }
        quizLayout.setOnClickListener {
            startActivity(Intent(this,  QuizCenterActivity::class.java))
        }

        findViewById<FrameLayout>(R.id.home_button_container).setOnClickListener {
            // Create intent for MainActivity
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}