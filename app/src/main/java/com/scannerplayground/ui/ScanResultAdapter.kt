package com.scannerplayground.ui

import android.content.Context
import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.scannerplayground.R
import com.scannerplayground.databinding.AdapterResultBinding
import com.scannerplayground.model.ScanResult

class ScanResultAdapter(context: Context): ArrayAdapter<ScanResult>(context, R.layout.adapter_result) {

    override fun getCount(): Int {
        return super.getCount()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?) =
            when (convertView) {
                null -> DataBindingUtil.inflate(inflater, R.layout.adapter_result, parent, false) as AdapterResultBinding
                else -> DataBindingUtil.getBinding(convertView) as AdapterResultBinding
            }
                    .apply { result = getItem(position) }
                    .root

    private val inflater by lazy {
        LayoutInflater.from(context)
    }
}