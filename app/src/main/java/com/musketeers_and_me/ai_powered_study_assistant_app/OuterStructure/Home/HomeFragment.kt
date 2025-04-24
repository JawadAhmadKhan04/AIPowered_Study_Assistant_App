package com.musketeers_and_me.ai_powered_study_assistant_app.OuterStructure.Home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.CardItem
import com.musketeers_and_me.ai_powered_study_assistant_app.QuizCenter.QuizCenterActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.GroupStudy.GroupStudyActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Set dynamic toolbar and bottom nav icon
        (activity as? MainActivity)?.apply {
            updateToolbar("Home", R.drawable.home_logo_top_bar)
            updateBottomNavIcon(R.id.nav_home, R.drawable.home_navbar_selected)
        }

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardList = listOf(
            CardItem("My Courses", R.drawable.courses),
            CardItem("Quiz Center", R.drawable.quiz),
            CardItem("Group Study", R.drawable.group_study)
            // Add more cards as needed
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.cardRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = CardAdapter(cardList) { card ->
            when (card.title) {
                "Quiz Center" -> {
                    startActivity(Intent(requireContext(), QuizCenterActivity::class.java))
                }
                "My Courses" -> {
                    // Handle My Courses click
                }
                "Group Study" -> {
                    startActivity(Intent(requireContext(), GroupStudyActivity::class.java))
                }
            }
        }
    }
}
