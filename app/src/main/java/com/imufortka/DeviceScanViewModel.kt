package com.imufortka

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.*
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

private const val SCAN_PERIOD = 30000L

private const val TAG = "DeviceScanViewModel"


class DeviceScanViewModel(app: Application) : AndroidViewModel(app) {

    private val _viewState = MutableLiveData<DeviceScanViewState>()

    val viewState = _viewState as LiveData<DeviceScanViewState>

    private val scanResults = mutableMapOf<String, BluetoothDevice>()

    private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private var scanner: BluetoothLeScanner? = null

    private var scannCallback: DeviceScanCallback? = null

    private var scanFilters: List<ScanFilter>?=null

    private var scanSettings: ScanSettings?=null

  /*  init {

        startScan()
    }*/


    fun startScan(scanner1:Boolean) {
        scanFilters = buildScanFilters(scanner1)
        scanSettings = buildScanSettings()

        if (scannCallback == null) {
            Log.d(TAG, "Start Scanning")
            scanner = adapter.bluetoothLeScanner
            _viewState.value = DeviceScanViewState.ActiveScan

            Handler().postDelayed({ stopScanning() }, SCAN_PERIOD)

            scannCallback = DeviceScanCallback()
            scanner?.startScan(scanFilters, scanSettings, scannCallback)
        } else {
            Log.d(TAG, "Already scanning")
        }
    }


    private fun buildScanFilters(scanner1: Boolean): List<ScanFilter> {

        val builder = ScanFilter.Builder()

        if(scanner1){
            Log.i(TAG,"barcode"+App.getStringPrefernce(Constants.BARCODE1,""))
            builder.setServiceUuid(ParcelUuid(Constants.SERVICE_UUID))
        }else{
            builder.setServiceUuid(ParcelUuid(Constants.SERVICE_UUID))
        }
        val filter = builder.build()
        return listOf(filter)
    }

    private fun buildScanSettings(): ScanSettings {
        return ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()
    }

    private inner class DeviceScanCallback : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)

            if (results != null) {
                for (item in results) {
                    item.device?.let { device ->
                        scanResults[device.address] = device
                    }
                }
            }


            _viewState.value = DeviceScanViewState.ScanResults(scanResults)
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.device?.let { device ->
                scanResults[device.address] = device
            }
            _viewState.value = DeviceScanViewState.ScanResults(scanResults)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            val errorMessage = "Scan failed with error: $errorCode"
            _viewState.value = DeviceScanViewState.Error(errorMessage)
        }

    }

    private fun stopScanning() {
        scanner?.stopScan(scannCallback)
        scannCallback = null
        _viewState.value = DeviceScanViewState.ScanResults(scanResults)
    }

    override fun onCleared() {
        super.onCleared()
        stopScanning()
    }


}