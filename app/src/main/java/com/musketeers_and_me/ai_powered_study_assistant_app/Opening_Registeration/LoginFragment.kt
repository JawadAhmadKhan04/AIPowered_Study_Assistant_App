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

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val forgotpassword = view.findViewById<TextView>(R.id.tvForgotPassword)
        val loginButton = view.findViewById<MaterialButton>(R.id.loginButton)
        val emailField = view.findViewById<EditText>(R.id.login_email)
        val passwordField = view.findViewById<EditText>(R.id.login_password)

        // Initialize Firebase Auth
        auth = (activity as LoginSignUpActivity).getFirebaseAuth()

        forgotpassword.setOnClickListener{
            val email = emailField.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Enter your email to reset password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener { task1 ->
                    if (task1.isSuccessful) {
                        val signInMethods = task1.result?.signInMethods ?: emptyList()
                        Log.d("ResetPassword", "SignInMethods: $signInMethods")

                      //  if (signInMethods.isNotEmpty()) {
                            // Email is registered
                            auth.sendPasswordResetEmail(email)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(requireContext(), "Password reset email sent", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(requireContext(), "Failed to send reset email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            // Email exists, but no sign-in methods = not registered
                            Toast.makeText(requireContext(), "Email is not registered", Toast.LENGTH_SHORT).show()
                        }
                    }
//            else {
//                        Toast.makeText(
//                            requireContext(),
//                            "Error checking email: ${task1.exception?.message}",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }

        }



        loginButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        val currentUser = auth.currentUser
                        val name = currentUser?.displayName ?: "Unknown"

                        Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()



                        // Save user info to shared preferences
                        val sharedPreferences = requireActivity().getSharedPreferences("users_data", Context.MODE_PRIVATE)
                        sharedPreferences.edit() {
                            putString("user_name", name)
                            putString("user_email", email)
                            putString("user_id", currentUser?.uid)
                        } // Correct usage of apply


                        // Save user data globally
                        GlobalData.user_id = currentUser?.uid
                        GlobalData.user_name = name
                        GlobalData.user_email = email

                        GlobalData.done = true

                        Log.d("LoginFragment", "User ID: ${GlobalData.user_id}")

                        // Intent to open MainActivity
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish() // Close login activity so user can't go back


                    } else {
                        Toast.makeText(requireContext(), "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
