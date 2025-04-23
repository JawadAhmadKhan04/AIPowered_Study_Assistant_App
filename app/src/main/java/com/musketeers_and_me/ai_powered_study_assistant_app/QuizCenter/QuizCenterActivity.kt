package com.musketeers_and_me.ai_powered_study_assistant_app.QuizCenter

import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils


class QuizCenterActivity : AppCompatActivity() {
    lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_center)
        ToolbarUtils.setupToolbar(this, "Quiz Center", R.drawable.home_logo_top_bar, true)

        // Initialize views
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Quiz Center"

        // Setup ViewPager with adapter
        val pagerAdapter = QuizCenterPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Quiz Settings"
                1 -> "Take Quiz"
                else -> ""
            }
        }.attach()

        // Handle back navigation
        onBackPressedDispatcher.addCallback(this) {
            if (viewPager.currentItem == 0) {
                finish()
            } else {
                viewPager.currentItem = 0
            }
        }
    }

} 