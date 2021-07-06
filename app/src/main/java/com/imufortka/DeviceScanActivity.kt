package com.imufortka

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.imufortka.databinding.ActivityDeviceScanBinding

class DeviceScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceScanBinding

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDeviceScanBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        viewModel = ViewModelProvider(this).get(DeviceScanViewModel::class.java)
        viewModel.viewState.observe(this, viewStateObserver)
        viewModel.startScan(true)
        BluetoothServer.startServer(application,true)

     //   val address = "Your device address is" + BluetoothServer.getYourDeviceAddress()

    //    binding.textDeviceAddress.text = address

        binding.textConnectDevice.setOnClickListener(View.OnClickListener {
            BluetoothServer.setCurrentChatConnection(bluetoothDevice)
        })

        binding.buttonSend.setOnClickListener(View.OnClickListener {
             BluetoothServer.sendMessagePod1("success")
        })
    }


    private fun showLoading() {
        Toast.makeText(this, "Data is loading", Toast.LENGTH_LONG).show()
    }


    private fun showResults(scanResults: Map<String, BluetoothDevice>) {
        if (!scanResults.values.toList().isEmpty()) {
            bluetoothDevice = scanResults.values.toList().get(0)
            binding.textConnectDevice.text = scanResults.values.toList().get(0).name
            Toast.makeText(
                this,
                "Device is:" + scanResults.values.toList().get(0),
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, "Error:" + message, Toast.LENGTH_LONG).show()
    }

    override fun onStart() {
        super.onStart()
        BluetoothServer.connectionRequest.observe(this, connectionRequestObserver)
        BluetoothServer.deviceConnection.observe(this, deviceConnectionObserver)
        BluetoothServer.messages.observe(this,messageObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        BluetoothServer.stopServer()
    }


}