package com.musketeers_and_me.ai_powered_study_assistant_app.LectureAndNotes

import NoteAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.OfflineFirstDataManager
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import android.widget.Toast
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddLectureActivity : AppCompatActivity() {
    private val TAG = "AddLectureActivity"
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
    private lateinit var dataManager: OfflineFirstDataManager

    private var courseId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_lecture)
        enableEdgeToEdge()

        // Initialize data manager
        dataManager = OfflineFirstDataManager.getInstance(this)

        ToolbarUtils.setupToolbar(this, "Add Lecture", R.drawable.back, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Lecture"

        val ct = intent.getStringExtra("course_title").toString()
        courseId = intent.getStringExtra("course_id").toString()

        initializeViews()


        setupRecyclerViews()
        loadNotes()

        searchBar.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                textNoteAdapter?.filter(query)
                voiceNoteAdapter?.filter(query)
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })


        setupClickListeners()
    }

    private fun initializeViews() {
        courseTitle = findViewById(R.id.course_title)
        courseTitle.text = intent.getStringExtra("course_title")
        courseDescription = findViewById(R.id.course_description)
        courseDescription.text = intent.getStringExtra("course_description")
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
    }

    private fun setupRecyclerViews() {
        notesRecyclerView.layoutManager = LinearLayoutManager(this)
        voiceNotesRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private var textNoteAdapter: NoteAdapter? = null
    private var voiceNoteAdapter: NoteAdapter? = null

    private fun loadNotes() {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val notes = dataManager.getNotes(courseId)
                    val textNotes = notes.filter { it.type == "text" }
                    val voiceNotes = notes.filter { it.type == "voice" }

                    withContext(Dispatchers.Main) {
                        textNoteAdapter = NoteAdapter(textNotes) { note ->
                            val intent = Intent(this@AddLectureActivity, TextNoteActivity::class.java)
                            intent.putExtra("note_title", note.title)
                            intent.putExtra("note_id", note.note_id)
                            intent.putExtra("is_editable", true)
                            startActivity(intent)
                        }

                        voiceNoteAdapter = NoteAdapter(voiceNotes) { note ->
                            val intent = Intent(this@AddLectureActivity, VoiceNoteActivity::class.java)
                            intent.putExtra("note_title", note.title)
                            intent.putExtra("note_id", note.note_id)
                            startActivity(intent)
                        }

                        notesRecyclerView.adapter = textNoteAdapter
                        voiceNotesRecyclerView.adapter = voiceNoteAdapter

                        textNotesCount.text = "(${textNotes.size})"
                        voiceNotesCount.text = "(${voiceNotes.size})"
//                        Toast.makeText(this@AddLectureActivity, "Notes loaded successfully: ${voiceNotesCount.text}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading notes", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddLectureActivity, "Error loading notes: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun setupClickListeners() {
        addTextNote.setOnClickListener {
            val intent = Intent(this, NewTextNoteActivity::class.java)
            intent.putExtra("course_title", courseTitle.text.toString())
            intent.putExtra("course_id", courseId)
            intent.putExtra("course_description", courseDescription.text.toString())
            startActivity(intent)
        }

        addVoiceNote.setOnClickListener {
            val intent = Intent(this, NewVoiceNoteActivity::class.java)
            intent.putExtra("course_title", courseTitle.text.toString())
            intent.putExtra("course_id", courseId)
            startActivity(intent)
        }

        uploadIcon.setOnClickListener {
            val intent = Intent(this, UploadImageActivity::class.java)
            intent.putExtra("course_title", courseTitle.text.toString())
            intent.putExtra("course_id", courseId)
            intent.putExtra("course_description", courseDescription.text.toString())
            startActivity(intent)
        }

        findViewById<FrameLayout>(R.id.home_button_container).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onResume() {
        super.onResume()
        dataManager = OfflineFirstDataManager.getInstance(this)

        initializeViews()


        setupRecyclerViews()
        loadNotes()


//        setupRecyclerViews()
//        loadNotes()
    }
}