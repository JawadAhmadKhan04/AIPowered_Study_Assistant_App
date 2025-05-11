package com.musketeers_and_me.ai_powered_study_assistant_app.GroupStudy

import NoteAdapter
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.musketeers_and_me.ai_powered_study_assistant_app.LectureAndNotes.NoteItem
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBReadOperations

class AddNoteDialog : DialogFragment() {
    private lateinit var searchInput: EditText
    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var addButton: MaterialButton
    private lateinit var removeButton: MaterialButton
    private lateinit var closeButton: ImageButton
    private var onNoteSelected: ((NoteItem) -> Unit)? = null
    private var allNotes: List<NoteItem> = emptyList()
    private lateinit var noteAdapter: NoteAdapter

    private var databaseService = FBDataBaseService()
    private var ReadOperations = FBReadOperations(databaseService)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        searchInput = view.findViewById(R.id.searchInput)
        notesRecyclerView = view.findViewById(R.id.notesRecyclerView)
        addButton = view.findViewById(R.id.addButton)
        removeButton = view.findViewById(R.id.removeButton)
        closeButton = view.findViewById(R.id.closeButton)

        // Setup RecyclerView
        notesRecyclerView.layoutManager = LinearLayoutManager(context)
        noteAdapter = NoteAdapter(emptyList()) { note ->
            onNoteSelected?.invoke(note)
            dismiss()
        }
        notesRecyclerView.adapter = noteAdapter

        // Setup search functionality
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterNotes(s?.toString() ?: "")
            }
        })

        // Setup click listeners
        closeButton.setOnClickListener {
            dismiss()
        }

        addButton.setOnClickListener {
            // Handle add button click
            dismiss()
        }

        removeButton.setOnClickListener {
            // Handle remove button click
            dismiss()
        }

        // Fetch all notes from Firebase
        fetchAllNotes()
    }

    private fun fetchAllNotes() {
        ReadOperations.getAllNotes { notes ->
            allNotes = notes
            noteAdapter = NoteAdapter(allNotes) { note ->
                onNoteSelected?.invoke(note)
                dismiss()
            }
            notesRecyclerView.adapter = noteAdapter
        }
    }

    private fun filterNotes(query: String) {
        val filteredNotes = if (query.isEmpty()) {
            allNotes
        } else {
            allNotes.filter { note ->
                note.title.contains(query, ignoreCase = true)
            }
        }
        noteAdapter = NoteAdapter(filteredNotes) { note ->
            onNoteSelected?.invoke(note)
            dismiss()
        }
        notesRecyclerView.adapter = noteAdapter
    }

    companion object {
        fun newInstance(onNoteSelected: (NoteItem) -> Unit): AddNoteDialog {
            return AddNoteDialog().apply {
                this.onNoteSelected = onNoteSelected
            }
        }
    }
}