package com.imufortka

import android.bluetooth.BluetoothDevice

sealed class DeviceScanViewState {

        object ActiveScan:DeviceScanViewState()
        class ScanResults(val scanResults:Map<String,BluetoothDevice>):DeviceScanViewState()
        class Error(val message:String):DeviceScanViewState()


}


