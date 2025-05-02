package com.musketeers_and_me.ai_powered_study_assistant_app.OuterStructure.Settings

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.*

import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.Opening_Registeration.LoginSignUpActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.core.content.edit
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.auth
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBReadOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBWriteOperations

class SettingsFragment : Fragment() {

    private  var quiz_noti : Boolean = false
    private  var study_reminder : Boolean = false
    private  var add_in_group : Boolean = false
    private  var auto_login : Boolean = true
    private  var auto_sync : Boolean = false

    private val databaseService: FBDataBaseService = FBDataBaseService()
    private val ReadOperations = FBReadOperations(databaseService)
    private val WriteOperations = FBWriteOperations(databaseService)

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        (activity as? MainActivity)?.apply {
            updateBottomNavIcon(R.id.nav_settings, R.drawable.settings_navbar_selected)
        }

        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Save and Logout button listeners
        view.findViewById<MaterialButton>(R.id.btn_save).setOnClickListener {
            Toast.makeText(requireContext(), "Settings saved", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<MaterialButton>(R.id.btn_logout).setOnClickListener {
            Toast.makeText(requireContext(), "Logging out...", Toast.LENGTH_SHORT).show()
            // Add logout logic here
        }



        // Example: Switches for Notification settings
        val quizSwitch = view.findViewById<SwitchMaterial>(R.id.switch_quiz_notifications)
        val studySwitch = view.findViewById<SwitchMaterial>(R.id.switch_study_reminders)
        val groupApp = view.findViewById<SwitchMaterial>(R.id.switch_add_in_group)
        val loginAuto = view.findViewById<SwitchMaterial>(R.id.switch_auto_login)
        val passwordChange = view.findViewById<SwitchMaterial>(R.id.switch_change_password)
        val syncAuto = view.findViewById<SwitchMaterial>(R.id.switch_auto_sync)

        ReadOperations.getSettings(
            onDataReceived = { quizNotifications, studyReminders, addInGroups, autoLogin, autoSync ->
                // Use the boolean values
                quiz_noti = quizNotifications
                study_reminder = studyReminders
                add_in_group = addInGroups
                auto_login = autoLogin
                auto_sync = autoSync
                quizSwitch.isChecked = quizNotifications
                studySwitch.isChecked = studyReminders
                groupApp.isChecked = addInGroups
                loginAuto.isChecked = autoLogin
                syncAuto.isChecked = autoSync
            },
            onError = { error ->
                // Handle any error that occurred during fetching
                Log.d("SettingsError", "Error: ${error.message}")
            }
        )

        val logout_btn = view.findViewById<MaterialButton>(R.id.btn_logout)
        val save_btn = view.findViewById<MaterialButton>(R.id.btn_save)

        save_btn.setOnClickListener{
            WriteOperations.saveSettings(quiz_noti, study_reminder, add_in_group, auto_login, auto_sync)
            Toast.makeText(requireContext(), "Settings saved", Toast.LENGTH_SHORT).show()
        }

        logout_btn.setOnClickListener {
            val sharedPreferences = requireActivity().getSharedPreferences("users_data", MODE_PRIVATE)
            sharedPreferences.edit() { clear() }
            Toast.makeText(requireContext(), "Logging out...", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginSignUpActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish() // ⬅️ This finishes the host activity to prevent back navigation
        }

        groupApp.setOnCheckedChangeListener { _, isChecked ->
            add_in_group = isChecked
//            Toast.makeText(requireContext(), "Add in Group: $isChecked", Toast.LENGTH_SHORT).show()
            // Save setting or update preference
        }

        loginAuto.setOnCheckedChangeListener { _, isChecked ->
            auto_login = isChecked
//            Toast.makeText(requireContext(), "Auto Login: $isChecked", Toast.LENGTH_SHORT).show()
            // Save setting or update preference
        }

        passwordChange.setOnCheckedChangeListener { buttonView, _ ->
            buttonView.isChecked = false

            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_change_password, null)

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            dialogView.findViewById<MaterialButton>(R.id.changePasswordButton).setOnClickListener {
                val oldPass = dialogView.findViewById<EditText>(R.id.et_old_password).text.toString()
                val newPass = dialogView.findViewById<EditText>(R.id.et_new_password).text.toString()
                val confirmPass = dialogView.findViewById<EditText>(R.id.et_confirm_password).text.toString()

                if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }


                if (newPass != confirmPass) {
                    Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                } else {
                    val user = Firebase.auth.currentUser
                    Log.d("SettingsFragment", "User: $user")
                    val email = user?.email

                    if (email != null) {
                        val credential = EmailAuthProvider.getCredential(email, oldPass)

                        user.reauthenticate(credential)
                            .addOnCompleteListener { reAuthTask ->
                                if (reAuthTask.isSuccessful) {
                                    // Now safe to change password
                                    user.updatePassword(newPass)
                                        .addOnCompleteListener { updateTask ->
                                            if (updateTask.isSuccessful) {
                                                Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show()
                                                dialog.dismiss()
                                            } else {
                                                Toast.makeText(requireContext(), "Failed to change password: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(requireContext(), "Incorrect Password entered", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }

                }
            }

            dialog.show()
        }


        syncAuto.setOnCheckedChangeListener { _, isChecked ->
            auto_sync = isChecked
//            Toast.makeText(requireContext(), "Auto Sync: $isChecked", Toast.LENGTH_SHORT).show()
            // Save setting or update preference
        }

        quizSwitch.setOnCheckedChangeListener { _, isChecked ->
            quiz_noti = isChecked
//            Toast.makeText(requireContext(), "Quiz Notifications: $isChecked", Toast.LENGTH_SHORT).show()
            // Save setting or update preference
        }

        studySwitch.setOnCheckedChangeListener { _, isChecked ->
            study_reminder = isChecked
//            Toast.makeText(requireContext(), "Study Reminders: $isChecked", Toast.LENGTH_SHORT).show()
        }

        // Add listeners for privacy/account/sync settings in the same way

        return view
    }
}
