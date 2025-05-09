package com.musketeers_and_me.ai_powered_study_assistant_app.GroupStudy

import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.musketeers_and_me.ai_powered_study_assistant_app.LectureAndNotes.TextNoteActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.LectureAndNotes.VoiceNoteActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.GroupMessage
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.MessageType
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val currentUserId: String
) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    private val messages = mutableListOf<GroupMessage>()
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun addMessage(message: GroupMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun setMessages(newMessages: List<GroupMessage>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount() = messages.size

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.nameText)
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val messageContainer: LinearLayout = itemView.findViewById(R.id.messageContainer)

        fun bind(message: GroupMessage) {
            // Set sender name
            nameText.text = message.senderName
            
            // Set message content and handle note messages
            if (message.messageType == MessageType.NOTE) {
                messageText.text = "📝 ${message.content}"
                messageText.setOnClickListener {
                    val intent = if (message.noteType == "text") {
                        Intent(itemView.context, TextNoteActivity::class.java)
                    } else {
                        Intent(itemView.context, VoiceNoteActivity::class.java)
                    }
                    intent.putExtra("note_id", message.noteId)
                    itemView.context.startActivity(intent)
                }
            } else {
                messageText.text = message.content
                messageText.setOnClickListener(null)
            }
            
            // Set timestamp
            timeText.text = dateFormat.format(Date(message.timestamp))
            
            // Set background and alignment based on sender
            if (message.isCurrentUser) {
                // Current user's message
                messageText.setBackgroundResource(R.drawable.sender_message_background)
                messageText.setTextColor(itemView.context.getColor(R.color.white))
                
                // Align everything to right
                val containerParams = messageContainer.layoutParams as LinearLayout.LayoutParams
                containerParams.gravity = Gravity.END
                messageContainer.layoutParams = containerParams
                
                val nameParams = nameText.layoutParams as LinearLayout.LayoutParams
                nameParams.gravity = Gravity.END
                nameText.layoutParams = nameParams
                
                val messageParams = messageText.layoutParams as LinearLayout.LayoutParams
                messageParams.gravity = Gravity.END
                messageText.layoutParams = messageParams
                
                val timeParams = timeText.layoutParams as LinearLayout.LayoutParams
                timeParams.gravity = Gravity.END
                timeText.layoutParams = timeParams
            } else {
                // Other user's message
                messageText.setBackgroundResource(R.drawable.receiver_message_background)
                messageText.setTextColor(itemView.context.getColor(R.color.black))
                
                // Align everything to left
                val containerParams = messageContainer.layoutParams as LinearLayout.LayoutParams
                containerParams.gravity = Gravity.START
                messageContainer.layoutParams = containerParams
                
                val nameParams = nameText.layoutParams as LinearLayout.LayoutParams
                nameParams.gravity = Gravity.START
                nameText.layoutParams = nameParams
                
                val messageParams = messageText.layoutParams as LinearLayout.LayoutParams
                messageParams.gravity = Gravity.START
                messageText.layoutParams = messageParams
                
                val timeParams = timeText.layoutParams as LinearLayout.LayoutParams
                timeParams.gravity = Gravity.START
                timeText.layoutParams = timeParams
            }
        }
    }
} 