package com.musketeers_and_me.ai_powered_study_assistant_app.SmartDigest

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.WebApis
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils

class SummaryActivity : AppCompatActivity() {

    private val webApis = WebApis()  // ✅ Initialize the class here

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

        var regen = findViewById<MaterialButton>(R.id.regenerate)
        regen.setOnClickListener {
            // Handle regenerate button click
            Toast.makeText(this, "Regenerate button clicked", Toast.LENGTH_SHORT).show()
            val summary_text = summary.text.toString()
            webApis.getSummary(this , summary_text) { result ->
                if (result != null) {
                    summary.text = result
                }
//                } else {
//                    Toast.makeText(this, "Error fetching summary", Toast.LENGTH_SHORT).show()
//                }
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