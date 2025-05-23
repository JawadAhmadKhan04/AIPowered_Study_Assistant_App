package com.musketeers_and_me.ai_powered_study_assistant_app.QuizCenter

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
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
        viewPager.offscreenPageLimit = 2 // Ensure both fragments are instantiated

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Quiz Settings"
                1 -> "Take Quiz"
                else -> null
            }
        }.attach()

        // Set up All Results button click listener
        allResultsButton.setOnClickListener {
            val intent = Intent(this, AllQuizResultsActivity::class.java)
            startActivity(intent)
        }

        findViewById<FrameLayout>(R.id.home_button_container).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    fun switchToTakeQuizTab() {
        viewPager.setCurrentItem(1, true)
    }
}