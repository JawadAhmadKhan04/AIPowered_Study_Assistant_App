package com.musketeers_and_me.ai_powered_study_assistant_app.OuterStructure.Profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R


class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as? MainActivity)?.apply{
            updateToolbar("Profile", R.drawable.back)
            updateBottomNavIcon(R.id.nav_profile, R.drawable.profile_navbar_selected) // optional dynamic nav icon
        }
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
}

