package com.musketeers_and_me.ai_powered_study_assistant_app.GroupStudy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.OfflineFirstDataManager
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.GroupMessage
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.MessageType
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils
import kotlinx.coroutines.launch
import android.util.Log

class GroupChatActivity : AppCompatActivity() {
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var courseTitleText: TextView
    private lateinit var codeButton: MaterialButton
    private lateinit var notesButton: ImageView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var groupId: String
    private lateinit var groupName: String
    private lateinit var dataManager: OfflineFirstDataManager
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""
    private var currentUserName: String = ""
    private val TAG = "GroupChatActivity"
    private var messageListener: ChildEventListener? = null
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)
        enableEdgeToEdge()

        // Set window to adjust for keyboard
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        // Initialize data manager
        dataManager = OfflineFirstDataManager.getInstance(applicationContext)

        // Initialize views
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        courseTitleText = findViewById(R.id.courseTitleText)
        codeButton = findViewById(R.id.codeButton)
        notesButton = findViewById(R.id.notes)

        // Get group ID and name from intent
        groupId = intent.getStringExtra(EXTRA_GROUP_ID) ?: return
        groupName = intent.getStringExtra(EXTRA_GROUP_NAME) ?: return

        // Setup toolbar
        ToolbarUtils.setupToolbar(this, "Group Chat", R.drawable.home_logo_top_bar, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Group Chat"

        // Set up RecyclerView
        chatAdapter = ChatAdapter(currentUserId)
        messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@GroupChatActivity).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }

        // Set group name
        courseTitleText.text = groupName

        // Initialize data manager if needed
        lifecycleScope.launch {
            try {
                if (!dataManager.isInitialized) {
                    dataManager.initialize()
                }
                
                // Load current user's name
                loadCurrentUserName()
                
                // Load messages
                loadMessages()
                
                // Set up real-time message listener
                setupMessageListener()
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing data manager", e)
                Toast.makeText(this@GroupChatActivity, 
                    "Failed to initialize data. Please try again.", 
                    Toast.LENGTH_SHORT).show()
            }
        }

        // Set up message input
        setupMessageInput()

        // Set up code button
        codeButton.setOnClickListener {
            // Show group code
            lifecycleScope.launch {
                try {
                    val group = dataManager.getGroupDetails(groupId)
                    group?.let {
                        Toast.makeText(this@GroupChatActivity, "Group Code: ${it.code}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting group details", e)
                    Toast.makeText(this@GroupChatActivity, 
                        "Failed to get group code", 
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set up notes button
        notesButton.setOnClickListener {
            // Show add note dialog
            showAddNoteDialog()
        }
    }

    private suspend fun loadCurrentUserName() {
        try {
            val user = dataManager.getUserDetails(currentUserId)
            if (user != null) {
                currentUserName = user.username
                Log.d(TAG, "Current user name loaded: $currentUserName")
            } else {
                Log.e(TAG, "Failed to load user details")
                // Set a default username if we can't get the real one
                currentUserName = "User"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user details", e)
            // Set a default username if there's an error
            currentUserName = "User"
        }
    }

    private fun loadMessages() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Loading messages for group: $groupId")
                val messages = dataManager.getGroupMessages(groupId)
                Log.d(TAG, "Loaded ${messages.size} messages")
                
                // Log message details for debugging
                messages.forEach { message ->
                    Log.d(TAG, "Message: id=${message.id}, sender=${message.senderName}, content=${message.content}")
                }
                
                chatAdapter.setMessages(messages)
                if (messages.isNotEmpty()) {
                    messagesRecyclerView.scrollToPosition(messages.size - 1)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading messages", e)
                Toast.makeText(this@GroupChatActivity, 
                    "Failed to load messages", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddNoteDialog() {
        val dialog = AddNoteDialog.newInstance { note ->
            // Create a note message
            val message = GroupMessage(
                content = note.title,
                timestamp = System.currentTimeMillis(),
                messageType = MessageType.NOTE,
                noteId = note.note_id,
                noteType = note.type
            )
            
            // Send the note message
            sendMessage(message)
        }
        dialog.show(supportFragmentManager, "AddNoteDialog")
    }

    private fun sendMessage(message: GroupMessage) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Preparing to send message: ${message.content}")
                
                // Create a complete message with groupId
                val completeMessage = message.copy(
                    groupId = groupId,
                    senderId = currentUserId,
                    senderName = currentUserName
                )
                
                Log.d(TAG, "Calling dataManager.sendGroupMessage")
                val success = dataManager.sendGroupMessage(groupId, completeMessage)
                
                if (!success) {
                    Log.e(TAG, "Failed to send message - dataManager.sendGroupMessage returned false")
                    Toast.makeText(this@GroupChatActivity, 
                        "Failed to send message. Please check your connection and try again.", 
                        Toast.LENGTH_SHORT).show()
                } else {
                    Log.d(TAG, "Message sent successfully")
                    if (message.messageType == MessageType.REGULAR) {
                        messageInput.text.clear()
                        // Reload messages to show the new one
                        loadMessages()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while sending message", e)
                e.printStackTrace()
                Toast.makeText(this@GroupChatActivity, 
                    "Error sending message: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupMessageInput() {
        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val message = GroupMessage(
                    content = messageText,
                    timestamp = System.currentTimeMillis(),
                    messageType = MessageType.REGULAR
                )
                sendMessage(message)
            }
        }
    }

    private fun setupMessageListener() {
        // Remove any existing listener
        messageListener?.let {
            database.getReference("groupChats").child(groupId).child("messages").removeEventListener(it)
        }
        
        // Set up new listener for real-time updates
        messageListener = database.getReference("groupChats").child(groupId).child("messages")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(TAG, "New message received: ${snapshot.key}")
                    // Reload all messages to ensure proper order
                    loadMessages()
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    // Message updated, reload all messages
                    loadMessages()
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    // Message deleted, reload all messages
                    loadMessages()
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    // Not used for messages
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Message listener cancelled", error.toException())
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up message listener
        messageListener?.let {
            database.getReference("groupChats").child(groupId).child("messages").removeEventListener(it)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        private const val EXTRA_GROUP_ID = "extra_group_id"
        private const val EXTRA_GROUP_NAME = "extra_group_name"

        fun start(context: Context, groupId: String, groupName: String) {
            val intent = Intent(context, GroupChatActivity::class.java).apply {
                putExtra(EXTRA_GROUP_ID, groupId)
                putExtra(EXTRA_GROUP_NAME, groupName)
            }
            context.startActivity(intent)
        }
    }
} 