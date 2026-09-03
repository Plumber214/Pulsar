package com.antigravity.pulsar.data.providers

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.net.wifi.WifiManager
import android.os.SystemClock
import com.antigravity.pulsar.model.NetworkState

class NetworkProvider(private val context: Context) {

    private var prevRxBytes: Long = 0L
    private var prevTxBytes: Long = 0L
    private var prevTimeMs: Long = 0L
    private val rxHistory = ArrayDeque<Float>(30)
    private val txHistory = ArrayDeque<Float>(30)

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    fun getNetworkState(): NetworkState {
        val now = SystemClock.elapsedRealtime()
        val curRx = TrafficStats.getTotalRxBytes()
        val curTx = TrafficStats.getTotalTxBytes()

        var rxSpeed = 0L
        var txSpeed = 0L

        if (prevTimeMs > 0L && now > prevTimeMs) {
            val deltaSec = (now - prevTimeMs) / 1000f
            if (deltaSec > 0f) {
                if (curRx >= prevRxBytes) rxSpeed = ((curRx - prevRxBytes) / deltaSec).toLong()
                if (curTx >= prevTxBytes) txSpeed = ((curTx - prevTxBytes) / deltaSec).toLong()
            }
        }

        prevRxBytes = curRx
        prevTxBytes = curTx
        prevTimeMs = now

        val rxMb = rxSpeed / (1024f * 1024f)
        val txMb = txSpeed / (1024f * 1024f)

        synchronized(rxHistory) {
            if (rxHistory.size >= 30) rxHistory.removeFirst()
            rxHistory.addLast(rxMb)
        }
        synchronized(txHistory) {
            if (txHistory.size >= 30) txHistory.removeFirst()
            txHistory.addLast(txMb)
        }

        val activeNetwork = connectivityManager?.activeNetwork
        val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)
        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val isCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true

        var wifiSsid = "Disconnected"
        var wifiSpeed = 0
        var wifiFreq = 0f

        if (isWifi) {
            val wifiInfo = wifiManager?.connectionInfo
            wifiSsid = wifiInfo?.ssid?.replace("\"", "") ?: "Connected Wi-Fi"
            wifiSpeed = wifiInfo?.linkSpeed ?: 0
            val freqMhz = wifiInfo?.frequency ?: 0
            wifiFreq = when {
                freqMhz >= 5945 -> 6.0f
                freqMhz >= 4900 -> 5.0f
                freqMhz in 2400..2500 -> 2.4f
                else -> 0f
            }
        }

        val cellularType = if (isCellular) "5G / LTE" else "Offline"

        return NetworkState(
            downloadBytesPerSec = rxSpeed,
            uploadBytesPerSec = txSpeed,
            isWifiConnected = isWifi,
            isCellularConnected = isCellular,
            wifiSsid = wifiSsid,
            wifiLinkSpeedMbps = wifiSpeed,
            wifiFrequencyGhz = wifiFreq,
            cellularNetworkType = cellularType,
            downloadHistoryMb = rxHistory.toList(),
            uploadHistoryMb = txHistory.toList()
        )
    }
}