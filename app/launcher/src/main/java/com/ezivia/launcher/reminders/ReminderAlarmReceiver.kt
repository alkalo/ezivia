package com.ezivia.launcher.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ezivia.launcher.R
import com.ezivia.launcher.RemindersOverviewActivity
import com.ezivia.utilities.reminders.ReminderType
import java.util.Locale

class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_SHOW_REMINDER) return

        val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID)?.hashCode() ?: DEFAULT_NOTIFICATION_ID
        val title = intent.getStringExtra(EXTRA_REMINDER_TITLE)
            ?: context.getString(R.string.reminders_notification_title_fallback)
        val type = intent.getStringExtra(EXTRA_REMINDER_TYPE)?.let {
            runCatching { ReminderType.valueOf(it) }.getOrNull()
        }

        val notificationManager = NotificationManagerCompat.from(context)
        val channelId = ensureChannel(context, notificationManager)
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val alarmAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val contentIntent = PendingIntent.getActivity(
            context,
            reminderId,
            Intent(context, RemindersOverviewActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val fullScreenIntent = PendingIntent.getActivity(
            context,
            reminderId,
            Intent(context, RemindersOverviewActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val contentText = context.getString(
            R.string.reminders_notification_body,
            type?.toFriendlyText()?.replaceFirstChar { it.titlecase(Locale.getDefault()) }
                ?: context.getString(R.string.reminders_notification_title_fallback),
            title
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_action_reminders)
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setVibrate(VIBRATION_PATTERN)
            .setSound(alarmSound, alarmAttributes)
            .setContentIntent(contentIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setFullScreenIntent(fullScreenIntent, true)
            .build()

        notificationManager.notify(reminderId, notification)
    }

    private fun ensureChannel(
        context: Context,
        notificationManager: NotificationManagerCompat
    ): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.reminders_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.reminders_notification_channel_description)
                enableVibration(true)
                vibrationPattern = VIBRATION_PATTERN
                setSound(
                    alarmSound,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            notificationManager.createNotificationChannel(channel)
        }
        return CHANNEL_ID
    }

    companion object {
        const val ACTION_SHOW_REMINDER = "com.ezivia.launcher.reminders.ACTION_SHOW_REMINDER"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_REMINDER_TITLE = "extra_reminder_title"
        const val EXTRA_REMINDER_TYPE = "extra_reminder_type"
        private const val CHANNEL_ID = "reminders_notification_channel"
        private val VIBRATION_PATTERN = longArrayOf(0, 1500, 1000, 1500, 1000)
        private const val DEFAULT_NOTIFICATION_ID = 7001
    }
}
