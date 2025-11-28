package com.ezivia.launcher

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezivia.launcher.databinding.ActivityRemindersOverviewBinding
import com.ezivia.launcher.reminders.ReminderAlarmScheduler
import com.ezivia.utilities.reminders.Reminder
import com.ezivia.utilities.reminders.ReminderRepository

class RemindersOverviewActivity : BaseActivity() {

    private lateinit var binding: ActivityRemindersOverviewBinding
    private lateinit var reminderRepository: ReminderRepository
    private lateinit var remindersAdapter: RemindersAdapter
    private lateinit var reminderAlarmScheduler: ReminderAlarmScheduler
    private val addReminderLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                loadReminders()
            }
        }

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
            setOnClickListener { openAddReminderScreen() }
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

    private fun openAddReminderScreen() {
        addReminderLauncher.launch(Intent(this, AddReminderActivity::class.java))
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
