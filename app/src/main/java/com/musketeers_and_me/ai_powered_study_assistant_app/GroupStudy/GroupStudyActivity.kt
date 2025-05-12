package com.musketeers_and_me.ai_powered_study_assistant_app.GroupStudy

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.OfflineFirstDataManager
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.StudyGroup
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils
import android.util.Log
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GroupStudyActivity : AppCompatActivity(), 
    NewGroupDialog.OnGroupCreatedListener,
    JoinGroupDialog.OnGroupJoinedListener {
    
    private val TAG = "GroupStudyActivity"
    private lateinit var recyclerView: RecyclerView
    private lateinit var joinButton: MaterialButton
    private lateinit var newButton: MaterialButton
    private lateinit var adapter: GroupAdapter
    private lateinit var dataManager: OfflineFirstDataManager
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_study)
        enableEdgeToEdge()

        // Initialize the data manager
        dataManager = OfflineFirstDataManager.getInstance(applicationContext)
        
        // Initialize data manager if needed
        lifecycleScope.launch {
            if (!dataManager.isInitialized) {
                try {
                    dataManager.initialize()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to initialize data manager", e)
                    Toast.makeText(this@GroupStudyActivity, 
                        "Failed to initialize data. Please try again.", 
                        Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }
            }
            
            // Setup UI after initialization
            setupUI()
        }
    }
    
    private fun setupUI() {
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
            Log.d(TAG, "Join button clicked")
            showJoinDialog()
        }

        newButton.setOnClickListener {
            Log.d(TAG, "New button clicked")
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
        Log.d(TAG, "Loading study groups")
        val userId = auth.currentUser?.uid
        
        if (userId == null) {
            Log.e(TAG, "User is not authenticated")
            Toast.makeText(this, "Please login to view your groups", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val groups = withContext(Dispatchers.IO) {
                    dataManager.getStudyGroups(userId)
                }
                
                Log.d(TAG, "Received ${groups.size} groups")
                adapter.updateGroups(groups)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading study groups", e)
                Toast.makeText(this@GroupStudyActivity, 
                    "Failed to load groups. Please try again.", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showNewGroupDialog() {
        val dialog = NewGroupDialog()
        dialog.setOnGroupCreatedListener(this)
        dialog.show(supportFragmentManager, "NewGroupDialog")
    }

    private fun showJoinDialog() {
        val dialog = JoinGroupDialog()
        dialog.setOnGroupJoinedListener(this)
        dialog.show(supportFragmentManager, "JoinGroupDialog")
    }

    override fun onGroupCreated() {
        // Refresh the groups list when a new group is created
        Log.d(TAG, "Group created, refreshing list")
        loadStudyGroups()
    }
    
    override fun onGroupJoined() {
        // Refresh the groups list when a group is joined
        Log.d(TAG, "Group joined, refreshing list")
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