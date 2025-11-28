package com.ezivia.launcher

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezivia.launcher.R
import com.ezivia.launcher.databinding.ActivityRemindersOverviewBinding
import com.ezivia.launcher.databinding.DialogAddReminderBinding
import com.ezivia.utilities.reminders.Reminder
import com.ezivia.utilities.reminders.ReminderRepository
import com.ezivia.utilities.reminders.ReminderType
import java.time.LocalDate
import java.time.LocalDateTime

class RemindersOverviewActivity : BaseActivity() {

    private lateinit var binding: ActivityRemindersOverviewBinding
    private lateinit var reminderRepository: ReminderRepository
    private lateinit var remindersAdapter: RemindersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemindersOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.remindersToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.reminders_title)

        reminderRepository = ReminderRepository(this)
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

        loadReminders()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadReminders() {
        val reminders = reminderRepository.getReminders()
        remindersAdapter.submitList(reminders)
        binding.remindersEmptyView.isVisible = reminders.isEmpty()
    }

    private fun onReminderToggle(reminder: Reminder, completed: Boolean) {
        reminderRepository.updateCompletion(reminder.id, completed)
        showSuccessFeedback(R.string.reminders_status_updated)
        loadReminders()
    }

    private fun showAddReminderDialog() {
        val dialogBinding = DialogAddReminderBinding.inflate(LayoutInflater.from(this))
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

                val time = dialogBinding.reminderTimePicker.toLocalDateTime()
                reminderRepository.create(
                    title = name,
                    notes = notes.takeIf { it.isNotBlank() },
                    dateTime = time,
                    type = type
                )
                showSuccessFeedback(R.string.reminders_saved)
                dialog.dismiss()
                loadReminders()
            }
        }

        dialog.show()
    }

    private fun TimePicker.toLocalDateTime(): LocalDateTime {
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
        return LocalDateTime.of(LocalDate.now(), java.time.LocalTime.of(hourValue, minuteValue))
    }
}
