package com.musketeers_and_me.ai_powered_study_assistant_app.GroupStudy

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Window
import android.widget.EditText
import android.widget.ImageButton
import com.google.android.material.button.MaterialButton
import com.musketeers_and_me.ai_powered_study_assistant_app.R

class AddNoteDialog(
    context: Context,
    private val onAddClicked: (List<String>) -> Unit
) {
    private val dialog: Dialog = Dialog(context)
    private lateinit var searchInput: EditText
    private lateinit var topic1: EditText
    private lateinit var topic2: EditText
    private lateinit var topic3: EditText
    private lateinit var topic4: EditText
    private lateinit var addButton: MaterialButton
    private lateinit var removeButton: MaterialButton
    private lateinit var closeButton: ImageButton

    init {
        setupDialog()
    }

    private fun setupDialog() {
        val view = LayoutInflater.from(dialog.context).inflate(R.layout.dialog_add_note, null)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Initialize views
        searchInput = view.findViewById(R.id.searchInput)
        topic1 = view.findViewById(R.id.topic1)
        topic2 = view.findViewById(R.id.topic2)
        topic3 = view.findViewById(R.id.topic3)
        topic4 = view.findViewById(R.id.topic4)
        addButton = view.findViewById(R.id.addButton)
        removeButton = view.findViewById(R.id.removeButton)
        closeButton = view.findViewById(R.id.closeButton)

        // Setup search functionality
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Implement search functionality here
            }
        })

        // Setup click listeners
        closeButton.setOnClickListener {
            dismiss()
        }

        addButton.setOnClickListener {
            val topics = listOf(
                topic1.text.toString(),
                topic2.text.toString(),
                topic3.text.toString(),
                topic4.text.toString()
            ).filter { it.isNotEmpty() }
            
            if (topics.isNotEmpty()) {
                onAddClicked(topics)
                dismiss()
            }
        }

        removeButton.setOnClickListener {
            // Clear the selected topic (green background)
            topic3.text.clear()
        }
    }

    fun show() {
        dialog.show()
    }

    private fun dismiss() {
        dialog.dismiss()
    }
} 