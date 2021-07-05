package com.imufortka

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView

class BarCodeScanActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private lateinit var mScannerView: ZXingScannerView

    private var scanner1:Boolean=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bar_code_scan)
        val contentFrame = findViewById(R.id.content_frame) as ViewGroup
        mScannerView = ZXingScannerView(this)
        contentFrame.addView(mScannerView)

        scanner1=intent.getBooleanExtra(Constants.BAR_CODE_NUMBER,false)

    }

    override fun onResume() {
        super.onResume()
        mScannerView.setResultHandler(this)
        mScannerView.startCamera()

    }

    override fun onPause() {
        super.onPause()
        mScannerView.stopCamera()
    }

    override fun handleResult(rawResult: Result?) {
        if(scanner1){
            App.storeStringPrefernce(Constants.BARCODE1,rawResult?.text.toString())
        }else{
            App.storeStringPrefernce(Constants.BARCODE2,rawResult?.text.toString())
        }
        Toast.makeText(this, "Contents= " + rawResult?.text, Toast.LENGTH_LONG).show()
        val handler = Handler()
        handler.postDelayed(Runnable() {
            @Override
            fun run() {
                mScannerView.resumeCameraPreview(this@BarCodeScanActivity)
            }
        }, 2000);
        setResult(Activity.RESULT_OK)
        finish()
    }
}