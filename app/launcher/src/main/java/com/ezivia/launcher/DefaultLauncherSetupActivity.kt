package com.ezivia.launcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.ezivia.launcher.databinding.ActivityDefaultLauncherSetupBinding

private const val REQUEST_CODE_DEFAULT_LAUNCHER = 1001

class DefaultLauncherSetupActivity : BaseActivity() {

    companion object {
        const val EXTRA_FORCE_RECONFIGURE = "com.ezivia.launcher.extra.FORCE_RECONFIGURE"
    }

    private lateinit var binding: ActivityDefaultLauncherSetupBinding
    private lateinit var onboardingPreferences: LauncherOnboardingPreferences
    private var forceReconfigure: Boolean = false

    private val roleRequestLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            handleLauncherStateChange()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onboardingPreferences = LauncherOnboardingPreferences(this)
        forceReconfigure = intent.getBooleanExtra(EXTRA_FORCE_RECONFIGURE, false)

        val alreadyDefault = DefaultLauncherHelper.isDefaultLauncher(this)
        if (!forceReconfigure && alreadyDefault) {
            onboardingPreferences.setDefaultLauncherCompleted(true)
            openHomeAndFinish()
            return
        }

        binding = ActivityDefaultLauncherSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (forceReconfigure) {
            binding.description.setText(R.string.default_launcher_reconfigure_description)
            binding.configureButton.setText(R.string.default_launcher_reconfigure_action)
            binding.statusMessage.apply {
                isVisible = true
                setText(R.string.default_launcher_force_hint)
            }
        }

        binding.configureButton.setOnClickListener {
            onboardingPreferences.setDefaultLauncherCompleted(false)
            val alreadyDefault = DefaultLauncherHelper.isDefaultLauncher(this)
            if (forceReconfigure && alreadyDefault) {
                val settingsIntent = DefaultLauncherHelper.createSettingsIntent(this)
                startActivity(settingsIntent)
            } else {
                DefaultLauncherHelper.requestDefaultLauncher(
                    this,
                    roleRequestLauncher,
                    REQUEST_CODE_DEFAULT_LAUNCHER
                )
            }
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
            if (forceReconfigure) {
                onboardingPreferences.setDefaultLauncherCompleted(false)
                if (::binding.isInitialized) {
                    binding.statusMessage.isVisible = true
                    binding.statusMessage.setText(R.string.default_launcher_force_hint)
                }
            } else {
                onboardingPreferences.setDefaultLauncherCompleted(true)
                openHomeAndFinish()
            }
        } else {
            onboardingPreferences.setDefaultLauncherCompleted(false)
            if (forceReconfigure) {
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(homeIntent)
                finishAffinity()
            } else if (::binding.isInitialized) {
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
