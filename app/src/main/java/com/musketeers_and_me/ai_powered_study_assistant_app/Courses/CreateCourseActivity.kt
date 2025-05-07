package com.musketeers_and_me.ai_powered_study_assistant_app.Courses

import ColorPickerAdapter
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.AppDatabase
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBWriteOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.OfflineFirstDataManager
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.dao.UserLocalDao
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.GlobalData
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils
import java.util.UUID

class CreateCourseActivity : AppCompatActivity() {
    private lateinit var userLocalDao: UserLocalDao
    private var selectedColor: Int = R.color.red

    private val databaseService = FBDataBaseService()
    private val WriteOperations = FBWriteOperations(databaseService)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_course)

        // Initialize UserLocalDao
        val db = AppDatabase(this).writableDatabase
        userLocalDao = UserLocalDao(db)

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



            if (courseTitle.isEmpty()) {
                Toast.makeText(this, "Please enter a course title", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Course Created", Toast.LENGTH_SHORT).show()
                Log.d("CreateCourseActivity", "Course Title: $courseTitle")
                Log.d("CreateCourseActivity", "Course Description: $courseDescription")
                Log.d("CreateCourseActivity", "Selected Color: $selectedColor")
                WriteOperations.CreateCourse(courseTitle, courseDescription, selectedColor)
//                val intent = Intent(this, CourseActivity::class.java)
//                startActivity(intent)
                finish()
            }


//            } else {
//                // Create a new course
//                val course = Course(
//                    title = courseTitle,
//                    noteCount = 0,
//                    daysAgo = 0,
//                    buttonColorResId = selectedColor,
//                    bookmarked = false,
//                    courseId = UUID.randomUUID().toString(),
//                    description = courseDescription
//                )
//
//                // Save to SQLite and mark for sync
//                GlobalData.user_id?.let { userId ->
//                    userLocalDao.insertCourse(userId, course)
//                    userLocalDao.markCourseForSync(course.courseId)
//
//                    // Trigger sync
//                    val dataManager = OfflineFirstDataManager.getInstance(this)
//                    dataManager.syncNow()
//
//                    // Update UI
//                    setResult(Activity.RESULT_OK)
//                    finish()
//                } ?: run {
//                    Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show()
//                }
//            }
        }

        val adapter = ColorPickerAdapter(colors) { color ->
            selectedColor = color
            Log.d("ColorPicker", "Selected color updated: $selectedColor")
        }

        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter

        findViewById<FrameLayout>(R.id.home_button_container).setOnClickListener {
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
