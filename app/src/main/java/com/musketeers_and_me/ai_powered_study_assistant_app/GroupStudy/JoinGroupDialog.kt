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
import android.util.Log
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class JoinGroupDialog : DialogFragment() {
    private lateinit var codeEditText: TextInputEditText
    private lateinit var joinButton: MaterialButton
    private lateinit var closeButton: ImageButton
    private lateinit var dataManager: OfflineFirstDataManager
    private lateinit var networkMonitor: NetworkConnectivityMonitor
    private var onGroupJoinedListener: OnGroupJoinedListener? = null
    private val TAG = "JoinGroupDialog"

    // Interface to notify the activity when a group is joined
    interface OnGroupJoinedListener {
        fun onGroupJoined()
    }

    // Method to set the listener from the activity
    fun setOnGroupJoinedListener(listener: OnGroupJoinedListener) {
        this.onGroupJoinedListener = listener
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
        return inflater.inflate(R.layout.dialog_join_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize data manager and network monitor
        dataManager = OfflineFirstDataManager.getInstance(requireContext())
        networkMonitor = NetworkConnectivityMonitor(requireContext())
        
        // Start network monitoring
        networkMonitor.startMonitoring()
        
        codeEditText = view.findViewById(R.id.secretCodeInput)
        joinButton = view.findViewById(R.id.joinButton)
        closeButton = view.findViewById(R.id.closeButton)

        joinButton.setOnClickListener {
            val code = codeEditText.text.toString()
            Log.d(TAG, "Join button clicked with code: $code")

            if (code.isBlank()) {
                codeEditText.error = "Group code is required"
                return@setOnClickListener
            }

            // Show loading state
            joinButton.isEnabled = false
            joinButton.text = "Joining..."

            // Check for internet connectivity first
            lifecycleScope.launch {
                try {
                    // Check if internet is available
                    val isOnline = checkInternetConnectivity()
                    
                    if (!isOnline) {
                        // No internet connection, show toast and return
                        activity?.runOnUiThread {
                            joinButton.isEnabled = true
                            joinButton.text = "Join"
                            Toast.makeText(
                                context,
                                "Internet connection required. Please try again when online.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return@launch
                    }
                    
                    // Internet is available, proceed with joining the group
                    // Initialize data manager if needed
                    if (!dataManager.isInitialized) {
                        dataManager.initialize()
                    }
                    
                    val success = dataManager.joinStudyGroup(code)
                    
                    // Handle result on main thread
                    activity?.runOnUiThread {
                        joinButton.isEnabled = true
                        joinButton.text = "Join"
                        
                        if (success) {
                            Log.d(TAG, "Successfully joined group with code: $code")
                            Toast.makeText(context, "Joined group successfully!", Toast.LENGTH_SHORT).show()
                            onGroupJoinedListener?.onGroupJoined()
                            dismiss()
                        } else {
                            Log.e(TAG, "Failed to join group with code: $code")
                            Toast.makeText(context, "Invalid group code or error joining group", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error joining group", e)
                    
                    // Handle error on main thread
                    activity?.runOnUiThread {
                        joinButton.isEnabled = true
                        joinButton.text = "Join"
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