package com.musketeers_and_me.ai_powered_study_assistant_app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.AppDatabase
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBReadOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.Opening_Registeration.LoginSignUpActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.OuterStructure.Home.HomeFragment
import com.musketeers_and_me.ai_powered_study_assistant_app.OuterStructure.Notifications.NotificationsFragment
import com.musketeers_and_me.ai_powered_study_assistant_app.databinding.ActivityMainBinding
import com.musketeers_and_me.ai_powered_study_assistant_app.OuterStructure.Profile.ProfileFragment
import com.musketeers_and_me.ai_powered_study_assistant_app.OuterStructure.Settings.SettingsFragment
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.GlobalData
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val databaseService = FBDataBaseService()
    private var ReadOperations = FBReadOperations(databaseService)
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var binding: ActivityMainBinding
    private var currentMenuItemId: Int? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isInitialized = false
    private lateinit var auth: FirebaseAuth

    private val defaultIcons = mapOf(
        R.id.nav_home to R.drawable.home_navbar,
        R.id.nav_profile to R.drawable.profile_navbar,
        R.id.nav_settings to R.drawable.settings_navbar,
        R.id.nav_noti to R.drawable.notifications_navbar
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize UI components first
        setupUI()
        
        // Check authentication only once at startup
        if (savedInstanceState == null) {
            scope.launch(Dispatchers.IO) {
                try {
                    checkAuthentication()
                } catch (e: Exception) {
                    Log.e(TAG, "Error during authentication check", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Error initializing app", Toast.LENGTH_SHORT).show()
                        // Force redirect to login on error
                        startActivity(Intent(this@MainActivity, LoginSignUpActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }

    private fun setupUI() {
        // Setup toolbar
        ToolbarUtils.setupToolbar(this, "Home", R.drawable.home_logo_top_bar, false)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Home"

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        replaceFragment(HomeFragment())
        
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_settings -> {
                    supportActionBar?.title = "Settings"
                    replaceFragment(SettingsFragment())
                    true
                }
                R.id.nav_home -> {
                    supportActionBar?.title = "Home"
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_profile -> {
                    supportActionBar?.title = "Profile"
                    replaceFragment(ProfileFragment())
                    true
                }
                R.id.nav_noti -> {
                    supportActionBar?.title = "Notifications"
                    replaceFragment(NotificationsFragment())
                    true
                }
                else -> false
            }
        }

        findViewById<FrameLayout>(R.id.home_button_container).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    moveTaskToBack(true)
                }
            }
        })
    }

    @SuppressLint("SuspiciousIndentation")
    fun replaceFragment(fragment: Fragment) {
        var transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onResume() {
        super.onResume()
        // Only check if not initialized
        if (!isInitialized) {
            scope.launch(Dispatchers.IO) {
                try {
                    checkAuthentication()
                } catch (e: Exception) {
                    Log.e(TAG, "Error during authentication check in onResume", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Error initializing app", Toast.LENGTH_SHORT).show()
                        // Force redirect to login on error
                        startActivity(Intent(this@MainActivity, LoginSignUpActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }

    private fun hideBottomNavigationView() {
        bottomNavigationView.visibility = View.GONE
    }

    fun showBottomNavigationView() {
        bottomNavigationView.visibility = View.VISIBLE
    }

    fun updateBottomNavIcon(menuItemId: Int, activeIconResId: Int) {
        // Reset previous icon
        currentMenuItemId?.let { previousId ->
            val defaultIcon = defaultIcons[previousId]
            if (defaultIcon != null) {
                binding.bottomNavigation.menu.findItem(previousId)?.setIcon(defaultIcon)
            }
        }

        // Set new active icon
        binding.bottomNavigation.menu.findItem(menuItemId)?.setIcon(activeIconResId)
        currentMenuItemId = menuItemId
    }

    private suspend fun checkAuthentication() {
        try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.d(TAG, "No user found, redirecting to login")
                withContext(Dispatchers.Main) {
                    startActivity(Intent(this@MainActivity, LoginSignUpActivity::class.java))
                    finish()
                }
                return
            }

            Log.d(TAG, "User authenticated: ${currentUser.uid}")

            // Initialize data manager if not already initialized
            if (!isInitialized) {
                try {
                    (application as MyApplication).dataManager.initialize()
                    isInitialized = true
                    Log.d(TAG, "Data manager initialized successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing data manager", e)
                    throw e
                }
            }

            // Load user data
            withContext(Dispatchers.Main) {
                loadUserData(currentUser.uid)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in checkAuthentication", e)
            throw e
        }
    }

    fun assignGlobalData() {
        val sharedPreferences = getSharedPreferences("users_data", MODE_PRIVATE)
        GlobalData.user_id = sharedPreferences.getString("user_id", null)
        GlobalData.user_name = sharedPreferences.getString("user_name", null)
        GlobalData.user_email = sharedPreferences.getString("user_email", null)
    }

    private fun loadUserData(userId: String) {
        // Load user data from Firebase
        ReadOperations.getUser(userId) { user ->
            if (user != null) {
                // Update UI with user data
                GlobalData.user_id = userId
                GlobalData.user_name = user.username
                GlobalData.user_email = user.email
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
