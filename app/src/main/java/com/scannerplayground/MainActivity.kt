package com.scannerplayground

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import android.widget.TextView

class MainActivity : Activity() {

    class Fingerprint(val samples: List<ScanResult>)

    val wifiBroadcast
        get() = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                displayFingerprintCount()
                analyzeResults()
                requestScan()
            }
        }

    private var fingerprintCount = 0
    private var resultsCountSum = 0
    private var shouldScan = false
    private var scanResultsCached = ArrayList<ScanResult>()
    private var fingerprints = ArrayList<Fingerprint>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerReceiver(wifiBroadcast, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScan()
    }

    fun onStart(view: View) {
        shouldScan = true
        requestScan()
    }

    fun onStop(view: View) {
        stopScan()
    }

    private fun stopScan() {
        shouldScan = false
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

        countUniqueSamples()
        countUniqueFingerprints()
    }

    private fun countUniqueFingerprints() {
        var canBeAdded = true
        fingerprints.forEach { fingerprint ->
            wifiManager.scanResults.forEach { scanResult ->
                val sameCount = calculateSameCount(fingerprint.samples, scanResult)
                if (sameCount == fingerprint.samples.size) {
                    canBeAdded = false
                }
            }
        }
        if (canBeAdded)
            fingerprints.add(Fingerprint(wifiManager.scanResults))

        fingerprintsCountUniqueText.text = "Unique fingerprints: ${fingerprints.size}"
    }

    private fun calculateSameCount(samples: List<ScanResult>, scanResult: ScanResult) =
            samples.count { p ->
                p.SSID == scanResult.SSID
                        && p.BSSID == scanResult.BSSID
                        && p.frequency == scanResult.frequency
                        && p.level == scanResult.level
            }

    private fun countUniqueSamples() {
        wifiManager.scanResults.forEach { scanResult ->
            val sameCount = calculateSameCount(scanResultsCached, scanResult)
            if (sameCount == 0)
                scanResultsCached.add(scanResult)
        }

        samplesCountUniqueText.text = "Unique samples: ${scanResultsCached.size}"
    }

    private fun requestScan() {
        if (!shouldScan) return
        wifiManager.startScan()
    }

    private val statusText by lazy { findViewById(R.id.status_text) as TextView }
    private val samplesCountAvgText by lazy { findViewById(R.id.samples_count_avg_text) as TextView }
    private val samplesCountUniqueText by lazy { findViewById(R.id.samples_unique_count) as TextView }
    private val fingerprintsCountUniqueText by lazy { findViewById(R.id.fingerptints_unique_count) as TextView }
    private val wifiManager by lazy { applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager }
}