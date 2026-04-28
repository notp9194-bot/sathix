package com.sathix.app.core.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

enum class NetworkQuality { NONE, POOR, GOOD, EXCELLENT }

@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun isConnected(): Boolean {
        val active = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(active) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun quality(): NetworkQuality {
        val active = cm.activeNetwork ?: return NetworkQuality.NONE
        val caps = cm.getNetworkCapabilities(active) ?: return NetworkQuality.NONE
        if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) return NetworkQuality.NONE
        val down = caps.linkDownstreamBandwidthKbps
        return when {
            down >= 5000 -> NetworkQuality.EXCELLENT
            down >= 1000 -> NetworkQuality.GOOD
            down > 0 -> NetworkQuality.POOR
            else -> NetworkQuality.GOOD
        }
    }

    fun observe(): Flow<Boolean> = callbackFlow {
        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(true) }
            override fun onLost(network: Network) { trySend(false) }
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                trySend(caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
            }
        }
        val req = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(req, cb)
        trySend(isConnected())
        awaitClose { cm.unregisterNetworkCallback(cb) }
    }.distinctUntilChanged()
}
