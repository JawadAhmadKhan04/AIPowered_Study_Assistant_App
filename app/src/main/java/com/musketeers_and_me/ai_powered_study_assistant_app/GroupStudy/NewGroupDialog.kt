package com.musketeers_and_me.ai_powered_study_assistant_app.GroupStudy

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.NetworkConnectivityMonitor
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.OfflineFirstDataManager
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.util.Log
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class NewGroupDialog : DialogFragment() {
    private lateinit var nameEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var createButton: MaterialButton
    private lateinit var closeButton: ImageButton
    private lateinit var dataManager: OfflineFirstDataManager
    private lateinit var networkMonitor: NetworkConnectivityMonitor
    private val TAG = "NewGroupDialog"

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

        // Initialize data manager and network monitor
        dataManager = OfflineFirstDataManager.getInstance(requireContext())
        networkMonitor = NetworkConnectivityMonitor(requireContext())
        
        // Start network monitoring
        networkMonitor.startMonitoring()
        
        // Initialize UI components
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

            // Show loading state
            createButton.isEnabled = false
            createButton.text = "Creating..."
            
            // Check for internet connectivity first
            lifecycleScope.launch {
                try {
                    // Check if internet is available
                    val isOnline = checkInternetConnectivity()
                    
                    if (!isOnline) {
                        // No internet connection, show toast and return
                        activity?.runOnUiThread {
                            createButton.isEnabled = true
                            createButton.text = "Create"
                            Toast.makeText(
                                context,
                                "Internet connection required. Please try again when online.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return@launch
                    }
                    
                    // Internet is available, proceed with creating the group
                    // Initialize data manager if needed
                    if (!dataManager.isInitialized) {
                        dataManager.initialize()
                    }
                    
                    val groupId = dataManager.createStudyGroup(name, description)
                    
                    // Handle result on main thread
                    activity?.runOnUiThread {
                        createButton.isEnabled = true
                        createButton.text = "Create"
                        
                        if (groupId != null) {
                            Log.d(TAG, "Group created successfully with ID: $groupId")
                            Toast.makeText(context, "Group created successfully!", Toast.LENGTH_SHORT).show()
                            groupCreatedListener?.onGroupCreated()
                            dismiss()
                        } else {
                            Log.e(TAG, "Failed to create group")
                            Toast.makeText(context, "Failed to create group", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating group", e)
                    
                    // Handle error on main thread
                    activity?.runOnUiThread {
                        createButton.isEnabled = true
                        createButton.text = "Create"
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        closeButton.setOnClickListener {
            dismiss()
        }
    }
    
    override fun onDestroyView() {
        // Stop network monitoring
        networkMonitor.stopMonitoring()
        super.onDestroyView()
    }
    
    private fun checkInternetConnectivity(): Boolean {
        // Get connectivity manager
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        // Check network capabilities
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        
        Log.d(TAG, "Manual connectivity check: ${if (isConnected) "Connected" else "Disconnected"}")
        return isConnected
    }
}