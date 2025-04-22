package com.musketeers_and_me.ai_powered_study_assistant_app.OuterStructure.Settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R


class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as? MainActivity)?.apply{
            updateToolbar("Settings", R.drawable.back)
            updateBottomNavIcon(R.id.nav_settings, R.drawable.settings_navbar_selected) // optional dynamic nav icon
        }
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
}

