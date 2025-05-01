//package com.musketeers_and_me.ai_powered_study_assistant_app.Opening_Registeration
//
//import android.os.Bundle
//import android.widget.Button
//import androidx.appcompat.app.AppCompatActivity
//import androidx.fragment.app.Fragment
//import com.musketeers_and_me.ai_powered_study_assistant_app.R
//
//class LoginSignUpActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login_signup)
//
//        showFragment(LoginFragment()) // Default fragment
//
//        findViewById<Button>(R.id.loginTabBtn).setOnClickListener {
//            showFragment(LoginFragment())
//        }
//
//        findViewById<Button>(R.id.signupTabBtn).setOnClickListener {
//            showFragment(SignUpFragment())
//        }
//    }
//
//    private fun showFragment(fragment: Fragment) {
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.authFragmentContainer, fragment)
//            .commit()
//    }
//}

package com.musketeers_and_me.ai_powered_study_assistant_app.Opening_Registeration

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.musketeers_and_me.ai_powered_study_assistant_app.R

class LoginSignUpActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth

    private lateinit var viewPager: ViewPager2
    private lateinit var loginTabBtn: Button
    private lateinit var signupTabBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_signup)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()

        viewPager = findViewById(R.id.viewPager)
        loginTabBtn = findViewById(R.id.btnLoginTab)
        signupTabBtn = findViewById(R.id.btnSignUpTab)

        viewPager.adapter = AuthPagerAdapter(this)

        // Tab click listeners
        loginTabBtn.setOnClickListener {
            viewPager.currentItem = 0
            updateTabSelection(0)
        }

        signupTabBtn.setOnClickListener {
            viewPager.currentItem = 1
            updateTabSelection(1)
        }

        // Sync tab highlight with swipe
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateTabSelection(position)
            }
        })

        updateTabSelection(0) // Set default selected tab
    }

    fun getFirebaseAuth(): FirebaseAuth {
        return auth
    }

    private fun updateTabSelection(position: Int) {
        if (position == 0) {
            loginTabBtn.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            loginTabBtn.alpha = 1f
            signupTabBtn.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            signupTabBtn.alpha = 0.5f
        } else {
            signupTabBtn.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            signupTabBtn.alpha = 1f
            loginTabBtn.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            loginTabBtn.alpha = 0.5f
        }
    }

}
