package com.ezivia.launcher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ezivia.launcher.databinding.ActivityHomeBinding

/**
 * Home screen for Ezivia that exposes the key actions older adults need in a
 * simplified layout.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        setupListeners()
    }

    private fun setupListeners() {
        binding.callsButton.setOnClickListener {
            // Placeholder for future calls experience.
        }

        binding.messagesButton.setOnClickListener {
            // Placeholder for future messaging experience.
        }

        binding.settingsButton.setOnClickListener {
            // Placeholder for future settings experience.
        }
    }
}
