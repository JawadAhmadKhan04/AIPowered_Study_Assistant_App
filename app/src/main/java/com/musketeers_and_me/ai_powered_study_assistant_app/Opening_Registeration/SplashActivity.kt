package com.musketeers_and_me.ai_powered_study_assistant_app.Opening_Registeration

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.databinding.ActivitySplashBinding
import com.musketeers_and_me.ai_powered_study_assistant_app.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // this must be called after super
        setContentView(R.layout.activity_splash)

        val logo = findViewById<View>(R.id.myImageView)
        logo.alpha = 0f
        Log.d("TAG", "Splash1: ${logo.alpha}")
        logo.animate().setDuration(2000).alpha(1f).withEndAction {
            startActivity(Intent(this, MainActivity::class.java))
            Log.d("TAG", "Splash2: ${logo.alpha}")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            Log.d("TAG", "Splash2: ${logo.alpha}")
            finish()
        }
    }
}

