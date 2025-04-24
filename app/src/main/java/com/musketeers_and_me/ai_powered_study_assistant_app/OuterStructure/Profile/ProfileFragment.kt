package com.musketeers_and_me.ai_powered_study_assistant_app.OuterStructure.Profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.CardItem
import com.musketeers_and_me.ai_powered_study_assistant_app.R



class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as? MainActivity)?.apply{
            updateBottomNavIcon(R.id.nav_profile, R.drawable.profile_navbar_selected) // optional dynamic nav icon
        }
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profileCards = listOf(
            CardItem("Courses", R.drawable.courses, "4"),
            CardItem("Lectures", R.drawable.lectures, "20"),
            CardItem("Smart Digest", R.drawable.smart_digest, "20"),
            CardItem("Quiz Created", R.drawable.quiz, "20"),
            CardItem("Groups", R.drawable.group_study, "3"),
            CardItem("Time Spent", R.drawable.clock, "00:00:00")
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.cardRecyclerView)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = ProfileCardAdapter(profileCards)

    }

}

