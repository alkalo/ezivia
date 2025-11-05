package com.ezivia.launcher

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class LauncherOnboardingPreferencesTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("launcher_onboarding_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun shouldShowPrompt_returnsTrueByDefault() {
        val preferences = LauncherOnboardingPreferences(context)

        assertThat(preferences.shouldShowDefaultLauncherPrompt()).isTrue()
    }

    @Test
    fun shouldShowPrompt_reflectsCompletionFlag() {
        val preferences = LauncherOnboardingPreferences(context)

        preferences.setDefaultLauncherCompleted(true)
        assertThat(preferences.shouldShowDefaultLauncherPrompt()).isFalse()

        preferences.setDefaultLauncherCompleted(false)
        assertThat(preferences.shouldShowDefaultLauncherPrompt()).isTrue()
    }

    @Test
    fun shouldShowLockGestureTutorial_defaultsToTrue() {
        val preferences = LauncherOnboardingPreferences(context)

        assertThat(preferences.shouldShowLockGestureTutorial()).isTrue()
    }

    @Test
    fun markLockGestureTutorialShown_updatesFlag() {
        val preferences = LauncherOnboardingPreferences(context)

        preferences.markLockGestureTutorialShown()

        assertThat(preferences.shouldShowLockGestureTutorial()).isFalse()
    }
}
