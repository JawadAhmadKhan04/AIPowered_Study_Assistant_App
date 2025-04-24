package com.musketeers_and_me.ai_powered_study_assistant_app.GroupStudy

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.ImageButton
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.musketeers_and_me.ai_powered_study_assistant_app.R

class JoinGroupDialog(
    context: Context,
    private val onJoinClicked: (String) -> Unit
) {
    private val dialog: Dialog = Dialog(context)
    private lateinit var secretCodeInput: TextInputEditText
    private lateinit var joinButton: MaterialButton
    private lateinit var closeButton: ImageButton

    init {
        setupDialog()
    }

    private fun setupDialog() {
        val view = LayoutInflater.from(dialog.context).inflate(R.layout.dialog_join_group, null)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Initialize views
        secretCodeInput = view.findViewById(R.id.secretCodeInput)
        joinButton = view.findViewById(R.id.joinButton)
        closeButton = view.findViewById(R.id.closeButton)

        // Set click listeners
        closeButton.setOnClickListener {
            dismiss()
        }

        joinButton.setOnClickListener {
            val code = secretCodeInput.text?.toString()?.trim() ?: ""
            if (code.isNotEmpty()) {
                onJoinClicked(code)
                dismiss()
            }
        }
    }

    fun show() {
        dialog.show()
    }

    private fun dismiss() {
        dialog.dismiss()
    }
} 