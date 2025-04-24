package com.musketeers_and_me.ai_powered_study_assistant_app.GroupStudy

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Group
import com.musketeers_and_me.ai_powered_study_assistant_app.R

class GroupAdapter(private val groups: List<Group>) :
    RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.bind(group)
    }

    override fun getItemCount() = groups.size

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.titleTextView)
        private val descriptionText: TextView = itemView.findViewById(R.id.descriptionTextView)
        val closeButton: ImageButton = itemView.findViewById(R.id.closeButton)

        init {
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, GroupChatActivity::class.java).apply {
                    putExtra("groupName", groups[adapterPosition].title)
                }
                context.startActivity(intent)
            }
        }

        fun bind(group: Group) {
            titleText.text = group.title
            descriptionText.text = group.description
            
            // Handle close button click
            closeButton.setOnClickListener {
                // TODO: Implement remove group functionality
            }
        }
    }
} 