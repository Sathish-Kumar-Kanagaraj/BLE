package com.imufortka

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.imufortka.databinding.ActivitySelectionBinding

class SelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectionBinding

    private lateinit var radioButton1: RadioButton
    private lateinit var radioButton2: RadioButton
    private lateinit var radioButton3: RadioButton
    private lateinit var radioButton4: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.buttonSubmit.setOnClickListener({
            submitButtonAction()
        })

    }


    private fun submitButtonAction() {
        val selectedButtonHip: Int = binding.radioGroupHip.checkedRadioButtonId
        radioButton1 = findViewById(selectedButtonHip)

        val selectedButtonUni: Int = binding.radioGroupUni.checkedRadioButtonId
        radioButton2 = findViewById(selectedButtonUni)

        val selectedButtonLeft: Int = binding.radioGroupLeft.checkedRadioButtonId
        radioButton3 = findViewById(selectedButtonLeft)

        val selectedButtonTible: Int = binding.radioGroupTible.checkedRadioButtonId
        radioButton4 = findViewById(selectedButtonTible)

        storeValues()

    }

    private fun storeValues() {
        if (radioButton1?.text.equals(Constants.HIP)) {
            App.storeIntPreference(Constants.HIP, 0)
        } else {
            App.storeIntPreference(Constants.HIP, 1)
        }

        if (radioButton2?.text.equals(Constants.UNI)) {
            App.storeIntPreference(Constants.UNI, 0)
        } else {
            App.storeIntPreference(Constants.UNI, 1)
        }

        if (radioButton3?.text.equals(Constants.RIGHT)) {
            App.storeIntPreference(Constants.RIGHT, 0)
        } else {
            App.storeIntPreference(Constants.RIGHT, 1)
        }

        if (radioButton4?.text.equals(Constants.FEMUR)) {
            App.storeIntPreference(Constants.FEMUR, 0)
        } else {
            App.storeIntPreference(Constants.FEMUR, 1)
        }

        nextActivity()
    }

    private fun nextActivity() {
        val intent = Intent(this, PodScanActivity::class.java)
        startActivity(intent)
    }

}