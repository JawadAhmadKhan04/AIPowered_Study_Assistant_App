package com.musketeers_and_me.ai_powered_study_assistant_app.LectureAndNotes

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils

class UploadImageActivity : AppCompatActivity() {
    private lateinit var courseTitle: TextView
    private lateinit var courseDescription: TextView
    private lateinit var takePhotoContainer: LinearLayout
    private lateinit var uploadImageContainer: LinearLayout
    private lateinit var imagePlaceholder: ImageView
    private lateinit var extractTextButton: MaterialButton
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upload_image)
        // Setup Toolbar
        ToolbarUtils.setupToolbar(this, "Upload Image", R.drawable.back, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Upload Image"

        //initialize views
        courseTitle = findViewById(R.id.course_title)
        courseDescription = findViewById(R.id.course_description)
        takePhotoContainer = findViewById(R.id.take_photo_container)
        uploadImageContainer = findViewById(R.id.upload_image_container)
        imagePlaceholder = findViewById(R.id.image_placeholder)
        extractTextButton = findViewById(R.id.extract_text_button)
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