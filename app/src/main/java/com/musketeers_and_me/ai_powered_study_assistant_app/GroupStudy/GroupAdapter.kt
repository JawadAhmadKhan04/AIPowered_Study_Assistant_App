package com.musketeers_and_me.ai_powered_study_assistant_app.GroupStudy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.StudyGroup
import com.musketeers_and_me.ai_powered_study_assistant_app.R

class GroupAdapter(
    private val onGroupClick: (StudyGroup) -> Unit
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    private val groups = mutableListOf<StudyGroup>()

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.titleTextView)
        val descriptionText: TextView = itemView.findViewById(R.id.descriptionTextView)
        val closeButton: ImageButton = itemView.findViewById(R.id.closeButton)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onGroupClick(groups[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.titleText.text = group.name
        holder.descriptionText.text = group.description
        
        // Handle close button click
        holder.closeButton.setOnClickListener {
            // TODO: Implement remove group functionality
        }
    }

    override fun getItemCount() = groups.size

    fun updateGroups(newGroups: List<StudyGroup>) {
        groups.clear()
        groups.addAll(newGroups)
        notifyDataSetChanged()
    }
} 