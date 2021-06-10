package com.imufortka

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.imufortka.databinding.ActivityPodScanBinding

class PodScanActivity : AppCompatActivity() {


    private lateinit var activityPodScanBinding: ActivityPodScanBinding

    private lateinit var viewModel: DeviceScanViewModel

    private lateinit var bluetoothDevice: BluetoothDevice

    private val connectionRequestObserver = Observer<BluetoothDevice> { device ->
        BluetoothServer.setCurrentChatConnection(device)
    }

    private val messageObserver= Observer<Message> {message->
        Toast.makeText(this,message.text,Toast.LENGTH_LONG).show()
    }

    private val viewStateObserver = Observer<DeviceScanViewState> { state ->
        when (state) {
            is DeviceScanViewState.ActiveScan -> showLoading()
            is DeviceScanViewState.ScanResults -> showResults(state.scanResults)
            is DeviceScanViewState.Error -> showError(state.message)
        }
    }

    private val deviceConnectionObserver = Observer<DeviceConnectionState> { state ->
        when (state) {
            is DeviceConnectionState.Connected -> {
                val device = state.device
                Toast.makeText(this, "Device Connected" + device, Toast.LENGTH_LONG).show()
            }
            is DeviceConnectionState.Disconnected -> {
                Toast.makeText(this, "Device Disconnected", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityPodScanBinding = ActivityPodScanBinding.inflate(layoutInflater)
        val view = activityPodScanBinding.root
        setContentView(view)


        activityPodScanBinding.buttonScan1.setOnClickListener({
            val intent = Intent(this, BarCodeScanActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            Toast.makeText(this, App.getStringPrefernce(Constants.BARCODE1, ""), Toast.LENGTH_LONG)
                .show()
            viewModel = ViewModelProvider(this).get(DeviceScanViewModel::class.java)
            viewModel.viewState.observe(this, viewStateObserver)

            BluetoothServer.startServer(application)
        }
    }

    private fun showLoading() {
        Toast.makeText(this, "Data is loading", Toast.LENGTH_LONG).show()
    }


    private fun showResults(scanResults: Map<String, BluetoothDevice>) {
        if (!scanResults.values.toList().isEmpty()) {

        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, "Error:" + message, Toast.LENGTH_LONG).show()
    }



}