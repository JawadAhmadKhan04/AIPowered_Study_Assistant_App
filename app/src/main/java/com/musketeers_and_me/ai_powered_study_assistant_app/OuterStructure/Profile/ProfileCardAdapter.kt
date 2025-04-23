package com.musketeers_and_me.ai_powered_study_assistant_app.OuterStructure.Profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.CardItem
import com.musketeers_and_me.ai_powered_study_assistant_app.R


class ProfileCardAdapter(private val cardList: List<CardItem>) : RecyclerView.Adapter<ProfileCardAdapter.CardViewHolder>() {

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.card_icon)
        val title: TextView = itemView.findViewById(R.id.card_title)
        val value: TextView = itemView.findViewById(R.id.card_value)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val item = cardList[position]
        holder.icon.setImageResource(item.iconResId)
        holder.title.text = item.title
        holder.value.text = item.value
    }

    override fun getItemCount() = cardList.size
}
