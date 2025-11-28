package com.ezivia.launcher

import android.Manifest
import android.os.Bundle
import com.ezivia.communication.telephony.NativeTelephonyController
import com.ezivia.launcher.R
import com.ezivia.launcher.databinding.ActivitySosBinding
import com.ezivia.utilities.caregiver.CaregiverPreferences

class SosActivity : BaseActivity() {

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

        binding.sosCallButton.apply {
            applyPressScaleEffect()
            setOnClickListener { triggerEmergencyCall() }
        }
        binding.sosMessageButton.apply {
            applyPressScaleEffect()
            setOnClickListener { notifyCaregiver() }
        }
        binding.sosCloseButton.apply {
            applyPressScaleEffect()
            setOnClickListener { finish() }
        }
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
            showErrorFeedback(R.string.sos_call_failed)
        }
    }

    private fun notifyCaregiver() {
        val caregiver = caregiverPreferences.loadCaregivers().firstOrNull()
        if (caregiver == null) {
            showErrorFeedback(R.string.sos_no_caregiver)
            return
        }
        if (telephonyController.startSms(caregiver.phoneNumber, getString(R.string.sos_message_body))) {
            showSuccessFeedback(R.string.sos_message_sent)
        } else {
            showErrorFeedback(R.string.telephony_not_available)
        }
    }
}
