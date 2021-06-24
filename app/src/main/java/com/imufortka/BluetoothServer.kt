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

    private var gattClientCallback: BluetoothGattCallback? = null

    private var gattClient: BluetoothGatt? = null

    private var app: Application? = null

    private var gatt: BluetoothGatt? = null

    private var gattServerCallback: BluetoothGattServerCallback? = null

    private var gattServer: BluetoothGattServer? = null

    private val _connectionRequest = MutableLiveData<BluetoothDevice>()

    val connectionRequest = _connectionRequest as LiveData<BluetoothDevice>


    // LiveData for reporting the messages sent to the device
    private val _messages = MutableLiveData<Message>()
    val messages = _messages as LiveData<Message>

    private var messageCharacteristic: BluetoothGattCharacteristic? = null

    fun startServer(app: Application, isScanner: Boolean) {
        Log.d(TAG, "startServer method called")
        isScanner1 = isScanner
        bluetoothManager = app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        setUpGattServer(app)
        startAdvertisement()
    }

    fun stopServer() {
        Log.d(TAG, "stopServer method called")
        stopAdvertising()
    }

    /**
     * Stops BLE Advertising.
     */
    private fun stopAdvertising() {
        Log.d(TAG, "Stopping Advertising with advertiser $advertiser")
        advertiser?.stopAdvertising(advertiseCallback)
        advertiseCallback = null
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
                .setIncludeDeviceName(true)
            return dataBuilder.build()

        } else {
            val dataBuilder = AdvertiseData.Builder()
                .addServiceUuid(ParcelUuid(Constants.SERVICE_UUID2))
                .setIncludeDeviceName(true)
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
        connectToChatDevice(device)
    }

    private fun connectToChatDevice(device: BluetoothDevice) {
        Log.d(TAG, "connectToChatDevice method called $device")
        gattClientCallback = GattClientCallback()
        gattClient = device.connectGatt(app, false, gattClientCallback)
    }


    private fun setUpGattServer(app: Application) {
        Log.d(TAG, "setUpGattServer method called ")
        gattServerCallback = GattServerCallback()
        gattServer = bluetoothManager.openGattServer(app, gattServerCallback)
            .apply { addService(setUpGattService()) }
    }

    private fun setUpGattService(): BluetoothGattService {
        Log.d(TAG, "setUpGattService method called ")

        if (isScanner1) {
            val service =
                BluetoothGattService(
                    Constants.SERVICE_UUID1,
                    BluetoothGattService.SERVICE_TYPE_PRIMARY
                )
            val messageCharacteristic = BluetoothGattCharacteristic(
                Constants.MESSAGE_UUID,
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
        } else {
            val service =
                BluetoothGattService(
                    Constants.SERVICE_UUID2,
                    BluetoothGattService.SERVICE_TYPE_PRIMARY
                )
            val messageCharacteristic = BluetoothGattCharacteristic(
                Constants.MESSAGE_UUID,
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

    }

    private class GattClientCallback : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothProfile.STATE_CONNECTED
            Log.d(TAG, "onGattConnect: Have gatt $isSuccess and $isConnected")

            if (isSuccess && isConnected) {
                gatt?.discoverServices()
            }
        }

        override fun onServicesDiscovered(discoveredGatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(discoveredGatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onServicesDiscovered: Have gatt $discoveredGatt")
                gatt = discoveredGatt
                if (isScanner1) {
                    val service = discoveredGatt?.getService(Constants.SERVICE_UUID1)
                    messageCharacteristic = service?.getCharacteristic(Constants.MESSAGE_UUID)
                } else {
                    val service = discoveredGatt?.getService(Constants.SERVICE_UUID2)
                    messageCharacteristic = service?.getCharacteristic(Constants.MESSAGE_UUID)
                }
            }
        }
    }


    private class GattServerCallback : BluetoothGattServerCallback() {
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
            if (characteristic?.uuid == Constants.MESSAGE_UUID) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                val message = value?.toString(Charsets.UTF_8)
                Log.d(TAG, "onCharacteristicWriteRequest: Have message: \"$message\"")
                message?.let { _messages.postValue(Message.RemoteMessage(it)) }
            }
        }

    }


    fun sendMessage(message: String): Boolean {
        messageCharacteristic?.let { characterstic ->
            characterstic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

            val messageBytes = message.toByteArray(Charsets.UTF_8)
            characterstic.value = messageBytes
            gatt?.let {
                val success = it.writeCharacteristic(messageCharacteristic)
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

    /* fun sendCharMessage(message: Char): Boolean {
         messageCharacteristic?.let { characterstic ->
             characterstic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
             val vOut = byteArrayOf(message as Byte)

          //   val messageBytes = message.toByteArray(Charsets.UTF_8)
             characterstic.value = vOut
             gatt?.let {
                 val success = it.writeCharacteristic(messageCharacteristic)
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

 */
}