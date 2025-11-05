package com.ezivia.launcher

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ezivia.communication.telephony.NativeTelephonyController
import com.ezivia.launcher.R
import com.ezivia.launcher.databinding.ActivitySosBinding
import com.ezivia.utilities.caregiver.CaregiverPreferences

class SosActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySosBinding
    private lateinit var telephonyController: NativeTelephonyController
    private lateinit var caregiverPreferences: CaregiverPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.sosToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.quick_action_sos_title)

        telephonyController = NativeTelephonyController(this)
        caregiverPreferences = CaregiverPreferences(this)

        binding.sosCallButton.setOnClickListener { triggerEmergencyCall() }
        binding.sosMessageButton.setOnClickListener { notifyCaregiver() }
        binding.sosCloseButton.setOnClickListener { finish() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun triggerEmergencyCall() {
        val emergencyNumber = "112"
        val hasPermission = checkSelfPermission(Manifest.permission.CALL_PHONE) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val handled = if (hasPermission) {
            telephonyController.startCall(emergencyNumber)
        } else {
            telephonyController.startDial(emergencyNumber)
        }
        if (!handled) {
            Toast.makeText(this, R.string.sos_call_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun notifyCaregiver() {
        val caregiver = caregiverPreferences.loadCaregivers().firstOrNull()
        if (caregiver == null) {
            Toast.makeText(this, R.string.sos_no_caregiver, Toast.LENGTH_SHORT).show()
            return
        }
        if (telephonyController.startSms(caregiver.phoneNumber, getString(R.string.sos_message_body))) {
            Toast.makeText(this, R.string.sos_message_sent, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, R.string.telephony_not_available, Toast.LENGTH_SHORT).show()
        }
    }
}
