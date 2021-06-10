package com.imufortka

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import com.imufortka.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val bluetoothEnableObserver = Observer<Boolean> { showPrompt ->
        if (!showPrompt) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        BluetoothServer.requestEnableBluetooth.observe(this, bluetoothEnableObserver)

        binding.buttonNext.setOnClickListener({

            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT)


            val intent = Intent(this, PodScanActivity::class.java)
            startActivity(intent)
            /*   val intent = Intent(this, SelectionActivity::class.java)
               startActivity(intent)*/
        })

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            Constants.REQUEST_ENABLE_BT -> {
                if (resultCode == Activity.RESULT_OK) {
                    BluetoothServer.startServer(application)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
      //  BluetoothServer.stopServer()
    }
}