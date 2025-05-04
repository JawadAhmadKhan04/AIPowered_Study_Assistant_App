package com.musketeers_and_me.ai_powered_study_assistant_app.SmartDigest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.WebApis
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.Functions
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils
import org.json.JSONException
import org.json.JSONObject

class ConceptListActivity : AppCompatActivity() {

    private val webApis = WebApis()  // ✅ Initialize the class here
    private lateinit var CourseTitle: TextView
    private lateinit var WordCount: TextView
    private lateinit var CopyIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_concept_list)

        // Setup Toolbar
        ToolbarUtils.setupToolbar(this, "Concept List", R.drawable.back, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Concept List"
        findViewById<FrameLayout>(R.id.home_button_container).setOnClickListener {
            // Create intent for MainActivity
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        var conceptpts = findViewById<TextView>(R.id.concept_list)
        WordCount = findViewById(R.id.word_count)
        CourseTitle = findViewById(R.id.course_title)
        CourseTitle.text = intent.getStringExtra("course_title").toString()
        CopyIcon = findViewById(R.id.copy_icon)
        val data_text = intent.getStringExtra("note_content").toString()

        CopyIcon.setOnClickListener{
            Functions.copyToClipboard(this, conceptpts.text.toString())
//            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        var regen = findViewById<MaterialButton>(R.id.regenerate)
        regen.setOnClickListener {
            // Handle regenerate button click
//            Toast.makeText(this, "Regenerate button clicked", Toast.LENGTH_SHORT).show()
//            val summary_text = summary.text.toString()
            webApis.getConceptList(this, data_text, CourseTitle.text.toString()) { result ->
                Log.d("WebApis", "Result from aagay se: $result")
                if (result != null) {
                    try {
                        val jsonObject = JSONObject(result)
                        val message = jsonObject.getString("concept_list")
                        runOnUiThread {
                            conceptpts.text = message
                            WordCount.text = Functions.countPoints(message)
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