package com.ezivia.launcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.ezivia.launcher.databinding.ActivityDefaultLauncherSetupBinding

private const val REQUEST_CODE_DEFAULT_LAUNCHER = 1001

class DefaultLauncherSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDefaultLauncherSetupBinding
    private lateinit var onboardingPreferences: LauncherOnboardingPreferences

    private val roleRequestLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            handleLauncherStateChange()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onboardingPreferences = LauncherOnboardingPreferences(this)

        if (DefaultLauncherHelper.isDefaultLauncher(this)) {
            onboardingPreferences.setDefaultLauncherCompleted(true)
            openHomeAndFinish()
            return
        }

        binding = ActivityDefaultLauncherSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.configureButton.setOnClickListener {
            onboardingPreferences.setDefaultLauncherCompleted(false)
            DefaultLauncherHelper.requestDefaultLauncher(
                this,
                roleRequestLauncher,
                REQUEST_CODE_DEFAULT_LAUNCHER
            )
        }

        binding.skipButton.isVisible = false
    }

    override fun onResume() {
        super.onResume()
        handleLauncherStateChange()
    }

    private fun handleLauncherStateChange() {
        val isDefault = DefaultLauncherHelper.isDefaultLauncher(this)
        if (isDefault) {
            onboardingPreferences.setDefaultLauncherCompleted(true)
            openHomeAndFinish()
        } else {
            onboardingPreferences.setDefaultLauncherCompleted(false)
            if (::binding.isInitialized) {
                binding.statusMessage.isVisible = true
            }
        }
    }

    private fun openHomeAndFinish() {
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }
}
