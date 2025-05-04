package com.musketeers_and_me.ai_powered_study_assistant_app.LectureAndNotes

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.QuizCenter.QuizCenterActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.SmartDigest.ConceptListActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.SmartDigest.ExtractKeyPointsActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.SmartDigest.SummaryActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils

class VoiceNoteActivity : AppCompatActivity() {
    private lateinit var contentLayout: LinearLayout
    private lateinit var noteTitle: TextView
    private lateinit var audioSeekbar: SeekBar
    private lateinit var playButton: ImageView
    private lateinit var extraAudioIcon: ImageView
    private lateinit var transcriptionContent: EditText
    private lateinit var summaryLayout: LinearLayout
    private lateinit var summaryLinearLayout: LinearLayout
    private lateinit var keyPointsLayout: LinearLayout
    private lateinit var conceptListLayout: LinearLayout
    private lateinit var quizLayout: LinearLayout
    private lateinit var saveButton: MaterialButton
    private lateinit var bottomNavigation: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_voice_note)
        // Setup Toolbar
        ToolbarUtils.setupToolbar(this, "Voice Note", R.drawable.back, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Voice Note"

        // Initialize views
        contentLayout = findViewById(R.id.content_layout)
        noteTitle = findViewById(R.id.note_title)
        noteTitle.text = intent.getStringExtra("course_title").toString()
        audioSeekbar = findViewById(R.id.audio_seekbar)
        playButton = findViewById(R.id.play_button)
        extraAudioIcon = findViewById(R.id.extra_audio_icon)
        transcriptionContent = findViewById(R.id.transcription_content)
        summaryLayout = findViewById(R.id.summary_linear_layout)
        keyPointsLayout = findViewById(R.id.key_points_layout)
        conceptListLayout = findViewById(R.id.concept_list_layout)
        quizLayout = findViewById(R.id.quiz_layout)
        saveButton = findViewById(R.id.save_button)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        summaryLayout.setOnClickListener {
            val intent = Intent(this, SummaryActivity::class.java)
            intent.putExtra("course_title", noteTitle.text.toString())
            intent.putExtra("note_content", transcriptionContent.text.toString())
            startActivity(intent)
        }
        keyPointsLayout.setOnClickListener {
            val intent = Intent(this, ExtractKeyPointsActivity::class.java)
            intent.putExtra("course_title", noteTitle.text.toString())
            intent.putExtra("note_content", transcriptionContent.text.toString())
            startActivity(intent)
//            startActivity(Intent(this,  ExtractKeyPointsActivity::class.java))
        }
        conceptListLayout.setOnClickListener {
            val intent = Intent(this, ConceptListActivity::class.java)
            intent.putExtra("course_title", noteTitle.text.toString())
            intent.putExtra("note_content", transcriptionContent.text.toString())
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