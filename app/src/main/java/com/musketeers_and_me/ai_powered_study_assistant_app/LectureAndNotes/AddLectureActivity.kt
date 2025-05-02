package com.musketeers_and_me.ai_powered_study_assistant_app.LectureAndNotes

import NoteAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils

class AddLectureActivity : AppCompatActivity() {

    private lateinit var courseTitle: TextView
    private lateinit var courseDescription: TextView
    private lateinit var searchBar: EditText
    private lateinit var uploadImageButton: TextView
    private lateinit var uploadIcon: ImageView
    private lateinit var textNotesLabel: TextView
    private lateinit var textNotesCount: TextView
    private lateinit var addTextNote: ImageView
    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var voiceNotesTitle: TextView
    private lateinit var voiceNotesCount: TextView
    private lateinit var addVoiceNote: ImageView
    private lateinit var voiceNotesRecyclerView: RecyclerView
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_lecture)
        enableEdgeToEdge()

        // Setup Toolbar
        ToolbarUtils.setupToolbar(this, "Add Lecture", R.drawable.back, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Lecture"

        // Initialize views
        courseTitle = findViewById(R.id.course_title)
        courseDescription = findViewById(R.id.course_description)
        searchBar = findViewById(R.id.search_bar)
        uploadImageButton = findViewById(R.id.upload_image_button)
        uploadIcon = findViewById(R.id.upload_icon)
        textNotesLabel = findViewById(R.id.text_notes_label)
        textNotesCount = findViewById(R.id.text_notes_count)
        addTextNote = findViewById(R.id.add_text_note)
        notesRecyclerView = findViewById(R.id.notes_recycler_view)
        voiceNotesTitle = findViewById(R.id.voice_notes_title)
        voiceNotesCount = findViewById(R.id.voice_notes_count)
        addVoiceNote = findViewById(R.id.add_voice_note)
        voiceNotesRecyclerView = findViewById(R.id.voice_notes_recycler_view)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        // Set RecyclerViews
        notesRecyclerView.layoutManager = LinearLayoutManager(this)
        voiceNotesRecyclerView.layoutManager = LinearLayoutManager(this)

        val sampleTextNotes = listOf(
            NoteItem("Text Note 1", "1 day ago", NoteType.TEXT),
            NoteItem("Text Note 2", "2 days ago", NoteType.TEXT)
        )
        val sampleVoiceNotes = listOf(
            NoteItem("Voice Note 1", "3 hours ago", NoteType.VOICE),
            NoteItem("Voice Note 2", "5 days ago", NoteType.VOICE)
        )

        // Adapters with note type filtering
        notesRecyclerView.adapter = NoteAdapter(sampleTextNotes) { note ->
            startActivity(Intent(this, TextNoteActivity::class.java))
        }

        voiceNotesRecyclerView.adapter = NoteAdapter(sampleVoiceNotes) { note ->
            startActivity(Intent(this, VoiceNoteActivity::class.java))
        }

        // Click listeners
        addTextNote.setOnClickListener {
            startActivity(Intent(this, NewTextNoteActivity::class.java))
        }
        addVoiceNote.setOnClickListener {
            startActivity(Intent(this, NewVoiceNoteActivity::class.java))
        }
        uploadIcon.setOnClickListener {
            startActivity(Intent(this, UploadImageActivity::class.java))
        }

        // Home button handling
        findViewById<FrameLayout>(R.id.home_button_container).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        // Handle back navigation

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
