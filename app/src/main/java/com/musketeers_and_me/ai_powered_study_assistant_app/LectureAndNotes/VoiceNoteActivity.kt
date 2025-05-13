package com.musketeers_and_me.ai_powered_study_assistant_app.LectureAndNotes

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBReadOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.QuizCenter.QuizCenterActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.SmartDigest.ConceptListActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.SmartDigest.ExtractKeyPointsActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.SmartDigest.SummaryActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils
import java.io.IOException

class VoiceNoteActivity : AppCompatActivity() {
    private lateinit var contentLayout: LinearLayout
    private lateinit var noteTitle: TextView
    private lateinit var audioSeekbar: SeekBar
    private lateinit var playButton: ImageView
    private lateinit var extraAudioIcon: ImageView
    private lateinit var transcriptionContent: EditText
    private lateinit var summaryLayout: LinearLayout
    private lateinit var keyPointsLayout: LinearLayout
    private lateinit var conceptListLayout: LinearLayout
    private lateinit var quizLayout: LinearLayout
    private lateinit var saveButton: MaterialButton
    private lateinit var bottomNavigation: BottomNavigationView
    private var mediaPlayer: MediaPlayer? = null

    private val databaseService = FBDataBaseService()
    private val ReadOperations = FBReadOperations(databaseService)

    private var summary = ""
    private var keyPoints = ""
    private var conceptList = ""
    private var audioUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_voice_note)
        ToolbarUtils.setupToolbar(this, "Voice Note", R.drawable.back, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Voice Note"

        contentLayout = findViewById(R.id.content_layout)
        noteTitle = findViewById(R.id.note_title)
        audioSeekbar = findViewById(R.id.audio_seekbar)
        playButton = findViewById(R.id.play_button)
        extraAudioIcon = findViewById(R.id.extra_audio_icon)
        transcriptionContent = findViewById(R.id.transcription_content)
        summaryLayout = findViewById(R.id.summary_layout)
        keyPointsLayout = findViewById(R.id.key_points_layout)
        conceptListLayout = findViewById(R.id.concept_list_layout)
        quizLayout = findViewById(R.id.quiz_layout)
        saveButton = findViewById(R.id.save_button)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        val isEditable = intent.getBooleanExtra("is_editable", false)

        if (!isEditable) {
            transcriptionContent.isEnabled = false
            saveButton.visibility = View.GONE
        }

        val note_id = intent.getStringExtra("note_id") ?: ""
        noteTitle.text = intent.getStringExtra("note_title") ?: ""

        ReadOperations.getDigest(note_id) { content, audio, type, s, t, k, c ->
            // Handle the retrieved strings
            transcriptionContent.setText(content)
            audioUrl = audio
            summary = s
            keyPoints = k
            conceptList = c
        }

        playButton.setOnClickListener {
            if (audioUrl != "") {
                playAudio(audioUrl)
            } else {
                Toast.makeText(this, "No audio URL provided", Toast.LENGTH_SHORT).show()
            }
        }

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
        }
        conceptListLayout.setOnClickListener {
            val intent = Intent(this, ConceptListActivity::class.java)
            intent.putExtra("course_title", noteTitle.text.toString())
            intent.putExtra("note_content", transcriptionContent.text.toString())
            startActivity(intent)
        }
        quizLayout.setOnClickListener {
            val intent = Intent(this, QuizCenterActivity::class.java)
            intent.putExtra("note_id", note_id)
            intent.putExtra("note_content", transcriptionContent.text.toString())
            intent.putExtra("course_title", noteTitle.text.toString())
            startActivity(intent)
        }

        saveButton.setOnClickListener {
            // TODO: Implement saving updated transcription to Firebase
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<FrameLayout>(R.id.home_button_container).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun playAudio(url: String) {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            playButton.setImageResource(R.drawable.play_audio)
            Log.d("VoiceNoteActivity", "Audio paused")
            return
        }

        mediaPlayer?.release() // Release any existing MediaPlayer
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(url)
                setOnPreparedListener {
                    start()
                    playButton.setImageResource(R.drawable.pause)
                    audioSeekbar.max = duration
                    Log.d("VoiceNoteActivity", "Audio started, duration: $duration ms")
                }
                setOnCompletionListener {
                    playButton.setImageResource(R.drawable.play_audio)
                    audioSeekbar.progress = 0
                    Log.d("VoiceNoteActivity", "Audio completed")
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("VoiceNoteActivity", "MediaPlayer error: what=$what, extra=$extra")
                    Toast.makeText(this@VoiceNoteActivity, "Error playing audio", Toast.LENGTH_SHORT).show()
                    playButton.setImageResource(R.drawable.play_audio)
                    true
                }
                prepareAsync() // Use async preparation for network streams
            } catch (e: IOException) {
                Log.e("VoiceNoteActivity", "IOException setting data source: ${e.message}")
                Toast.makeText(this@VoiceNoteActivity, "Error loading audio", Toast.LENGTH_SHORT).show()
                playButton.setImageResource(R.drawable.play_audio)
            } catch (e: IllegalStateException) {
                Log.e("VoiceNoteActivity", "IllegalStateException: ${e.message}")
                Toast.makeText(this@VoiceNoteActivity, "Error initializing audio", Toast.LENGTH_SHORT).show()
                playButton.setImageResource(R.drawable.play_audio)
            }
        }

        // Update SeekBar
        val handler = android.os.Handler(mainLooper)
        val updateSeekBar = object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    audioSeekbar.progress = it.currentPosition
                    handler.postDelayed(this, 100)
                }
            }
        }
        handler.post(updateSeekBar)

        audioSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                    Log.d("VoiceNoteActivity", "Seek to: $progress ms")
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
        playButton.setImageResource(R.drawable.play_audio)
        Log.d("VoiceNoteActivity", "Activity paused, audio paused")
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        Log.d("VoiceNoteActivity", "Activity destroyed, MediaPlayer released")
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