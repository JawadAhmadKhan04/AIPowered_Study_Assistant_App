package com.musketeers_and_me.ai_powered_study_assistant_app.Opening_Registeration

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.musketeers_and_me.ai_powered_study_assistant_app.R

class LoginSignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_signup)

        showFragment(LoginFragment()) // Default fragment

        findViewById<Button>(R.id.loginTabBtn).setOnClickListener {
            showFragment(LoginFragment())
        }

        findViewById<Button>(R.id.signupTabBtn).setOnClickListener {
            showFragment(SignUpFragment())
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.authFragmentContainer, fragment)
            .commit()
    }
}
