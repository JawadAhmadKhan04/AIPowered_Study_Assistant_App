package com.musketeers_and_me.ai_powered_study_assistant_app.OuterStructure.Settings

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
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

class SettingsFragment : Fragment() {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
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


        val logout_btn = view.findViewById<MaterialButton>(R.id.btn_logout)
        val save_btn = view.findViewById<MaterialButton>(R.id.btn_save)

        save_btn.setOnClickListener{
            Toast.makeText(requireContext(), "Settings saved", Toast.LENGTH_SHORT).show()
        }

        logout_btn.setOnClickListener {
            Toast.makeText(requireContext(), "Logging out...", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginSignUpActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish() // ⬅️ This finishes the host activity to prevent back navigation
        }

        groupApp.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(requireContext(), "Add in Group: $isChecked", Toast.LENGTH_SHORT).show()
            // Save setting or update preference
        }

        loginAuto.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(requireContext(), "Auto Login: $isChecked", Toast.LENGTH_SHORT).show()
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

                if (newPass != confirmPass) {
                    Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }

            dialog.show()
        }


        syncAuto.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(requireContext(), "Auto Sync: $isChecked", Toast.LENGTH_SHORT).show()
            // Save setting or update preference
        }

        quizSwitch.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(requireContext(), "Quiz Notifications: $isChecked", Toast.LENGTH_SHORT).show()
            // Save setting or update preference
        }

        studySwitch.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(requireContext(), "Study Reminders: $isChecked", Toast.LENGTH_SHORT).show()
        }

        // Add listeners for privacy/account/sync settings in the same way

        return view
    }
}
