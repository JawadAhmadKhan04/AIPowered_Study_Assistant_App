package com.musketeers_and_me.ai_powered_study_assistant_app.QuizCenter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class QuizCenterPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> QuizSettingsFragment()
            1 -> QuizQuestionFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
} 