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

    private val messageObserver = Observer<Message> { message ->
        Toast.makeText(this, message.text, Toast.LENGTH_LONG).show()
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
                activityPodScanBinding.progressbar.visibility = View.GONE
                val device = state.device
                Toast.makeText(this, "Device Connected" + device, Toast.LENGTH_LONG).show()
            }
            is DeviceConnectionState.Disconnected -> {
                activityPodScanBinding.progressbar.visibility = View.GONE
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

        viewModel = ViewModelProvider(this).get(DeviceScanViewModel::class.java)

        activityPodScanBinding.buttonScan1.setOnClickListener({
            App.storeIntPreference(Constants.BAR_CODE_NUMBER, 0)
            val intent = Intent(this, BarCodeScanActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
          /*  activityPodScanBinding.buttonScan1.setText("Pod1 Connected")
            activityPodScanBinding.buttonScan1.isEnabled=false*/
            activityPodScanBinding.progressbar.visibility = View.VISIBLE
            Toast.makeText(this, App.getStringPrefernce(Constants.BARCODE1, ""), Toast.LENGTH_LONG)
                .show()
            viewModel.viewState.observe(this, viewStateObserver)
            viewModel.startScan()
            BluetoothServer.startServer(application)
        }
    }

    private fun showLoading() {
        activityPodScanBinding.progressbar.visibility = View.VISIBLE
    }


    private fun showResults(scanResults: Map<String, BluetoothDevice>) {
        if (!scanResults.values.toList().isEmpty()) {
            //  activityPodScanBinding.progressbar.visibility = View.GONE
            activityPodScanBinding.progressbar.visibility = View.VISIBLE
            bluetoothDevice = scanResults.values.toList().get(0)
            Toast.makeText(
                this,
                "Connected with " + scanResults.values.toList().get(0).name,
                Toast.LENGTH_LONG
            ).show()
            BluetoothServer.setCurrentChatConnection(bluetoothDevice)
            var userChoices = App.getIntPreference(
                Constants.BAR_CODE_NUMBER,
                0).toString() + "0" + "0" + App.getIntPreference(Constants.HIP, 0).toString() +
                    App.getIntPreference(Constants.UNI, 0).toString() + App.getIntPreference(
                Constants.RIGHT, 0) +
                    App.getIntPreference(Constants.FEMUR, 0).toString()
            BluetoothServer.sendMessage(userChoices)
        }
    }

    private fun showError(message: String) {
        activityPodScanBinding.progressbar.visibility = View.GONE
        Toast.makeText(this, "Error:" + message, Toast.LENGTH_LONG).show()
    }


    override fun onStart() {
        super.onStart()
        BluetoothServer.connectionRequest.observe(this, connectionRequestObserver)
        BluetoothServer.deviceConnection.observe(this, deviceConnectionObserver)
        BluetoothServer.messages.observe(this, messageObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        BluetoothServer.stopServer()
    }

}