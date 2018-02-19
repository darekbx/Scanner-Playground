package com.scannerplayground.ui

import android.app.Activity
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.scannerplayground.R
import com.scannerplayground.model.ScanResult
import com.scannerplayground.ssh.SSHClient
import io.reactivex.disposables.Disposable

class MainActivity : Activity() {

    class Fingerprint(val samples: List<ScanResult>)

    /*val wifiBroadcast
        get() = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                displayFingerprintCount()
                analyzeResults()
                requestScan()
            }
        }*/

    private var fingerprintCount = 0
    private var resultsCountSum = 0
    private var shouldScan = false
    private var scanResultsCached = ArrayList<ScanResult>()
    private var fingerprints = ArrayList<Fingerprint>()

    private var handle: Disposable? = null
    private var adapter: ScanResultAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //registerReceiver(wifiBroadcast, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScan()
    }

    fun onStart(view: View) {

        fingerprintCount = 0

        adapter = ScanResultAdapter(this)
        listView.adapter = adapter

        handle = SSHClient().open(30L, { setResponse(it) }, { showError(it) })

        //shouldScan = true
        //requestScan()
    }

    fun showError(message: String) {
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_LONG).show() }
    }

    fun setResponse(json: String) {
        if (TextUtils.isEmpty(json)) return
        val result = Gson().fromJson(json, Array<ScanResult>::class.java)

        fingerprintCount++

        runOnUiThread {
            countText.text = "Fingerprint count: $fingerprintCount"
            adapter?.let { adapter ->
                with(adapter) {
                    clear()
                    addAll(result.toList())
                    notifyDataSetChanged()
                }
            }
        }
    }

    fun onStop(view: View) {
        stopScan()
    }

    private fun stopScan() {
        handle?.dispose()
        //shouldScan = false
        //wifiManager.reconnect()
    }
/*
    private fun displayFingerprintCount() {
        fingerprintCount++
        //statusText.text = "Fingerprint count $fingerprintCount"
    }

    private fun analyzeResults() {
        resultsCountSum += wifiManager.scanResults.size
        val average = resultsCountSum / fingerprintCount
        //samplesCountAvgText.text = "Samples count average: $average"

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

        //fingerprintsCountUniqueText.text = "Unique fingerprints: ${fingerprints.size}"
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

        //samplesCountUniqueText.text = "Unique samples: ${scanResultsCached.size}"
    }

    private fun requestScan() {
        if (!shouldScan) return
        wifiManager.startScan()
    }
    private val statusText by lazy { null }
    private val samplesCountAvgText by lazy { null }
    private val samplesCountUniqueText by lazy { null }
    private val fingerprintsCountUniqueText by lazy { null }
*/
    private val wifiManager by lazy { applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager }
    private val listView by lazy { findViewById(R.id.list) as ListView }
    private val countText by lazy { findViewById(R.id.count) as TextView }
}