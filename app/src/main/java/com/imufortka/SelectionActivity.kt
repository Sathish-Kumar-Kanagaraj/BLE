package com.imufortka

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
            App.storeIntPreference(Constants.HIP, 1)
        } else {
            App.storeIntPreference(Constants.HIP, 0)
        }

        if (radioButton2?.text.equals(Constants.UNI)) {
            App.storeIntPreference(Constants.UNI, 1)
        } else {
            App.storeIntPreference(Constants.UNI, 0)
        }

        if (radioButton3?.text.equals(Constants.Left)) {
            App.storeIntPreference(Constants.Left, 1)
        } else {
            App.storeIntPreference(Constants.Left, 0)
        }

        if (radioButton4?.text.equals(Constants.Tible)) {
            App.storeIntPreference(Constants.Tible, 1)
        } else {
            App.storeIntPreference(Constants.Tible, 0)
        }
    }

}