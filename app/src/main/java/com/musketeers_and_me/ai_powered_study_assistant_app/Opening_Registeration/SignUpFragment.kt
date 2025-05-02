package com.musketeers_and_me.ai_powered_study_assistant_app.Opening_Registeration

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import androidx.fragment.app.Fragment
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import androidx.core.content.edit
import com.musketeers_and_me.ai_powered_study_assistant_app.AuthService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBWriteOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.GlobalData

class SignUpFragment : Fragment() {
//    private val authService = AuthService()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    @SuppressLint("CommitPrefEdits")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val auth = (activity as LoginSignUpActivity).getFirebaseAuth()
        val nameField = view.findViewById<EditText>(R.id.name)
        val emailField = view.findViewById<EditText>(R.id.email)
        val passwordField = view.findViewById<EditText>(R.id.password)
        val confirmPasswordField = view.findViewById<EditText>(R.id.confirm_password)

        val signupButton = view.findViewById<MaterialButton>(R.id.signupButton)

        signupButton.setOnClickListener {
            val email = emailField.text.toString()
            val name = nameField.text.toString()
            val pass = passwordField.text.toString()
            val confirmpass = confirmPasswordField.text.toString()

            if (email.isEmpty() || pass.isEmpty() || name.isEmpty() || confirmpass.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (pass != confirmpass) {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if the email already exists
            auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener { task1 ->
                    if (task1.isSuccessful) {
                        val signInMethods = task1.result?.signInMethods
                        if (signInMethods != null && signInMethods.isNotEmpty()) {
                            // Email is already in use
                            Toast.makeText(context, "This email is already registered", Toast.LENGTH_SHORT).show()
                        } else {
                            // Email is not registered, proceed with the signup
                            auth.createUserWithEmailAndPassword(email, pass)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val currentuser = task.result?.user?.uid



//                                        val currentuser = authService.getCurrentUserId().toString()


                                        val sharedPreferences = requireActivity().getSharedPreferences("users_data", Context.MODE_PRIVATE)
                                        sharedPreferences.edit() {
                                            putString("user_name", name)
                                            putString("user_email", email)
                                            putString("user_id", currentuser)
                                        }
                                        GlobalData.user_id = currentuser
                                        GlobalData.user_name = name
                                        GlobalData.user_email = email
                                        GlobalData.done = true

                                        Log.d("TEST", "Registered1")
                                        Log.d("TEST", "User ID: $currentuser")

                                        val databaseService = FBDataBaseService()
                                        val writeOperations = FBWriteOperations(databaseService)

                                        writeOperations.saveSettings(quizNotifications = false, studyReminders = false, addInGroups = false, autoLogin = true, autoSync = false)
                                        Log.d("TEST", "Registered2")


                                        Toast.makeText(context, "Account created successfully", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(requireContext(), MainActivity::class.java)
                                        startActivity(intent)
                                        requireActivity().finish()
                                    } else {
                                        Toast.makeText(context, "Signup failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                        }
                    } else {
                        // Handle any errors from fetchSignInMethodsForEmail
                        Toast.makeText(context, "Error checking email: ${task1.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }
}
