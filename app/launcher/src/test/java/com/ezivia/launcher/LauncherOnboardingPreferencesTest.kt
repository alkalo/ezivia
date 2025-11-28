package com.ezivia.launcher

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LauncherOnboardingPreferencesTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("launcher_onboarding_prefs", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test
    fun defaultLauncherPrompt_isTrueAndPersistsCompletionFlag() {
        val first = LauncherOnboardingPreferences(context)

        assertThat(first.shouldShowDefaultLauncherPrompt()).isTrue()

        first.setDefaultLauncherCompleted(true)
        assertThat(first.shouldShowDefaultLauncherPrompt()).isFalse()

        val second = LauncherOnboardingPreferences(context)
        assertThat(second.shouldShowDefaultLauncherPrompt()).isFalse()
    }
}
