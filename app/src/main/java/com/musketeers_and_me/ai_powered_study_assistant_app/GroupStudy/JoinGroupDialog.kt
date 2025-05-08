package com.musketeers_and_me.ai_powered_study_assistant_app.GroupStudy

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBWriteOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.R

class JoinGroupDialog : DialogFragment() {
    private lateinit var codeEditText: TextInputEditText
    private lateinit var joinButton: MaterialButton
    private lateinit var closeButton: MaterialButton
    private val databaseService = FBDataBaseService()
    private val writeOperations = FBWriteOperations(databaseService)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_join_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        codeEditText = view.findViewById(R.id.secretCodeInput)
        joinButton = view.findViewById(R.id.joinButton)
        closeButton = view.findViewById(R.id.closeButton)

        joinButton.setOnClickListener {
            val code = codeEditText.text.toString()

            if (code.isBlank()) {
                codeEditText.error = "Group code is required"
                return@setOnClickListener
            }

            writeOperations.joinStudyGroup(code) { success ->
                if (success) {
                    Toast.makeText(context, "Joined group successfully!", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    Toast.makeText(context, "Invalid group code", Toast.LENGTH_SHORT).show()
                }
            }
        }

        closeButton.setOnClickListener {
            dismiss()
        }
    }
} 