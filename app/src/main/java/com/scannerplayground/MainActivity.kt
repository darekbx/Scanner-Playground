package com.scannerplayground

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import android.widget.TextView

class MainActivity : Activity() {

    val wifiBroadcast: BroadcastReceiver
        get() = object: BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                displayFingerprintCount()
                analyzeResults()
                requestScan()
            }
        }

    private var fingerprintCount = 0
    private var resultsCountSum = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerReceiver(wifiBroadcast, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
    }

    override fun onDestroy() {
        super.onDestroy()
        wifiManager.reconnect()
    }

    fun onStart(view: View) {
        requestScan()
    }

    fun onStop(view: View) {
        wifiManager.reconnect()
    }

    private fun displayFingerprintCount() {
        fingerprintCount++
        statusText.text = "Fingerprint count $fingerprintCount"
    }

    private fun analyzeResults() {

        resultsCountSum += wifiManager.scanResults.size
        val average = resultsCountSum / fingerprintCount
        samplesCountAvgText.text = "Samples count average: $average"
    }

    private fun requestScan() {
        wifiManager.startScan()
    }

    private val statusText by lazy { findViewById(R.id.status_text) as TextView }
    private val samplesCountAvgText by lazy { findViewById(R.id.samples_count_avg_text) as TextView }
    private val wifiManager by lazy { applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager }
}
