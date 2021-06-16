package com.imufortka

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import com.imufortka.databinding.ActivityMainBinding
import java.util.*
import java.util.jar.Manifest

const val PERMISSION_REQUEST_CAMERA = 0

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //     BluetoothServer.requestEnableBluetooth.observe(this, bluetoothEnableObserver)


        enableBluetooth()
        showCamera()
        binding.buttonNext.setOnClickListener({

        /*    val intent = Intent(this, DeviceScanActivity::class.java)
            startActivity(intent)*/
               val intent = Intent(this, SelectionActivity::class.java)
               startActivity(intent)
        })

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        /*   when (requestCode) {
               Constants.REQUEST_ENABLE_BT -> {
                   if (resultCode == Activity.RESULT_OK) {
            //           BluetoothServer.startServer(application)
                   }
               }
           }*/
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.size == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
           //     Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Required Permissions Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCamera() {
        if (checkSelfPermissionCompat(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            && checkSelfPermissionCompat(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        )  {
            requestCameraPermission()
        }
    }


    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationaleCompat(android.Manifest.permission.CAMERA)) {
            requestPermissionsCompat(
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),
                PERMISSION_REQUEST_CAMERA
            )
        } else {
            requestPermissionsCompat(
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),
                PERMISSION_REQUEST_CAMERA
            )
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        //  BluetoothServer.stopServer()
    }

    fun enableBluetooth() {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (!mBluetoothAdapter.isEnabled) {
            mBluetoothAdapter.enable()
        }
    }
}