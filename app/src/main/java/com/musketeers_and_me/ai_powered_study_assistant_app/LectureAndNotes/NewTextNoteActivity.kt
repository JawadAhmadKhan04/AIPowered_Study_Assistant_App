package com.musketeers_and_me.ai_powered_study_assistant_app.LectureAndNotes

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.OfflineFirstDataManager
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.Functions
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils
import kotlinx.coroutines.launch

class NewTextNoteActivity : AppCompatActivity() {
    private lateinit var scrollView: ScrollView
    private lateinit var contentLayout: LinearLayout
    private lateinit var courseTitle: TextView
    private lateinit var courseDescription: TextView
    private lateinit var noteTitle: EditText
    private lateinit var noteContent: EditText
    private lateinit var wordCount: TextView
    private lateinit var saveButton: com.google.android.material.button.MaterialButton
    private lateinit var bottomNavigation: com.google.android.material.bottomnavigation.BottomNavigationView
    private lateinit var BoldOption: ImageView
    private lateinit var ItalicOption: ImageView
    private lateinit var LAlignOption: ImageView
    private lateinit var CAlignOption: ImageView
    private lateinit var RAlignOption: ImageView

    private lateinit var dataManager: OfflineFirstDataManager
    private var course_id = ""
    private var text_align = 0 // 0 = left, 1 = center, 2 = right

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_text_note)

        // Initialize data manager
        dataManager = OfflineFirstDataManager.getInstance(this)

        // Setup Toolbar
        ToolbarUtils.setupToolbar(this, "New Text Note", R.drawable.back, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "New Text Note"

        //findViewById
        scrollView = findViewById(R.id.scroll_view)
        contentLayout = findViewById(R.id.content_layout)
        courseTitle = findViewById(R.id.course_title)
        courseTitle.text = intent.getStringExtra("course_title").toString()
        course_id = intent.getStringExtra("course_id").toString()
        courseDescription = findViewById(R.id.course_description)
        noteTitle = findViewById(R.id.note_title)
        noteContent = findViewById(R.id.note_content)
        wordCount = findViewById(R.id.word_count)
        saveButton = findViewById(R.id.save_button)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        BoldOption = findViewById(R.id.bold_button)
        ItalicOption = findViewById(R.id.italic_button)
        LAlignOption = findViewById(R.id.left_align)
        CAlignOption = findViewById(R.id.center_align)
        RAlignOption = findViewById(R.id.right_align)

        saveButton.setOnClickListener {
            val noteTitleText = noteTitle.text.toString()
            val noteContentText = noteContent.text.toString()

            if (noteTitleText.isNotEmpty() && noteContentText.isNotEmpty()) {
                lifecycleScope.launch {
                    try {
                        // Save note locally first
                        dataManager.saveNote(
                            courseId = course_id,
                            title = noteTitleText,
                            content = noteContentText,
                            type = "text",
                            tag = text_align
                        )
                        
                        Toast.makeText(this@NewTextNoteActivity, "Note saved successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } catch (e: Exception) {
                        Log.e("NewTextNoteActivity", "Error saving note", e)
                        Toast.makeText(this@NewTextNoteActivity, "Error saving note: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        BoldOption.setOnClickListener {
            val start = noteContent.selectionStart
            val end = noteContent.selectionEnd

            if (start < end) {
                val spannable = noteContent.text
                val existingSpans = spannable.getSpans(start, end, StyleSpan::class.java)

                var isBold = false
                for (span in existingSpans) {
                    if (span.style == Typeface.BOLD) {
                        spannable.removeSpan(span)
                        isBold = true
                    }
                }

                if (!isBold) {
                    spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                Log.d("NewTextNoteActivity", Functions.getHtmlFromEditText(noteContent.text))
            } else {
                Toast.makeText(this, "Please select text to bold", Toast.LENGTH_SHORT).show()
            }
        }

        ItalicOption.setOnClickListener {
            val start = noteContent.selectionStart
            val end = noteContent.selectionEnd

            if (start < end) {
                val spannable = noteContent.text
                val existingSpans = spannable.getSpans(start, end, StyleSpan::class.java)

                var isItalic = false
                for (span in existingSpans) {
                    if (span.style == Typeface.ITALIC) {
                        spannable.removeSpan(span)
                        isItalic = true
                    }
                }

                if (!isItalic) {
                    spannable.setSpan(
                        StyleSpan(Typeface.ITALIC),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                Log.d("NewTextNoteActivity", Functions.getHtmlFromEditText(noteContent.text))
            } else {
                Toast.makeText(this, "Please select text to italicize", Toast.LENGTH_SHORT).show()
            }
        }

        LAlignOption.setOnClickListener {
            noteContent.gravity = Gravity.START
            text_align = 0
        }

        CAlignOption.setOnClickListener {
            noteContent.gravity = Gravity.CENTER_HORIZONTAL
            text_align = 1
        }

        RAlignOption.setOnClickListener {
            noteContent.gravity = Gravity.END
            text_align = 2
        }

        findViewById<FrameLayout>(R.id.home_button_container).setOnClickListener {
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