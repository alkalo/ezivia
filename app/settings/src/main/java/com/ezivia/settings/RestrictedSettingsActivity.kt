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
private const val KEY_BIG_TEXT = "big_text"
private const val KEY_VOICE_ASSISTANCE = "voice_assistance"
private const val KEY_REMINDERS = "reminders"

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
        configureSwitches()

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

    private fun exitEziviaMode() {
        val resultData = Intent().putExtra(EXTRA_REQUEST_EXIT, true)
        setResult(Activity.RESULT_OK, resultData)
        finish()
    }
}
