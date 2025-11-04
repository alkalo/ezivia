package com.ezivia.launcher

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ezivia.communication.ConversationCoordinator
import com.ezivia.launcher.databinding.ActivityMainBinding
import com.ezivia.onboarding.OnboardingNavigator
import com.ezivia.utilities.DeviceProfileProvider

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val deviceProfile = DeviceProfileProvider().createProfile(this)
        binding.welcomeMessage.text = getString(R.string.launcher_welcome_message, deviceProfile.ownerName)

        binding.startButton.setOnClickListener {
            OnboardingNavigator(this).launchOnboarding()
        }

        viewModel.initialize(ConversationCoordinator())
    }
}
