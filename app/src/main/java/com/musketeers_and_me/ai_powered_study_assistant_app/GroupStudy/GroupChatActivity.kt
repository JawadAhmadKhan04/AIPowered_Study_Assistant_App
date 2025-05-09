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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.musketeers_and_me.ai_powered_study_assistant_app.AuthService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBReadOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBWriteOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.GroupMessage
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.MessageType
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.StudyGroup
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils

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
    private val authService = AuthService()
    private val currentUserId = authService.getCurrentUserId().toString()
    private var currentUserName: String = ""
    private val databaseService = FBDataBaseService()
    private val readOperations = FBReadOperations(databaseService)
    private val writeOperations = FBWriteOperations(databaseService)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)
        enableEdgeToEdge()

        // Set window to adjust for keyboard
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

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

        // Load current user's name
        loadCurrentUserName()

        // Load messages
        loadMessages()

        // Set up message input
        setupMessageInput()

        // Set up code button
        codeButton.setOnClickListener {
            // Show group code
            readOperations.getGroupDetails(groupId) { group ->
                group?.let {
                    Toast.makeText(this, "Group Code: ${it.code}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Set up notes button
        notesButton.setOnClickListener {
            // Show add note dialog
            showAddNoteDialog()
        }
    }

    private fun loadCurrentUserName() {
        readOperations.getUserDetails(currentUserId) { user ->
            if (user != null) {
                currentUserName = user.username
            }
        }
    }

    private fun loadMessages() {
        readOperations.getGroupMessages(groupId) { messages ->
            chatAdapter.setMessages(messages)
            messagesRecyclerView.scrollToPosition(messages.size - 1)
        }
    }

    private fun showAddNoteDialog() {
        val dialog = AddNoteDialog.newInstance { note ->
            // Create a note message
            val message = GroupMessage(
                content = note.title,
                senderId = currentUserId,
                senderName = currentUserName,
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
        writeOperations.sendGroupMessage(groupId, message) { success ->
            if (!success) {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
            } else if (message.messageType == MessageType.REGULAR) {
                messageInput.text.clear()
            }
        }
    }

    private fun setupMessageInput() {
        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val message = GroupMessage(
                    content = messageText,
                    senderId = currentUserId,
                    senderName = currentUserName,
                    timestamp = System.currentTimeMillis(),
                    messageType = MessageType.REGULAR
                )
                sendMessage(message)
            }
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