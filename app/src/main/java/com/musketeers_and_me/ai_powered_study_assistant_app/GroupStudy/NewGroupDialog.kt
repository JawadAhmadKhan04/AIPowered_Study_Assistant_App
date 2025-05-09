package com.musketeers_and_me.ai_powered_study_assistant_app.GroupStudy

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.musketeers_and_me.ai_powered_study_assistant_app.AuthService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBWriteOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.R

class NewGroupDialog : DialogFragment() {
    private lateinit var nameEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var createButton: MaterialButton
    private lateinit var closeButton: ImageButton
    private val databaseService = FBDataBaseService()
    private val writeOperations = FBWriteOperations(databaseService)
    private val authService = AuthService()

    interface OnGroupCreatedListener {
        fun onGroupCreated()
    }

    private var groupCreatedListener: OnGroupCreatedListener? = null

    fun setOnGroupCreatedListener(listener: OnGroupCreatedListener) {
        groupCreatedListener = listener
    }

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
        return inflater.inflate(R.layout.dialog_new_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameEditText = view.findViewById(R.id.groupNameInput)
        descriptionEditText = view.findViewById(R.id.groupDescriptionInput)
        createButton = view.findViewById(R.id.saveButton)
        closeButton = view.findViewById(R.id.closeButton)

        createButton.setOnClickListener {

            val name = nameEditText.text.toString()
            val description = descriptionEditText.text.toString()

            if (name.isBlank()) {
                nameEditText.error = "Group name is required"
                return@setOnClickListener
            }

            writeOperations.createStudyGroup(name, description) { groupId ->
                if (groupId != null) {
                    Toast.makeText(context, "Group created successfully!", Toast.LENGTH_SHORT).show()
                    groupCreatedListener?.onGroupCreated()
                    dismiss()
                } else {
                    Toast.makeText(context, "Failed to create group", Toast.LENGTH_SHORT).show()
                }
            }
        }

        closeButton.setOnClickListener {
            dismiss()
        }
    }
}