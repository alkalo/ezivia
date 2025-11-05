package com.ezivia.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.os.Build
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class DefaultLauncherHelperTest {

    @Test
    @Config(sdk = [Build.VERSION_CODES.O])
    fun createSettingsIntent_prefersHomeSettingsWhenAvailable() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pmShadow = Shadows.shadowOf(context.packageManager)

        val intent = Intent(Settings.ACTION_HOME_SETTINGS)
        pmShadow.addResolveInfoForIntent(intent, resolveInfo("android", "SettingsActivity"))

        val result = DefaultLauncherHelper.createSettingsIntent(context)

        assertThat(result.action).isEqualTo(Settings.ACTION_HOME_SETTINGS)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun createSettingsIntent_fallsBackToManageDefaultsWhenHomeSettingsUnavailable() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pmShadow = Shadows.shadowOf(context.packageManager)

        val manageIntent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        pmShadow.addResolveInfoForIntent(manageIntent, resolveInfo("android", "DefaultApps"))

        val result = DefaultLauncherHelper.createSettingsIntent(context)

        assertThat(result.action).isEqualTo(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun createSettingsIntent_returnsChooserWhenNoSettingsAvailable() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val result = DefaultLauncherHelper.createSettingsIntent(context)

        assertThat(result.action).isEqualTo(Intent.ACTION_MAIN)
        assertThat(result.categories).containsExactly(
            Intent.CATEGORY_HOME,
            Intent.CATEGORY_DEFAULT
        )
    }

    private fun resolveInfo(packageName: String, name: String): ResolveInfo {
        return ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                this.packageName = packageName
                this.name = name
            }
        }
    }
}
