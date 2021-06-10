package com.imufortka

import java.util.*

class Constants {

    companion object {
        const val HIP = "Hip"
        const val UNI = "Uni"
        const val Left = "Left"
        const val Tible = "Tible"
        const val BARCODE1="Barcode1"
        const val BARCODE2="Barcode2"

        const val SAMPLE_UUID="0000b81d-0000-1000-8000-00805f9b34fb"

        val SERVICE_UUID:UUID=UUID.fromString("0000b81d-0000-1000-8000-00805f9b34fb")

        val MESSAGE_UUID: UUID = UUID.fromString("7db3e235-3608-41f3-a03c-955fcbd2ea4b")

        /**
         * UUID to confirm device connection
         */
        val CONFIRM_UUID: UUID = UUID.fromString("36d4dc5c-814b-4097-a5a6-b93b39085928")

        const val REQUEST_ENABLE_BT = 1

    }

}