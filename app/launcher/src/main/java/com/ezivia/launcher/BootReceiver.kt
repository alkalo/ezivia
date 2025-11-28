package com.ezivia.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ezivia.launcher.reminders.ReminderAlarmScheduler
import com.ezivia.utilities.reminders.ReminderRepository

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        ReminderAlarmScheduler(context).reschedule(ReminderRepository(context).getReminders())

        if (DefaultLauncherHelper.isDefaultLauncher(context)) {
            LauncherOnboardingPreferences(context).setDefaultLauncherCompleted(true)
            val launchIntent = Intent(context, HomeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(launchIntent)
        } else {
            LauncherOnboardingPreferences(context).setDefaultLauncherCompleted(false)
        }
    }
}
