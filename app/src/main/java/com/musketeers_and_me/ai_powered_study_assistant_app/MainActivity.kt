package com.musketeers_and_me.ai_powered_study_assistant_app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
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
// import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.OfflineFirstDataManager

class MainActivity : AppCompatActivity() {

    private val databaseService = FBDataBaseService()
    private var ReadOperations = FBReadOperations(databaseService)
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var binding: ActivityMainBinding
    private var currentMenuItemId: Int? = null

    private val defaultIcons = mapOf(
        R.id.nav_home to R.drawable.home_navbar,
        R.id.nav_profile to R.drawable.profile_navbar,
        R.id.nav_settings to R.drawable.settings_navbar,
        R.id.nav_noti to R.drawable.notifications_navbar
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        FirebaseApp.initializeApp(this)

        checkAuthentication()

        assignGlobalData()

        setContentView(binding.root)
        enableEdgeToEdge()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    moveTaskToBack(true)
                }
            }
        })

        // Setup toolbar
        ToolbarUtils.setupToolbar(this, "Home", R.drawable.home_logo_top_bar, false)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Home"

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        replaceFragment(HomeFragment())
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_settings->{
                    supportActionBar?.title = "Settings"
                    replaceFragment(SettingsFragment())
                    true
                }
                R.id.nav_home->{
                    supportActionBar?.title = "Home"
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_profile->{
                    supportActionBar?.title = "Profile"
                    replaceFragment(ProfileFragment())
                    true
                }
                R.id.nav_noti->{
                    supportActionBar?.title = "Notifications"
                    replaceFragment(NotificationsFragment())
                    true
                }
                else -> false
            }
        }

        findViewById<FrameLayout>(R.id.home_button_container).setOnClickListener {
            // Create intent for MainActivity
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
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
        checkAuthentication()
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

    private fun checkAuthentication() {
        val sharedPreferences = getSharedPreferences("users_data", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)
        
        if (userId == null) {
            // User not logged in, redirect to login
            val intent = Intent(this, LoginSignUpActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // User is logged in, initialize their data
        if (!GlobalData.done) {
            ReadOperations.autoLoginAllowed(this) { isAllowed ->
                if (!isAllowed) {
                    // Auto-login not allowed, redirect to login
                    val intent = Intent(this, LoginSignUpActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Initialize user session in DataManager
                    // (application as MyApplication).dataManager.initializeUserSession(userId) {
                    //     Log.d("MainActivity", "User session initialized successfully")
                    // }
                }
            }
        }
    }

    fun assignGlobalData() {
        val sharedPreferences = getSharedPreferences("users_data", MODE_PRIVATE)
        GlobalData.user_id = sharedPreferences.getString("user_id", null)
        GlobalData.user_name = sharedPreferences.getString("user_name", null)
        GlobalData.user_email = sharedPreferences.getString("user_email", null)
    }
}
