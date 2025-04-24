package com.musketeers_and_me.ai_powered_study_assistant_app.GroupStudy

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.musketeers_and_me.ai_powered_study_assistant_app.R

class NewGroupDialog(
    context: Context,
    private val onSaveClicked: (name: String, description: String, code: String) -> Unit
) {
    private val dialog: Dialog = Dialog(context)
    private lateinit var groupNameInput: TextInputEditText
    private lateinit var groupDescriptionInput: TextInputEditText
    private lateinit var groupCodeInput: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var closeButton: ImageButton

    init {
        setupDialog()
    }

    private fun setupDialog() {
        val view = LayoutInflater.from(dialog.context).inflate(R.layout.dialog_new_group, null)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Initialize views
        groupNameInput = view.findViewById(R.id.groupNameInput)
        groupDescriptionInput = view.findViewById(R.id.groupDescriptionInput)
        groupCodeInput = view.findViewById(R.id.groupCodeInput)
        saveButton = view.findViewById(R.id.saveButton)
        closeButton = view.findViewById(R.id.closeButton)

        // Set click listeners
        closeButton.setOnClickListener {
            dismiss()
        }

        saveButton.setOnClickListener {
            val name = groupNameInput.text?.toString()?.trim() ?: ""
            val description = groupDescriptionInput.text?.toString()?.trim() ?: ""
            val code = groupCodeInput.text?.toString()?.trim() ?: ""

            when {
                name.isEmpty() -> {
                    showToast("Please enter a group name")
                }
                description.isEmpty() -> {
                    showToast("Please enter a group description")
                }
                code.isEmpty() -> {
                    showToast("Please enter a 6-digit code")
                }
                code.length != 6 -> {
                    showToast("Code must be 6 digits")
                }
                else -> {
                    onSaveClicked(name, description, code)
                    dismiss()
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(dialog.context, message, Toast.LENGTH_SHORT).show()
    }

    fun show() {
        dialog.show()
    }

    private fun dismiss() {
        dialog.dismiss()
    }
}