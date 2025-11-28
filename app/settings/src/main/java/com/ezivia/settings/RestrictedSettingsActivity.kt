package com.ezivia.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ezivia.settings.databinding.ActivityRestrictedSettingsBinding

/**
 * Pantalla protegida para salir del modo Ezivia sin mostrar más controles.
 */
class RestrictedSettingsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_REQUEST_EXIT = "com.ezivia.settings.extra.REQUEST_EXIT"
    }

    private lateinit var binding: ActivityRestrictedSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestrictedSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.exitModeButton.setOnClickListener {
            exitEziviaMode()
        }
    }

    private fun exitEziviaMode() {
        val resultData = Intent().putExtra(EXTRA_REQUEST_EXIT, true)
        setResult(Activity.RESULT_OK, resultData)
        finish()
    }
}
