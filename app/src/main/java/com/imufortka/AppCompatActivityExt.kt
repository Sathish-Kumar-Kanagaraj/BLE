package com.imufortka

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

fun AppCompatActivity.checkSelfPermissionCompat(permission:String)=
    ActivityCompat.checkSelfPermission(this,permission)

fun AppCompatActivity.shouldShowRequestPermissionRationaleCompat(permission: String)=
    ActivityCompat.shouldShowRequestPermissionRationale(this,permission)

fun AppCompatActivity.requestPermissionsCompat(permissionArray:Array<String>,
                                               requestCode:Int){
    ActivityCompat.requestPermissions(this,permissionArray,requestCode)
}