package com.musketeers_and_me.ai_powered_study_assistant_app.LectureAndNotes

import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
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
import com.musketeers_and_me.ai_powered_study_assistant_app.R
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
        audioSeekbar = findViewById(R.id.audio_seekbar)
        playButton = findViewById(R.id.play_button)
        extraAudioIcon = findViewById(R.id.extra_audio_icon)
        transcriptionContent = findViewById(R.id.transcription_content)
        summaryLayout = findViewById(R.id.summary_layout)
        summaryLinearLayout = findViewById(R.id.summary_linear_layout)
        keyPointsLayout = findViewById(R.id.key_points_layout)
        conceptListLayout = findViewById(R.id.concept_list_layout)
        quizLayout = findViewById(R.id.quiz_layout)
        saveButton = findViewById(R.id.save_button)
        bottomNavigation = findViewById(R.id.bottom_navigation)


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