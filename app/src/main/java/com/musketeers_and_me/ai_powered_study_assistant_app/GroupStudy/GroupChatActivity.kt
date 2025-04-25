package com.musketeers_and_me.ai_powered_study_assistant_app.GroupStudy

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.ChatMessage
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.MessageType
import com.musketeers_and_me.ai_powered_study_assistant_app.OuterStructure.Home.HomeFragment
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils
import java.util.UUID

class GroupChatActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var notesImageView: ImageView
    private lateinit var courseTitleText: TextView
    private lateinit var adapter: ChatAdapter

    // Simulated user IDs for demo
    private val currentUserId = "current_user"
    private val receiver1Id = "receiver1"
    private val receiver2Id = "receiver2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        enableEdgeToEdge()
        // Setup toolbar
        ToolbarUtils.setupToolbar(this, "Chat", R.drawable.home_logo_top_bar, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Chat"

        // Get group info from intent
        val groupName = intent.getStringExtra("groupName") ?: "Course Title"
        
        // Initialize views
        recyclerView = findViewById(R.id.messagesRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        notesImageView = findViewById(R.id.notes)
        courseTitleText = findViewById(R.id.courseTitleText)

        // Set group name
        courseTitleText.text = groupName

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // Messages start from bottom
        }
        adapter = ChatAdapter(currentUserId)
        recyclerView.adapter = adapter

        // Add sample chat messages
        val sampleMessages = listOf(
            // Note message
            ChatMessage(
                id = UUID.randomUUID().toString(),
                senderId = receiver1Id,
                senderName = "Receiver 1",
                message = "sent a note",
                messageType = MessageType.NOTE
            ),
            // Topic message from Receiver 1
            ChatMessage(
                id = UUID.randomUUID().toString(),
                senderId = receiver1Id,
                senderName = "Receiver 1",
                message = "",
                topicName = "Topic Name",
                messageType = MessageType.TOPIC
            ),
            // Regular message from Receiver 1
            ChatMessage(
                id = UUID.randomUUID().toString(),
                senderId = receiver1Id,
                senderName = "Receiver 1",
                message = "Here's my response to the topic"
            ),
            // Message from current user
            ChatMessage(
                id = UUID.randomUUID().toString(),
                senderId = currentUserId,
                senderName = "Sender",
                message = "I agree with that point"
            ),
            // Another message from Receiver 1
            ChatMessage(
                id = UUID.randomUUID().toString(),
                senderId = receiver1Id,
                senderName = "Receiver 1",
                message = "Let's discuss this further"
            )
        )
        adapter.setMessages(sampleMessages)

        // Setup click listeners
        sendButton.setOnClickListener {
            val messageText = messageInput.text?.toString()?.trim()
            if (!messageText.isNullOrEmpty()) {
                // Create and add the message
                val message = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    senderId = currentUserId,
                    senderName = "Sender",
                    message = messageText
                )
                adapter.addMessage(message)
                
                // Clear input and scroll to bottom
                messageInput.text.clear()
                recyclerView.smoothScrollToPosition(adapter.itemCount - 1)

                // Simulate a response after a delay
                simulateResponse()
            }
        }

        // Setup notes dialog
        notesImageView.setOnClickListener {
            showAddNoteDialog()
        }

        findViewById<FrameLayout>(R.id.home_button_container).setOnClickListener {
            // Create intent for MainActivity
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

    }

    private fun simulateResponse() {
        recyclerView.postDelayed({
            val responseMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                senderId = receiver1Id,
                senderName = "Receiver 1",
                message = "Thanks for your message!"
            )
            adapter.addMessage(responseMessage)
            recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
        }, 1500) // 1.5 second delay
    }

    private fun showAddNoteDialog() {
        val dialog = AddNoteDialog(this) { topics ->
            // Handle the selected topics
            Toast.makeText(this, "Selected topics: ${topics.joinToString()}", Toast.LENGTH_SHORT).show()
        }
        dialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 