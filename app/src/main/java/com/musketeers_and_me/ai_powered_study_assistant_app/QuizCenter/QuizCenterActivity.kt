package com.musketeers_and_me.ai_powered_study_assistant_app.QuizCenter

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils


class QuizCenterActivity : AppCompatActivity() {
    lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var allResultsButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_center)
        enableEdgeToEdge()
        ToolbarUtils.setupToolbar(this, "Quiz Center", R.drawable.home_logo_top_bar, true)

        // Initialize views
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        allResultsButton = findViewById(R.id.allResultsButton)

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
                0 -> "Questions"
                1 -> "Settings"
                else -> null
            }
        }.attach()

        // Handle back navigation
        onBackPressedDispatcher.addCallback(this) {
            if (viewPager.currentItem == 0) {
                // Navigate to MainActivity
                val intent = Intent(this@QuizCenterActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            } else {
                viewPager.currentItem = 0
            }
        }

        // Set up All Results button click listener
        allResultsButton.setOnClickListener {
            val intent = Intent(this, AllQuizResultsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
} 