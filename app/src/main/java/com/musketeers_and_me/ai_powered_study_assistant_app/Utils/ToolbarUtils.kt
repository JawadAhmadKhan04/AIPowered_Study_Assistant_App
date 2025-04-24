package com.musketeers_and_me.ai_powered_study_assistant_app.Utils

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.musketeers_and_me.ai_powered_study_assistant_app.R

object ToolbarUtils {
    fun setupToolbar(activity: AppCompatActivity, title: String, logoResId: Int, showBackButton: Boolean = false) {
        val toolbar = activity.findViewById<Toolbar>(R.id.toolbar)
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(showBackButton)
        activity.supportActionBar?.setDisplayShowTitleEnabled(false)  // Hide default title

        // Set custom title and logo
        //val titleTextView = toolbar.findViewById<TextView>(R.id.toolbarTitle)
        //val logoImageView = toolbar.findViewById<ImageView>(R.id.toolbarLogo)

        //titleTextView.text = title
        //logoImageView.setImageResource(logoResId)
    }
}