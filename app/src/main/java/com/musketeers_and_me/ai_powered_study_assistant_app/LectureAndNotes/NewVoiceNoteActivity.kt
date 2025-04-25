package com.musketeers_and_me.ai_powered_study_assistant_app.LectureAndNotes

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils

class NewVoiceNoteActivity : AppCompatActivity() {
    private lateinit var contentLayout: LinearLayout
    private lateinit var courseTitle: TextView
    private lateinit var courseDescription: TextView
    private lateinit var noteTitle: EditText
    private lateinit var recordingTime: TextView
    private lateinit var recordButton: com.google.android.material.button.MaterialButton
    private lateinit var voiceRecorderText: TextView
    private lateinit var voiceRecorderIcon: ImageView
    private lateinit var saveButton: com.google.android.material.button.MaterialButton
    private lateinit var bottomNavigation: com.google.android.material.bottomnavigation.BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_voice_note)
        // Setup Toolbar
        ToolbarUtils.setupToolbar(this, "New Voice Note", R.drawable.back, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "New Voice Note"

        //findViewById
        contentLayout = findViewById(R.id.content_layout)
        courseTitle = findViewById(R.id.course_title)
        courseDescription = findViewById(R.id.course_description)
        noteTitle = findViewById(R.id.note_title)
        recordingTime = findViewById(R.id.recording_time)
        recordButton = findViewById(R.id.record_button)
        saveButton = findViewById(R.id.save_button)
        bottomNavigation = findViewById(R.id.bottom_navigation)

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