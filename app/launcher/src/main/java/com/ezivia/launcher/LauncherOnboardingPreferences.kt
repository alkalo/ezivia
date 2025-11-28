package com.ezivia.launcher

import android.content.Context
import android.content.SharedPreferences

private const val PREFS_NAME = "launcher_onboarding_prefs"
private const val KEY_DEFAULT_LAUNCHER_COMPLETED = "default_launcher_completed"

class LauncherOnboardingPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setDefaultLauncherCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_DEFAULT_LAUNCHER_COMPLETED, completed).apply()
    }

    fun shouldShowDefaultLauncherPrompt(): Boolean {
        return !prefs.getBoolean(KEY_DEFAULT_LAUNCHER_COMPLETED, false)
    }
}
