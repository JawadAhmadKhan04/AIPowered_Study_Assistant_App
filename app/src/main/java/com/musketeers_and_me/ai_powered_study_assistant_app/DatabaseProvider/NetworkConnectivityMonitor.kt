package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NetworkConnectivityMonitor(private val context: Context) {
    private val TAG = "NetworkMonitor"
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Network became available")
            _isOnline.value = true
        }

        override fun onLost(network: Network) {
            Log.d(TAG, "Network connection lost")
            _isOnline.value = false
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            val unmetered = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            val wifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            val cellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            
            Log.d(TAG, "Network capabilities changed:")
            Log.d(TAG, "- Unmetered: $unmetered")
            Log.d(TAG, "- WiFi: $wifi")
            Log.d(TAG, "- Cellular: $cellular")
        }
    }

    fun startMonitoring() {
        Log.d(TAG, "Starting network monitoring")
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        
        // Check initial state
        val currentNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(currentNetwork)
        val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        Log.d(TAG, "Initial network state: ${if (isConnected) "Connected" else "Disconnected"}")
        _isOnline.value = isConnected
    }

    fun stopMonitoring() {
        Log.d(TAG, "Stopping network monitoring")
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
} 