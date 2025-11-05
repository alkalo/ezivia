package com.ezivia.launcher

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher

/**
 * Utilities around requesting and checking the HOME launcher role across
 * different Android API levels.
 */
object DefaultLauncherHelper {

    private const val ROLE_HOME = RoleManager.ROLE_HOME

    fun isDefaultLauncher(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            roleManager?.isRoleAvailable(ROLE_HOME) == true && roleManager.isRoleHeld(ROLE_HOME)
        } else {
            val packageManager = context.packageManager
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            val resolveInfo = packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
            resolveInfo?.activityInfo?.packageName == context.packageName
        }
    }

    fun requestDefaultLauncher(
        activity: Activity,
        launcher: ActivityResultLauncher<Intent>?,
        requestCode: Int
    ) {
        val roleIntent = createRoleRequestIntent(activity)
        if (roleIntent != null) {
            if (launcher != null) {
                launcher.launch(roleIntent)
            } else {
                @Suppress("DEPRECATION")
                activity.startActivityForResult(roleIntent, requestCode)
            }
            return
        }

        val settingsIntent = createSettingsIntent(activity)
        activity.startActivity(settingsIntent)
    }

    private fun createRoleRequestIntent(context: Context): Intent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null

        val roleManager = context.getSystemService(RoleManager::class.java) ?: return null
        if (!roleManager.isRoleAvailable(ROLE_HOME)) {
            return null
        }

        return roleManager.createRequestRoleIntent(ROLE_HOME)
    }

    fun createSettingsIntent(context: Context): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val homeSettings = Intent(Settings.ACTION_HOME_SETTINGS)
            if (homeSettings.resolveActivity(context.packageManager) != null) {
                return homeSettings
            }
        }

        val manageDefaults = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        if (manageDefaults.resolveActivity(context.packageManager) != null) {
            return manageDefaults
        }

        return Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
