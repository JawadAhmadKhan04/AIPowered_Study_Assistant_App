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

class ExtractKeyPointsActivity : AppCompatActivity() {

    private val webApis = WebApis()  // ✅ Initialize the class here
    private lateinit var CourseTitle: TextView
    private lateinit var WordCount: TextView
    private lateinit var CopyIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_extract_key_points)
        // Setup Toolbar
        ToolbarUtils.setupToolbar(this, "Key Points", R.drawable.back, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Key Points"
        findViewById<FrameLayout>(R.id.home_button_container).setOnClickListener {
            // Create intent for MainActivity
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        var key_pts = findViewById<TextView>(R.id.key_points)
        WordCount = findViewById(R.id.word_count)
        CourseTitle = findViewById(R.id.course_title)
        CourseTitle.text = intent.getStringExtra("course_title").toString()
        CopyIcon = findViewById(R.id.copy_icon)
        val data_text = intent.getStringExtra("note_content").toString()


        CopyIcon.setOnClickListener{
            // Handle copy icon click
            Functions.copyToClipboard(this, key_pts.text.toString())
//            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        var regen = findViewById<MaterialButton>(R.id.regenerate)
        regen.setOnClickListener {

            webApis.getKeyPts(this, data_text, CourseTitle.text.toString()) { result ->
                Log.d("WebApis", "Result from aagay se: $result")
                if (result != null) {
                    try {
                        val jsonObject = JSONObject(result)
                        val message = jsonObject.getString("key_points")
                        runOnUiThread {
                            key_pts.text = message
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