package com.ezivia.launcher.reminders

import android.app.AlarmManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.ezivia.utilities.reminders.Reminder
import com.ezivia.utilities.reminders.ReminderType
import com.google.common.truth.Truth.assertThat
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlarmManager

@RunWith(RobolectricTestRunner::class)
class ReminderAlarmSchedulerTest {

    private lateinit var context: Context
    private lateinit var scheduler: ReminderAlarmScheduler
    private lateinit var shadowAlarmManager: ShadowAlarmManager
    private var reminderCounter = 0

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        scheduler = ReminderAlarmScheduler(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        shadowAlarmManager = Shadows.shadowOf(alarmManager)
        shadowAlarmManager.cancelAll()
    }

    @Test
    fun scheduleFutureReminder_setsExactAlarm() {
        val reminder = createReminder(LocalDateTime.now().plusHours(2))

        scheduler.schedule(reminder)

        val scheduledAlarm = shadowAlarmManager.nextScheduledAlarm
        assertThat(scheduledAlarm).isNotNull()
        val triggerAtMillis = reminder.dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertThat(scheduledAlarm?.triggerAtTime).isAtLeast(triggerAtMillis)
    }

    @Test
    fun cancelReminder_removesScheduledAlarm() {
        val reminder = createReminder(LocalDateTime.now().plusHours(1))
        scheduler.schedule(reminder)

        scheduler.cancel(reminder.id)

        assertThat(shadowAlarmManager.scheduledAlarms).isEmpty()
    }

    @Test
    fun pastReminder_doesNotScheduleAlarm() {
        val reminder = createReminder(LocalDateTime.now().minusMinutes(30))

        scheduler.schedule(reminder)

        assertThat(shadowAlarmManager.scheduledAlarms).isEmpty()
    }

    private fun createReminder(dateTime: LocalDateTime): Reminder {
        return Reminder(
            id = "reminder-${reminderCounter++}",
            title = "Tomar medicación",
            notes = null,
            dateTime = dateTime,
            type = ReminderType.MEDICATION
        )
    }
}
