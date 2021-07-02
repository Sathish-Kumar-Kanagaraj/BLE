package com.imufortka

import java.util.*

class Constants {

    companion object {
        const val HIP = "Hip"
        const val UNI = "Unilateral"
        const val RIGHT = "Right"
        const val FEMUR = "Femur First"
        const val BAR_CODE_NUMBER="BarCode_Number"
        const val BARCODE1="Barcode1"
        const val BARCODE2="Barcode2"

        const val SAMPLE_UUID="0000b81d-0000-1000-8000-00805f9b34fb"

     //  val SERVICE_UUID:UUID=UUID.fromString("00001523-1212-efde-1523-785feabcd123")
        val SERVICE_UUID:UUID=UUID.fromString(App.getStringPrefernce(BARCODE1,"0000b81d-0000-1000-8000-00805f9b34fb"))

        val MESSAGE_UUID: UUID = UUID.fromString("00001525-1212-efde-1523-785feabcd123")

        /**
         * UUID to confirm device connection
         */
        val CONFIRM_UUID: UUID = UUID.fromString("36d4dc5c-814b-4097-a5a6-b93b39085928")

        const val REQUEST_ENABLE_BT = 1

    }

}