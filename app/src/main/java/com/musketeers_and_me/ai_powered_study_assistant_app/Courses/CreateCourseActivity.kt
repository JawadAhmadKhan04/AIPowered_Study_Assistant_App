package com.musketeers_and_me.ai_powered_study_assistant_app.Courses

import ColorPickerAdapter
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils

class CreateCourseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_course)

        // Setup toolbar
        ToolbarUtils.setupToolbar(this, "New Course", R.drawable.home_logo_top_bar, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "New Course"




        val recyclerView = findViewById<RecyclerView>(R.id.color_picker_recycler)


            val colors = listOf(
                ContextCompat.getColor(this, R.color.red),
                ContextCompat.getColor(this, R.color.green),
                ContextCompat.getColor(this, R.color.lavender),
                ContextCompat.getColor(this, R.color.cyan),
                ContextCompat.getColor(this, R.color.lightPurple),
                ContextCompat.getColor(this, R.color.yellow)
        )



        val adapter = ColorPickerAdapter(colors) { selectedColor ->
            // Handle color selection (optional)
        }
        recyclerView.layoutManager = GridLayoutManager(this, 3) // 3 columns
        recyclerView.adapter = adapter

        findViewById<FrameLayout>(R.id.home_button_container).setOnClickListener {
            // Create intent for MainActivity
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

    }
}
