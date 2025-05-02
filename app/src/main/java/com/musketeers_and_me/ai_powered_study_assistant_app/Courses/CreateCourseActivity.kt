package com.musketeers_and_me.ai_powered_study_assistant_app.Courses

import ColorPickerAdapter
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBWriteOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils
import kotlin.properties.Delegates

class CreateCourseActivity : AppCompatActivity() {

    private val databaseService = FBDataBaseService()
    private val WriteOperations = FBWriteOperations(databaseService)
    var selectedColor: Int = R.color.red


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

        val titleBox = findViewById<EditText>(R.id.edit_course_title)
        val descriptionBox = findViewById<EditText>(R.id.edit_course_description)
        val createCourseButton = findViewById<MaterialButton>(R.id.createCourseButton)


        createCourseButton.setOnClickListener {
            val courseTitle = titleBox.text.toString()
            val courseDescription = descriptionBox.text.toString()


            if (courseTitle == ""){
                Toast.makeText(this, "Please enter a course title", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Course Created", Toast.LENGTH_SHORT).show()
                Log.d("CreateCourseActivity", "Course Title: $courseTitle")
                Log.d("CreateCourseActivity", "Course Description: $courseDescription")
                Log.d("CreateCourseActivity", "Selected Color: $selectedColor")
                WriteOperations.CreateCourse(courseTitle, courseDescription, selectedColor)
//                val intent = Intent(this, CourseActivity::class.java)
//                startActivity(intent)
                finish()
            }

        }


        val adapter = ColorPickerAdapter(colors) { color ->
            selectedColor = color
            Log.d("ColorPicker", "Selected color updated: $selectedColor")
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

}
