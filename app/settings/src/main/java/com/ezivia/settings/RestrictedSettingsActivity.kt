package com.ezivia.settings

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ezivia.settings.databinding.ActivityRestrictedSettingsBinding
import com.ezivia.settings.databinding.DialogChangePinBinding
import com.ezivia.utilities.caregiver.CaregiverPreferences
import com.ezivia.utilities.security.PinStorage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView

private const val SETTINGS_PREFS = "ezivia_settings_preferences"
private const val KEY_BIG_TEXT = "big_text"
private const val KEY_VOICE_ASSISTANCE = "voice_assistance"
private const val KEY_REMINDERS = "reminders"

/**
 * Hosts configuration knobs reserved for caregivers while keeping the main
 * Ezivia experience protegida contra cambios accidentales.
 */
class RestrictedSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRestrictedSettingsBinding
    private lateinit var caregiverPreferences: CaregiverPreferences
    private lateinit var pinStorage: PinStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestrictedSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        caregiverPreferences = CaregiverPreferences(this)
        pinStorage = PinStorage(this)

        populateCaregivers()
        configureSwitches()

        binding.changePinButton.setOnClickListener {
            showChangePinDialog()
        }
    }

    private fun populateCaregivers() {
        val caregivers = caregiverPreferences.loadCaregivers()
        binding.caregiverContainer.removeAllViews()

        if (caregivers.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = getString(R.string.settings_caregiver_empty)
                setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Body2)
            }
            binding.caregiverContainer.addView(emptyView)
            return
        }

        caregivers.forEach { caregiver ->
            val textView = MaterialTextView(this).apply {
                text = caregiver.asDisplayText()
                setPadding(0, 8, 0, 8)
                setTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Body2)
            }
            binding.caregiverContainer.addView(textView)
        }
    }

    private fun configureSwitches() {
        val preferences = getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE)

        binding.bigTextSwitch.isChecked = preferences.getBoolean(KEY_BIG_TEXT, true)
        binding.voiceAssistanceSwitch.isChecked = preferences.getBoolean(KEY_VOICE_ASSISTANCE, false)
        binding.remindersSwitch.isChecked = preferences.getBoolean(KEY_REMINDERS, true)

        binding.bigTextSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean(KEY_BIG_TEXT, isChecked).apply()
        }
        binding.voiceAssistanceSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean(KEY_VOICE_ASSISTANCE, isChecked).apply()
        }
        binding.remindersSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean(KEY_REMINDERS, isChecked).apply()
        }
    }

    private fun showChangePinDialog() {
        val dialogBinding = DialogChangePinBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_change_pin_title)
            .setView(dialogBinding.root)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.settings_save_pin, null)
            .create()

        dialog.setOnShowListener {
            val positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positive.setOnClickListener {
                val currentPin = dialogBinding.currentPinInput.editText?.text?.toString()?.trim().orEmpty()
                val newPin = dialogBinding.newPinInput.editText?.text?.toString()?.trim().orEmpty()
                val confirmPin = dialogBinding.confirmPinInput.editText?.text?.toString()?.trim().orEmpty()

                dialogBinding.currentPinInput.error = null
                dialogBinding.newPinInput.error = null
                dialogBinding.confirmPinInput.error = null

                if (pinStorage.isPinConfigured() && !pinStorage.verifyPin(currentPin)) {
                    dialogBinding.currentPinInput.error = getString(R.string.settings_pin_error_incorrect)
                    return@setOnClickListener
                }

                if (newPin.length < 4) {
                    dialogBinding.newPinInput.error = getString(R.string.settings_pin_error_length)
                    return@setOnClickListener
                }

                if (newPin != confirmPin) {
                    dialogBinding.confirmPinInput.error = getString(R.string.settings_pin_error_match)
                    return@setOnClickListener
                }

                pinStorage.setPin(newPin)
                Toast.makeText(this, R.string.settings_pin_updated, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}
