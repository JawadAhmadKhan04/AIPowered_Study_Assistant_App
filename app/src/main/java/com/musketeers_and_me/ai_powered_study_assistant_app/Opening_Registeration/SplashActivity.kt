package com.musketeers_and_me.ai_powered_study_assistant_app.Opening_Registeration

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.databinding.ActivitySplashBinding
import com.musketeers_and_me.ai_powered_study_assistant_app.R

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load the animation
        val bookOpeningAnimation = AnimationUtils.loadAnimation(this, R.anim.book_opening)
        binding.bookImageView.startAnimation(bookOpeningAnimation)

        // Delay for 3 seconds and then start the main activity
        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}
