package com.musketeers_and_me.ai_powered_study_assistant_app.LectureAndNotes

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.style.StyleSpan
import android.util.Log
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.QuizCenter.QuizCenterActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.SmartDigest.ConceptListActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.SmartDigest.ExtractKeyPointsActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.SmartDigest.SummaryActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.Functions
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils
import android.view.Gravity
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBReadOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBWriteOperations


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
    private lateinit var BoldOption: ImageView
    private lateinit var ItalicOption: ImageView
    private lateinit var LAlignOption: ImageView
    private lateinit var CAlignOption: ImageView
    private lateinit var RAlignOption: ImageView
    private var note_id = ""

    private var text_align = 0 // 0 = left, 1 = center, 2 = right

    private var databaseService = FBDataBaseService()
    private var WriteOperations = FBWriteOperations(databaseService)
    private var ReadOperations = FBReadOperations(databaseService)

    private var summary = ""
    private var keyPoints = ""
    private var conceptList = ""


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
        note_id = intent.getStringExtra("note_id").toString()

        keyPointsLayout = findViewById(R.id.key_points_layout)
        conceptListLayout = findViewById(R.id.concept_list_layout)
        noteContent = findViewById(R.id.note_content)
        wordCount = findViewById(R.id.word_count)
        quizLayout = findViewById(R.id.quiz_layout)
        saveButton = findViewById(R.id.save_button)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        BoldOption = findViewById(R.id.bold_button)
        ItalicOption = findViewById(R.id.italic_button)
        LAlignOption = findViewById(R.id.left_align)
        CAlignOption = findViewById(R.id.center_align)
        RAlignOption = findViewById(R.id.right_align)

        ReadOperations.getDigest(note_id) { content, audio, type, s, t, k, c ->
            // Handle the retrieved strings
            noteContent.setText(content)
            summary = s
            keyPoints = k
            conceptList = c
            text_align = t

            if (text_align == 0) {
                noteContent.gravity = Gravity.START
            } else if (text_align == 1) {
                noteContent.gravity = Gravity.CENTER_HORIZONTAL
            } else if (text_align == 2) {
                noteContent.gravity = Gravity.END
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

        summaryLayout.setOnClickListener {
            val intent = Intent(this, SummaryActivity::class.java)
            intent.putExtra("note_id", note_id)
            intent.putExtra("course_title", courseTitle.text.toString())
            intent.putExtra("summary", summary)
            Log.d("TextNoteActivity", "Course Title: ${courseTitle.text}")
            intent.putExtra("note_content", noteContent.text.toString())
            startActivity(intent)
        }


        keyPointsLayout.setOnClickListener {
            val intent = Intent(this, ExtractKeyPointsActivity::class.java)
            intent.putExtra("note_id", note_id)
            intent.putExtra("course_title", courseTitle.text.toString())
            intent.putExtra("note_content", noteContent.text.toString())
            intent.putExtra("key_points", keyPoints)
            startActivity(intent)
//            startActivity(Intent(this,  ExtractKeyPointsActivity::class.java))
        }
        conceptListLayout.setOnClickListener {
            val intent = Intent(this, ConceptListActivity::class.java)
            intent.putExtra("note_id", note_id)
            intent.putExtra("course_title", courseTitle.text.toString())
            intent.putExtra("note_content", noteContent.text.toString())
            intent.putExtra("concept_list", conceptList)
            startActivity(intent)
//            startActivity(Intent(this,  ConceptListActivity::class.java))
        }
        quizLayout.setOnClickListener {
            intent.putExtra("note_id", note_id)
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