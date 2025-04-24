package com.musketeers_and_me.ai_powered_study_assistant_app.GroupStudy

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Group
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils

class GroupStudyActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var joinButton: MaterialButton
    private lateinit var newButton: MaterialButton
    private lateinit var adapter: GroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_study)
        enableEdgeToEdge()

        // Setup toolbar
        ToolbarUtils.setupToolbar(this, "Group", R.drawable.home_logo_top_bar, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Group"

        // Initialize views
        recyclerView = findViewById(R.id.groupsRecyclerView)
        joinButton = findViewById(R.id.joinButton)
        newButton = findViewById(R.id.newButton)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // Create dummy data
        val groups = listOf(
            Group("Data Structures", "Learn about basic data structures"),
            Group("Algorithms", "Study common algorithms"),
            Group("Programming Basics", "Introduction to programming concepts")
        )

        // Setup adapter
        adapter = GroupAdapter(groups)
        recyclerView.adapter = adapter

        // Setup button click listeners
        joinButton.setOnClickListener {
            showJoinDialog()
        }

        newButton.setOnClickListener {
            showNewGroupDialog()
        }
    }

    private fun showNewGroupDialog() {
        NewGroupDialog(this) { name, description, code ->
            // Handle the new group creation
            Toast.makeText(
                this,
                "Creating group: $name with code: $code",
                Toast.LENGTH_SHORT
            ).show()
            // TODO: Implement actual group creation logic
        }.show()
    }

    private fun showJoinDialog() {
        JoinGroupDialog(this) { code ->
            // Handle the join code
            Toast.makeText(this, "Joining group with code: $code", Toast.LENGTH_SHORT).show()
        }.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        // Navigate back to MainActivity (HomeFragment)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
        return true
    }

    override fun onBackPressed() {
        onSupportNavigateUp()
    }
} 