package com.ezivia.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ezivia.settings.databinding.ActivityRestrictedSettingsBinding
import com.ezivia.utilities.caregiver.CaregiverPreferences
import com.google.android.material.textview.MaterialTextView

private const val SETTINGS_PREFS = "ezivia_settings_preferences"
private const val KEY_FONT_SIZE = "font_size"
private const val KEY_TONE_VOLUME = "tone_volume"
private const val KEY_EMERGENCY_CONTACTS = "emergency_contacts"
private const val KEY_LARGE_LOCK_SCREEN = "large_lock_screen"

/**
 * Hosts configuration knobs reserved for caregivers while keeping the main
 * Ezivia experience protegida contra cambios accidentales.
 */
class RestrictedSettingsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_REQUEST_EXIT = "com.ezivia.settings.extra.REQUEST_EXIT"
    }

    private lateinit var binding: ActivityRestrictedSettingsBinding
    private lateinit var caregiverPreferences: CaregiverPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestrictedSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        caregiverPreferences = CaregiverPreferences(this)

        populateCaregivers()
        configureControls()

        binding.exitModeButton.setOnClickListener {
            exitEziviaMode()
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

    private fun configureControls() {
        val preferences = getSharedPreferences(SETTINGS_PREFS, MODE_PRIVATE)

        val defaultFontSize = 24f
        val defaultToneVolume = 70f

        binding.fontSizeSlider.value = preferences.getFloat(KEY_FONT_SIZE, defaultFontSize)
        updateFontSizeValue(binding.fontSizeSlider.value)
        binding.fontSizeSlider.addOnChangeListener { _, value, _ ->
            updateFontSizeValue(value)
            preferences.edit().putFloat(KEY_FONT_SIZE, value).apply()
        }

        binding.toneVolumeSlider.value = preferences.getFloat(KEY_TONE_VOLUME, defaultToneVolume)
        updateToneVolumeValue(binding.toneVolumeSlider.value)
        binding.toneVolumeSlider.addOnChangeListener { _, value, _ ->
            updateToneVolumeValue(value)
            preferences.edit().putFloat(KEY_TONE_VOLUME, value).apply()
        }

        binding.emergencyContactsSwitch.isChecked = preferences.getBoolean(KEY_EMERGENCY_CONTACTS, true)
        binding.emergencyContactsSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean(KEY_EMERGENCY_CONTACTS, isChecked).apply()
        }

        binding.largeLockScreenSwitch.isChecked = preferences.getBoolean(KEY_LARGE_LOCK_SCREEN, true)
        binding.largeLockScreenSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.edit().putBoolean(KEY_LARGE_LOCK_SCREEN, isChecked).apply()
        }
    }

    private fun updateFontSizeValue(value: Float) {
        binding.fontSizeValue.text = getString(R.string.settings_option_font_size_value, value.toInt())
    }

    private fun updateToneVolumeValue(value: Float) {
        binding.toneVolumeValue.text = getString(R.string.settings_option_tone_volume_value, value.toInt())
    }

    private fun exitEziviaMode() {
        val resultData = Intent().putExtra(EXTRA_REQUEST_EXIT, true)
        setResult(Activity.RESULT_OK, resultData)
        finish()
    }
}
