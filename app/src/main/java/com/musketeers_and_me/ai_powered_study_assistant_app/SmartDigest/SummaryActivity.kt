package com.musketeers_and_me.ai_powered_study_assistant_app.SmartDigest

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.WebApis
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.Functions
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils
import org.json.JSONException
import org.json.JSONObject

class SummaryActivity : AppCompatActivity() {

    private val webApis = WebApis()  // ✅ Initialize the class here
    private lateinit var CourseTitle: TextView
    private lateinit var WordCount: TextView
    private lateinit var CopyIcon: ImageView
    private lateinit var DownloadButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_summary)
        // Setup Toolbar
        ToolbarUtils.setupToolbar(this, "Summary", R.drawable.back, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Summary"
        findViewById<FrameLayout>(R.id.home_button_container).setOnClickListener {
            // Create intent for MainActivity
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        var summary = findViewById<TextView>(R.id.summary_content)
        WordCount = findViewById(R.id.word_count)
        CourseTitle = findViewById(R.id.course_title)
        CourseTitle.text = intent.getStringExtra("course_title").toString()
        CopyIcon = findViewById(R.id.copy_icon)
        DownloadButton = findViewById(R.id.download_icon)
        val data_text = intent.getStringExtra("note_content").toString()

        DownloadButton.setOnClickListener{
            // Handle download button click
//            Functions.saveTextAsPdf(this, summary.text.toString())
            Toast.makeText(this, "Code is commented out", Toast.LENGTH_SHORT).show()
        }

        CopyIcon.setOnClickListener{
            // Handle copy icon click
            Functions.copyToClipboard(this, summary.text.toString())
//            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        var regen = findViewById<MaterialButton>(R.id.regenerate)
        regen.setOnClickListener {
            // Handle regenerate button click
//            Toast.makeText(this, "Regenerate button clicked", Toast.LENGTH_SHORT).show()
//            val summary_text = summary.text.toString()
            webApis.getSummary(this, data_text, CourseTitle.text.toString()) { result ->
                Log.d("WebApis", "Result from aagay se: $result")
                if (result != null) {
                    try {
                        val jsonObject = JSONObject(result)
                        val message = jsonObject.getString("summary")
                        runOnUiThread {
                            summary.text = message
                            WordCount.text = Functions.countWords(message)
                        }
                    } catch (e: JSONException) {
                        Log.e("WebApis", "JSON parsing error: ${e.message}")
                    }
                }
            }

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