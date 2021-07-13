package com.imufortka

import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

private const val TAG = "Bluetooth Server"

object BluetoothServer {

    private val _requestEnableBluetooth = MutableLiveData<Boolean>()

    val requestEnableBluetooth = _requestEnableBluetooth as LiveData<Boolean>

    private lateinit var bluetoothManager: BluetoothManager

    private var isScanner1: Boolean = false

    private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private var advertiser: BluetoothLeAdvertiser? = null

    private var advertiseCallback: AdvertiseCallback? = null

    /*private var advertiseSettings: AdvertiseSettings = buildAdvertiseSettings()

    private var advertiseData: AdvertiseData = buildAdvertiseData()*/

    private var currentDevice: BluetoothDevice? = null

    private val _deviceConnection = MutableLiveData<DeviceConnectionState>()

    val deviceConnection = _deviceConnection as LiveData<DeviceConnectionState>

    private var gattClientCallback1: BluetoothGattCallback? = null
    private var gattClientCallback2: BluetoothGattCallback? = null

    private var gattClient1: BluetoothGatt? = null
    private var gattClient2: BluetoothGatt? = null

    private var app: Application? = null

    private var gatt1: BluetoothGatt? = null
    private var gatt2: BluetoothGatt? = null

    private var gattServerCallback1: BluetoothGattServerCallback? = null
    private var gattServerCallback2: BluetoothGattServerCallback? = null

    private var gattServer1: BluetoothGattServer? = null
    private var gattServer2: BluetoothGattServer? = null

    private val _connectionRequest = MutableLiveData<BluetoothDevice>()

    val connectionRequest = _connectionRequest as LiveData<BluetoothDevice>


    // LiveData for reporting the messages sent to the device
    private val _messages = MutableLiveData<Message>()
    val messages = _messages as LiveData<Message>

    private var messageCharacteristic1: BluetoothGattCharacteristic? = null
    private var messageCharacteristic2: BluetoothGattCharacteristic? = null

    fun startServer(app: Application, isScanner: Boolean) {
        Log.d(TAG, "startServer method called")
        isScanner1 = isScanner
        bluetoothManager = app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        if(isScanner1){
            setUpGattServer1(app)
        }else{
            setUpGattServer2(app)
        }
        startAdvertisement()
    }

    fun stopServer() {
        Log.d(TAG, "stopServer method called")
        stopAdvertising()
        gatt1?.disconnect()
        gatt2?.disconnect()
    }

    /**
     * Stops BLE Advertising.
     */
    private fun stopAdvertising() {
        Log.d(TAG, "Stopping Advertising with advertiser $advertiser")
        if (advertiseCallback != null) {
            advertiser?.stopAdvertising(advertiseCallback)
            advertiseCallback = null
        }

    }

    fun getYourDeviceAddress(): String = bluetoothManager.adapter.address

    /**
     * Returns an AdvertiseSettings object set to use low power (to help preserve battery life)
     * and disable the built-in timeout since this code uses its own timeout runnable.
     */
    private fun buildAdvertiseSettings(): AdvertiseSettings {
        return AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setTimeout(0)
            .build()
    }

    /**
     * Returns an AdvertiseData object which includes the Service UUID and Device Name.
     */
    private fun buildAdvertiseData(): AdvertiseData {
        /**
         * Note: There is a strict limit of 31 Bytes on packets sent over BLE Advertisements.
         * This limit is outlined in section 2.3.1.1 of this document:
         * https://inst.eecs.berkeley.edu/~ee290c/sp18/note/BLE_Vol6.pdf
         *
         * This limit includes everything put into AdvertiseData including UUIDs, device info, &
         * arbitrary service or manufacturer data.
         * Attempting to send packets over this limit will result in a failure with error code
         * AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE. Catch this error in the
         * onStartFailure() method of an AdvertiseCallback implementation.
         */

        if (isScanner1) {
            val dataBuilder = AdvertiseData.Builder()
                .addServiceUuid(ParcelUuid(Constants.SERVICE_UUID1))
                .setIncludeDeviceName(false)
            return dataBuilder.build()

        } else {
            val dataBuilder = AdvertiseData.Builder()
                .addServiceUuid(ParcelUuid(Constants.SERVICE_UUID2))
                .setIncludeDeviceName(false)
            return dataBuilder.build()
        }

        /* For example - this will cause advertising to fail (exceeds size limit) */
        //String failureData = "asdghkajsghalkxcjhfa;sghtalksjcfhalskfjhasldkjfhdskf";
        //dataBuilder.addServiceData(Constants.Service_UUID, failureData.getBytes());
    }


    private fun startAdvertisement() {
        advertiser = adapter.bluetoothLeAdvertiser
        var advertiseSettings = buildAdvertiseSettings()
        var advertiseData = buildAdvertiseData()
        Log.d(TAG, "startAdvertisement: with advertiser $advertiser")

        if (advertiseCallback == null) {
            advertiseCallback = DeviceAdvertiseCallback()
            advertiser?.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
        }
    }


    private class DeviceAdvertiseCallback : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.i(TAG, errorCode.toString())
            Log.d(TAG, "Advertising failed")

        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d(TAG, "Advertising successfully started")

        }
    }


    fun setCurrentChatConnection(device: BluetoothDevice) {
        Log.d(TAG, "setCurrentChatConnection method called $device")
        currentDevice = device
        _deviceConnection.value = DeviceConnectionState.Connected(device)

        if(isScanner1){
            connectToChatDevice1(device)
        }else{
            connectToChatDevice2(device)
        }

    }

    private fun connectToChatDevice1(device: BluetoothDevice) {
        Log.d(TAG, "connectToChatDevice method called $device")
            gattClientCallback1 = GattClientCallback1()
            gattClient1 = device.connectGatt(app, false, gattClientCallback1)

    }

    private fun connectToChatDevice2(device: BluetoothDevice) {
        Log.d(TAG, "connectToChatDevice method called $device")
        gattClientCallback2 = GattClientCallback2()
        gattClient2 = device.connectGatt(app, false, gattClientCallback2)

    }


    private fun setUpGattServer1(app: Application) {
        Log.d(TAG, "setUpGattServer method called ")
        gattServerCallback1 = GattServerCallback1()
        gattServer1 = bluetoothManager.openGattServer(app, gattServerCallback1)
            .apply { addService(setUpGattService1()) }
    }

    private fun setUpGattServer2(app: Application) {
        Log.d(TAG, "setUpGattServer method called ")
        gattServerCallback2 = GattServerCallback2()
        gattServer2 = bluetoothManager.openGattServer(app, gattServerCallback2)
            .apply { addService(setUpGattService2()) }
    }

    private fun setUpGattService1(): BluetoothGattService {
        Log.d(TAG, "setUpGattService method called ")
         val service =
                BluetoothGattService(
                    Constants.SERVICE_UUID1,
                    BluetoothGattService.SERVICE_TYPE_PRIMARY
                )
            val messageCharacteristic = BluetoothGattCharacteristic(
                Constants.MESSAGE_UUID1,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
            )
            service.addCharacteristic(messageCharacteristic)

            /*  val confirmCharacteristic = BluetoothGattCharacteristic(
                  Constants.CONFIRM_UUID,
                  BluetoothGattCharacteristic.PROPERTY_WRITE,
                  BluetoothGattCharacteristic.PERMISSION_WRITE
              )
              service.addCharacteristic(confirmCharacteristic)*/

            return service

    }

    private fun setUpGattService2(): BluetoothGattService {
        Log.d(TAG, "setUpGattService method called ")

            val service =
                BluetoothGattService(
                    Constants.SERVICE_UUID2,
                    BluetoothGattService.SERVICE_TYPE_PRIMARY
                )
            val messageCharacteristic = BluetoothGattCharacteristic(
                Constants.MESSAGE_UUID2,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE
            )
            service.addCharacteristic(messageCharacteristic)

            /*  val confirmCharacteristic = BluetoothGattCharacteristic(
                  Constants.CONFIRM_UUID,
                  BluetoothGattCharacteristic.PROPERTY_WRITE,
                  BluetoothGattCharacteristic.PERMISSION_WRITE
              )
              service.addCharacteristic(confirmCharacteristic)*/

            return service

    }



    private class GattClientCallback1 : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothProfile.STATE_CONNECTED
            Log.d(TAG, "onGattConnect: Have gatt $isSuccess and $isConnected")

            if (isSuccess && isConnected) {
                gatt?.discoverServices()
            }
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                gatt?.close()
                gatt == null
            }
        }

        override fun onServicesDiscovered(discoveredGatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(discoveredGatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onServicesDiscovered: Have gatt $discoveredGatt")
                gatt1 = discoveredGatt
                val service1 = discoveredGatt?.getService(Constants.SERVICE_UUID1)
                messageCharacteristic1 = service1?.getCharacteristic(Constants.MESSAGE_UUID1)

            }
        }
    }

    private class GattClientCallback2 : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothProfile.STATE_CONNECTED
            Log.d(TAG, "onGattConnect: Have gatt $isSuccess and $isConnected")

            if (isSuccess && isConnected) {
                gatt?.discoverServices()
            }
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                gatt?.close()
                gatt == null
            }
        }

        override fun onServicesDiscovered(discoveredGatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(discoveredGatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onServicesDiscovered: Have gatt $discoveredGatt")
                gatt2 = discoveredGatt
                val service = discoveredGatt?.getService(Constants.SERVICE_UUID2)
                messageCharacteristic2 = service?.getCharacteristic(Constants.MESSAGE_UUID2)
            }
        }
    }


    private class GattServerCallback1 : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothGatt.STATE_CONNECTED
            Log.d(
                TAG,
                "onConnectionStateChange: Server $device ${device?.name} success: $isSuccess connected: $isConnected"
            )
            if (isSuccess && isConnected) {
                _connectionRequest.postValue(device)
                Log.d(TAG, "onConnectionStateChange: Server connected")
            } else {
                Log.d(TAG, "onConnectionStateChange: Server Disconnected")
                _deviceConnection.postValue(DeviceConnectionState.Disconnected)
            }

        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            if (characteristic?.uuid == Constants.MESSAGE_UUID1) {
                gattServer1?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                val message = value?.toString(Charsets.UTF_8)
                Log.d(TAG, "onCharacteristicWriteRequest: Have message: \"$message\"")
                message?.let { _messages.postValue(Message.RemoteMessage(it)) }
            } else if (characteristic?.uuid == Constants.MESSAGE_UUID2) {
                gattServer2?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                val message = value?.toString(Charsets.UTF_8)
                Log.d(TAG, "onCharacteristicWriteRequest: Have message: \"$message\"")
                message?.let {
                    _messages.postValue(Message.RemoteMessage(it))
                }
            }
        }

    }

    private class GattServerCallback2 : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothGatt.STATE_CONNECTED
            Log.d(
                TAG,
                "onConnectionStateChange: Server $device ${device?.name} success: $isSuccess connected: $isConnected"
            )
            if (isSuccess && isConnected) {
                _connectionRequest.postValue(device)
                Log.d(TAG, "onConnectionStateChange: Server connected")
            } else {
                Log.d(TAG, "onConnectionStateChange: Server Disconnected")
                _deviceConnection.postValue(DeviceConnectionState.Disconnected)
            }

        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            if (characteristic?.uuid == Constants.MESSAGE_UUID1) {
                gattServer1?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                val message = value?.toString(Charsets.UTF_8)
                Log.d(TAG, "onCharacteristicWriteRequest: Have message: \"$message\"")
                message?.let { _messages.postValue(Message.RemoteMessage(it)) }
            } else if (characteristic?.uuid == Constants.MESSAGE_UUID2) {
                gattServer2?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                val message = value?.toString(Charsets.UTF_8)
                Log.d(TAG, "onCharacteristicWriteRequest: Have message: \"$message\"")
                message?.let {
                    _messages.postValue(Message.RemoteMessage(it))
                }
            }
        }

    }


    fun sendMessagePod1(message: String): Boolean {
        messageCharacteristic1?.let { characterstic ->
            characterstic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

            val messageBytes = message.toByteArray(Charsets.UTF_8)
            characterstic.value = messageBytes
            gatt1?.let {
                val success = it.writeCharacteristic(messageCharacteristic1)
                Log.d(TAG, "onServicesDiscovered: message send: $success")

                if (success) {
                    _messages.value = Message.LocalMessage(message)
                }
            } ?: run {
                Log.d(TAG, "sendMessage: no gatt connection to send a message with")
            }
        }
        return false
    }

    fun sendMessagePod2(message: String): Boolean {
        messageCharacteristic2?.let { characterstic ->
            characterstic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

            val messageBytes = message.toByteArray(Charsets.UTF_8)
            characterstic.value = messageBytes
            gatt2?.let {
                val success = it.writeCharacteristic(messageCharacteristic2)
                Log.d(TAG, "onServicesDiscovered: message send: $success")

                if (success) {
                    _messages.value = Message.LocalMessage(message)
                }
            } ?: run {
                Log.d(TAG, "sendMessage: no gatt connection to send a message with")
            }
        }
        return false
    }

}