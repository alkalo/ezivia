package com.ezivia.launcher.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.ezivia.utilities.reminders.Reminder
import java.time.ZoneId

class ReminderAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val zoneId: ZoneId = ZoneId.systemDefault()

    fun schedule(reminder: Reminder) {
        if (reminder.isCompleted) {
            cancel(reminder.id)
            return
        }
        val triggerAtMillis = reminder.dateTime.atZone(zoneId).toInstant().toEpochMilli()
        if (triggerAtMillis <= System.currentTimeMillis()) {
            cancel(reminder.id)
            return
        }
        val pendingIntent = buildPendingIntent(reminder)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    fun cancel(reminderId: String) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            Intent(context, ReminderAlarmReceiver::class.java).apply {
                action = ReminderAlarmReceiver.ACTION_SHOW_REMINDER
            },
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun reschedule(reminders: List<Reminder>) {
        reminders.forEach { cancel(it.id) }
        reminders.filterNot { it.isCompleted }
            .forEach { schedule(it) }
    }

    private fun buildPendingIntent(reminder: Reminder): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ReminderAlarmReceiver.ACTION_SHOW_REMINDER
            putExtra(ReminderAlarmReceiver.EXTRA_REMINDER_ID, reminder.id)
            putExtra(ReminderAlarmReceiver.EXTRA_REMINDER_TITLE, reminder.title)
            putExtra(ReminderAlarmReceiver.EXTRA_REMINDER_TYPE, reminder.type.name)
        }
        return PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
