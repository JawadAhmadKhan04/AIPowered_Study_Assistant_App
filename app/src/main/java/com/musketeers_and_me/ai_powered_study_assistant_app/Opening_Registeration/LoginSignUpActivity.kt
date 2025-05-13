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

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.AppDatabase
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.OfflineFirstDataManager
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.cancel

class LoginSignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dataManager: OfflineFirstDataManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var viewPager: ViewPager2
    private lateinit var loginTabBtn: Button
    private lateinit var signupTabBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_signup)
        enableEdgeToEdge()

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        // Initialize data manager
        dataManager = OfflineFirstDataManager.getInstance(this)

        // Check if user is already logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Check for pending syncs before proceeding
            checkPendingSyncs(currentUser.uid) { success ->
                if (success) {
                    // Start MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Show error message
                    Toast.makeText(this, "Error syncing data. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }

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

    private fun checkPendingSyncs(userId: String, onComplete: (Boolean) -> Unit) {
        scope.launch {
            try {
                // Initialize data manager and check sync status
                dataManager.initialize()
                
                // Check if we need to sync
                val db = AppDatabase.getInstance(this@LoginSignUpActivity)
                val sqliteDb = db.readableDatabase
                val cursor = sqliteDb.query(
                    AppDatabase.TABLE_USERS,
                    arrayOf(AppDatabase.COLUMN_PENDING_SYNC),
                    null,
                    null,
                    null,
                    null,
                    null
                )

                val needsSync = cursor.use {
                    if (it.moveToFirst()) {
                        it.getInt(it.getColumnIndexOrThrow(AppDatabase.COLUMN_PENDING_SYNC)) == 1
                    } else {
                        true // If no user data exists, we need to sync
                    }
                }

                if (needsSync) {
                    Log.d("LoginSignUpActivity", "Pending syncs found, starting sync...")
                    // The sync will be handled automatically by OfflineFirstDataManager
                    onComplete(true)
                } else {
                    Log.d("LoginSignUpActivity", "No pending syncs found")
                    onComplete(true)
                }
            } catch (e: Exception) {
                Log.e("LoginSignUpActivity", "Error checking pending syncs", e)
                onComplete(false)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
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
