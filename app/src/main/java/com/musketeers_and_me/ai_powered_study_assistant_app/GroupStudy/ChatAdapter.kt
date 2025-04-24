package com.musketeers_and_me.ai_powered_study_assistant_app.GroupStudy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.ChatMessage
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.MessageType
import com.musketeers_and_me.ai_powered_study_assistant_app.R

class ChatAdapter(
    private val currentUserId: String
) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    fun setMessages(newMessages: List<ChatMessage>) {
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

        // Show sender name only if it's different from previous message
        val showSenderName = position == 0 || 
            messages[position - 1].senderId != message.senderId ||
            messages[position - 1].messageType != message.messageType

        holder.nameText.visibility = if (showSenderName) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = messages.size

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.nameText)
        private val messageText: EditText = itemView.findViewById(R.id.messageText)

        fun bind(message: ChatMessage) {
            // Set sender name
            nameText.text = when (message.messageType) {
                MessageType.NOTE -> "${message.senderName} sent a note"
                else -> if (message.senderId == currentUserId) "Sender" else message.senderName
            }

            // Set message text
            when (message.messageType) {
                MessageType.TOPIC -> {
                    messageText.hint = "Topic Name"
                    messageText.setText(message.topicName)
                }
                else -> {
                    messageText.hint = ""
                    messageText.setText(message.message)
                }
            }

            // Align messages
            val params = itemView.layoutParams as RecyclerView.LayoutParams
            if (message.senderId == currentUserId) {
                params.marginStart = 80
                params.marginEnd = 16
            } else {
                params.marginStart = 16
                params.marginEnd = 80
            }
            itemView.layoutParams = params
        }
    }
} 