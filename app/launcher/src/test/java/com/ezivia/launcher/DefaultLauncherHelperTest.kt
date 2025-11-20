package com.ezivia.launcher

import android.app.Activity
import android.app.Application
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class DefaultLauncherHelperTest {

    private val packageName = "com.ezivia.launcher"

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun isDefaultLauncher_preQ_returnsTrueWhenHomeResolvesToSelf() {
        val packageManager = mock(PackageManager::class.java)
        val context = mock(Context::class.java)

        val resolveInfo = resolveInfo(packageName)

        `when`(context.packageManager).thenReturn(packageManager)
        `when`(context.packageName).thenReturn(packageName)
        `when`(packageManager.resolveActivity(any(Intent::class.java), eq(PackageManager.MATCH_DEFAULT_ONLY))).thenReturn(resolveInfo)

        val result = DefaultLauncherHelper.isDefaultLauncher(context)

        assertThat(result).isTrue()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun isDefaultLauncher_preQ_returnsFalseWhenHomeResolvesElsewhere() {
        val packageManager = mock(PackageManager::class.java)
        val context = mock(Context::class.java)

        `when`(context.packageManager).thenReturn(packageManager)
        `when`(context.packageName).thenReturn(packageName)
        `when`(packageManager.resolveActivity(any(Intent::class.java), eq(PackageManager.MATCH_DEFAULT_ONLY))).thenReturn(resolveInfo("other"))

        val result = DefaultLauncherHelper.isDefaultLauncher(context)

        assertThat(result).isFalse()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun isDefaultLauncher_roleHeldWhenAvailable() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val roleManager = mock(RoleManager::class.java)
        shadowOf(application).setSystemService(RoleManager::class.java, roleManager)

        `when`(roleManager.isRoleAvailable(RoleManager.ROLE_HOME)).thenReturn(true)
        `when`(roleManager.isRoleHeld(RoleManager.ROLE_HOME)).thenReturn(true)

        val result = DefaultLauncherHelper.isDefaultLauncher(application)

        assertThat(result).isTrue()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun isDefaultLauncher_roleUnavailableReturnsFalse() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val roleManager = mock(RoleManager::class.java)
        shadowOf(application).setSystemService(RoleManager::class.java, roleManager)

        `when`(roleManager.isRoleAvailable(RoleManager.ROLE_HOME)).thenReturn(false)

        val result = DefaultLauncherHelper.isDefaultLauncher(application)

        assertThat(result).isFalse()
        verify(roleManager, never()).isRoleHeld(RoleManager.ROLE_HOME)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun requestDefaultLauncher_launchesRoleIntentWhenAvailable() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val application = activity.application
        val roleManager = mock(RoleManager::class.java)
        val launcher = mock(ActivityResultLauncher::class.java) as ActivityResultLauncher<Intent>
        val roleIntent = Intent("requestRole")

        shadowOf(application).setSystemService(RoleManager::class.java, roleManager)
        `when`(roleManager.isRoleAvailable(RoleManager.ROLE_HOME)).thenReturn(true)
        `when`(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)).thenReturn(roleIntent)

        DefaultLauncherHelper.requestDefaultLauncher(activity, launcher, 100)

        verify(launcher).launch(roleIntent)
        assertThat(shadowOf(activity).nextStartedActivity).isNull()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun requestDefaultLauncher_prefersHomeSettingsWhenResolvable() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val pmShadow = shadowOf(activity.packageManager)
        pmShadow.addResolveInfoForIntent(Intent(Settings.ACTION_HOME_SETTINGS), resolveInfo("android", "SettingsActivity"))

        DefaultLauncherHelper.requestDefaultLauncher(activity, null, 100)

        val started = shadowOf(activity).nextStartedActivity
        assertThat(started.action).isEqualTo(Settings.ACTION_HOME_SETTINGS)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun requestDefaultLauncher_fallsBackToManageDefaultsWhenHomeUnavailable() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val pmShadow = shadowOf(activity.packageManager)
        pmShadow.addResolveInfoForIntent(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS), resolveInfo("android", "DefaultApps"))

        DefaultLauncherHelper.requestDefaultLauncher(activity, null, 100)

        val started = shadowOf(activity).nextStartedActivity
        assertThat(started.action).isEqualTo(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.M])
    fun requestDefaultLauncher_fallsBackToHomeCategoryWhenNothingResolves() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()

        DefaultLauncherHelper.requestDefaultLauncher(activity, null, 100)

        val started = shadowOf(activity).nextStartedActivity
        assertThat(started.action).isEqualTo(Intent.ACTION_MAIN)
        assertThat(started.categories).containsExactly(Intent.CATEGORY_HOME, Intent.CATEGORY_DEFAULT)
    }

    private fun resolveInfo(packageName: String, name: String = "LauncherActivity"): ResolveInfo {
        return ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                this.packageName = packageName
                this.name = name
            }
        }
    }
}
