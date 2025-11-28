package com.ezivia.launcher

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezivia.launcher.databinding.ActivityRemindersOverviewBinding
import com.ezivia.launcher.databinding.DialogAddReminderBinding
import com.ezivia.launcher.reminders.ReminderAlarmScheduler
import com.ezivia.utilities.reminders.Reminder
import com.ezivia.utilities.reminders.ReminderRepository
import com.ezivia.utilities.reminders.ReminderType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class RemindersOverviewActivity : BaseActivity() {

    private lateinit var binding: ActivityRemindersOverviewBinding
    private lateinit var reminderRepository: ReminderRepository
    private lateinit var remindersAdapter: RemindersAdapter
    private lateinit var reminderAlarmScheduler: ReminderAlarmScheduler

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                showErrorFeedback(R.string.reminders_notification_permission_denied)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemindersOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.remindersToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.reminders_title)

        reminderRepository = ReminderRepository(this)
        reminderAlarmScheduler = ReminderAlarmScheduler(this)
        remindersAdapter = RemindersAdapter(::onReminderToggle)

        binding.remindersList.apply {
            adapter = remindersAdapter
            layoutManager = LinearLayoutManager(this@RemindersOverviewActivity)
            itemAnimator = ScaleInItemAnimator()
        }

        binding.addReminderFab.apply {
            applyPressScaleEffect()
            setOnClickListener { showAddReminderDialog() }
        }

        requestNotificationPermissionIfNeeded()
        loadReminders()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadReminders() {
        val reminders = reminderRepository.getReminders()
        reminderAlarmScheduler.reschedule(reminders)
        remindersAdapter.submitList(reminders)
        binding.remindersEmptyView.isVisible = reminders.isEmpty()
    }

    private fun onReminderToggle(reminder: Reminder, completed: Boolean) {
        reminderRepository.updateCompletion(reminder.id, completed)
        showSuccessFeedback(R.string.reminders_status_updated)
        if (completed) {
            reminderAlarmScheduler.cancel(reminder.id)
        } else {
            reminderAlarmScheduler.schedule(reminder.copy(isCompleted = false))
        }
        loadReminders()
    }

    private fun showAddReminderDialog() {
        val dialogBinding = DialogAddReminderBinding.inflate(LayoutInflater.from(this))
        dialogBinding.reminderDatePicker.minDate = System.currentTimeMillis()
        dialogBinding.reminderTimePicker.setIs24HourView(DateFormat.is24HourFormat(this))
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.reminders_dialog_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.reminders_dialog_save, null)
            .setNegativeButton(R.string.reminders_dialog_cancel, null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.applyPressScaleEffect()
            positiveButton.setOnClickListener {
                val name = dialogBinding.reminderTitleInput.editText?.text?.toString()?.trim().orEmpty()
                val notes = dialogBinding.reminderNotesInput.editText?.text?.toString()?.trim().orEmpty()
                if (name.isEmpty()) {
                    dialogBinding.reminderTitleInput.error = getString(R.string.reminders_dialog_error_name)
                    return@setOnClickListener
                } else {
                    dialogBinding.reminderTitleInput.error = null
                }

                val type = if (dialogBinding.reminderTypeMedication.isChecked) {
                    ReminderType.MEDICATION
                } else {
                    ReminderType.APPOINTMENT
                }

                val selectedDateTime = dialogBinding.toLocalDateTime()
                if (selectedDateTime.isBefore(LocalDateTime.now())) {
                    showErrorFeedback(R.string.reminders_dialog_error_time)
                    return@setOnClickListener
                }

                val reminder = reminderRepository.create(
                    title = name,
                    notes = notes.takeIf { it.isNotBlank() },
                    dateTime = selectedDateTime,
                    type = type
                )
                reminderAlarmScheduler.schedule(reminder)
                showSuccessFeedback(R.string.reminders_saved)
                dialog.dismiss()
                loadReminders()
            }
        }

        dialog.show()
    }

    private fun DialogAddReminderBinding.toLocalDateTime(): LocalDateTime {
        val date = reminderDatePicker.toLocalDate()
        val time = reminderTimePicker.toLocalTime()
        return LocalDateTime.of(date, time)
    }

    private fun DatePicker.toLocalDate(): LocalDate {
        return LocalDate.of(year, month + 1, dayOfMonth)
    }

    private fun TimePicker.toLocalTime(): LocalTime {
        val hourValue = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hour
        } else {
            @Suppress("DEPRECATION")
            currentHour
        }
        val minuteValue = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            minute
        } else {
            @Suppress("DEPRECATION")
            currentMinute
        }
        return LocalTime.of(hourValue, minuteValue)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
