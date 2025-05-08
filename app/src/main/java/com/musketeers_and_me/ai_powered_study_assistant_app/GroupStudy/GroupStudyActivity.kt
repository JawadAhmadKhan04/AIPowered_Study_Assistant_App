package com.musketeers_and_me.ai_powered_study_assistant_app.GroupStudy

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBReadOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.StudyGroup
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils

class GroupStudyActivity : AppCompatActivity(), NewGroupDialog.OnGroupCreatedListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var joinButton: MaterialButton
    private lateinit var newButton: MaterialButton
    private lateinit var adapter: GroupAdapter
    private val databaseService = FBDataBaseService()
    private val readOperations = FBReadOperations(databaseService)

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
        adapter = GroupAdapter { group ->
            GroupChatActivity.start(this, group.id, group.name)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Setup button click listeners
        joinButton.setOnClickListener {
            showJoinDialog()
        }

        newButton.setOnClickListener {
            showNewGroupDialog()
        }

        findViewById<FrameLayout>(R.id.home_button_container).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        // Load study groups
        loadStudyGroups()
    }

    private fun loadStudyGroups() {
        readOperations.getStudyGroups { groups ->
            adapter.updateGroups(groups)
        }
    }

    private fun showNewGroupDialog() {
        val dialog = NewGroupDialog()
        dialog.setOnGroupCreatedListener(this)
        dialog.show(supportFragmentManager, "NewGroupDialog")
    }

    private fun showJoinDialog() {
        val dialog = JoinGroupDialog()
        dialog.show(supportFragmentManager, "JoinGroupDialog")
    }

    override fun onGroupCreated() {
        // Refresh the groups list when a new group is created
        loadStudyGroups()
    }

    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        onSupportNavigateUp()
    }
} 