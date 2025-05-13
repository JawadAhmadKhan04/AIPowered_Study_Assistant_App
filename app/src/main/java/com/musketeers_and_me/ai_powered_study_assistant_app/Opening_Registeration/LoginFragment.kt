package com.musketeers_and_me.ai_powered_study_assistant_app.Opening_Registeration

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.edit
import com.google.android.material.button.MaterialButton
import androidx.fragment.app.Fragment
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.GlobalData
import com.google.firebase.auth.FirebaseAuth
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.cancel
import kotlinx.coroutines.tasks.await
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.OfflineFirstDataManager

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: AppDatabase
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getInstance(requireContext())
        
        val forgotpassword = view.findViewById<TextView>(R.id.tvForgotPassword)
        val loginButton = view.findViewById<MaterialButton>(R.id.loginButton)
        val emailField = view.findViewById<EditText>(R.id.login_email)
        val passwordField = view.findViewById<EditText>(R.id.login_password)

        // Initialize Firebase Auth
        auth = (activity as LoginSignUpActivity).getFirebaseAuth()

        // Check if already logged in
        if (auth.currentUser != null) {
            navigateToMainActivity()
            return
        }

        forgotpassword.setOnClickListener {
            val email = emailField.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Enter your email to reset password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            scope.launch {
                try {
                    val signInMethods = withContext(Dispatchers.IO) {
                        auth.fetchSignInMethodsForEmail(email).await()
                    }

                    if (signInMethods.signInMethods?.isNotEmpty() == true) {
                        withContext(Dispatchers.IO) {
                            auth.sendPasswordResetEmail(email).await()
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Password reset email sent", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Email is not registered", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        loginButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show loading indicator
            loginButton.isEnabled = false
            loginButton.text = "Logging in..."

            scope.launch {
                try {
                    // Authenticate user
                    val authResult = withContext(Dispatchers.IO) {
                        auth.signInWithEmailAndPassword(email, password).await()
                    }

                    val currentUser = authResult.user
                    if (currentUser != null) {
                        val name = currentUser.displayName ?: "Unknown"

                        // Save user info to shared preferences
                        withContext(Dispatchers.IO) {
                            val sharedPreferences = requireActivity().getSharedPreferences("users_data", Context.MODE_PRIVATE)
                            sharedPreferences.edit {
                                putString("user_name", name)
                                putString("user_email", email)
                                putString("user_id", currentUser.uid)
                            }
                        }

                        // Save user data globally
                        withContext(Dispatchers.Main) {
                            GlobalData.user_id = currentUser.uid
                            GlobalData.user_name = name
                            GlobalData.user_email = email
                            GlobalData.done = true
                        }

                        // Initialize data manager which will start sync
                        withContext(Dispatchers.IO) {
                            try {
                                val dataManager = OfflineFirstDataManager.getInstance(requireContext())
                                dataManager.initialize() // This will start the sync process
                            } catch (e: Exception) {
                                Log.e("LoginFragment", "Error during initialization", e)
                            }
                        }

                        // Navigate to MainActivity after sync is initiated
                        navigateToMainActivity()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("LoginFragment", "Authentication failed", e)
                        Toast.makeText(requireContext(), "Authentication failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        loginButton.isEnabled = true
                        loginButton.text = "Login"
                    }
                }
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
