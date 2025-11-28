package com.ezivia.launcher

import android.app.Activity
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.ImageView
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.ezivia.launcher.databinding.ActivityAddReminderBinding
import com.ezivia.launcher.reminders.ReminderAlarmScheduler
import com.ezivia.utilities.reminders.ReminderRepository
import com.ezivia.utilities.reminders.ReminderType
import com.google.android.material.card.MaterialCardView
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class AddReminderActivity : BaseActivity() {

    private lateinit var binding: ActivityAddReminderBinding
    private lateinit var reminderRepository: ReminderRepository
    private lateinit var reminderAlarmScheduler: ReminderAlarmScheduler

    private var currentStep: Step = Step.TYPE
    private var selectedType: ReminderType = ReminderType.MEDICATION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.addReminderToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.reminders_dialog_title)

        reminderRepository = ReminderRepository(this)
        reminderAlarmScheduler = ReminderAlarmScheduler(this)

        setupPickers()
        setupActions()
        selectType(ReminderType.MEDICATION)
        showTypeStep()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupPickers() {
        binding.reminderDatePicker.minDate = System.currentTimeMillis()
        binding.reminderTimePicker.setIs24HourView(DateFormat.is24HourFormat(this))
    }

    private fun setupActions() {
        binding.typeMedicationCard.apply {
            applyPressScaleEffect()
            setOnClickListener { selectType(ReminderType.MEDICATION) }
        }

        binding.typeAppointmentCard.apply {
            applyPressScaleEffect()
            setOnClickListener { selectType(ReminderType.APPOINTMENT) }
        }

        binding.primaryActionButton.apply {
            applyPressScaleEffect()
            setOnClickListener {
                when (currentStep) {
                    Step.TYPE -> showDetailsStep()
                    Step.DETAILS -> saveReminder()
                }
            }
        }

        binding.secondaryActionButton.apply {
            applyPressScaleEffect()
            setOnClickListener { showTypeStep() }
        }

        binding.toggleNotesButton.apply {
            applyPressScaleEffect()
            setOnClickListener { toggleNotesVisibility() }
        }
    }

    private fun selectType(type: ReminderType) {
        selectedType = type
        updateTypeCard(
            binding.typeMedicationCard,
            binding.typeMedicationIcon,
            binding.typeMedicationTitle,
            binding.typeMedicationDescription,
            type == ReminderType.MEDICATION
        )
        updateTypeCard(
            binding.typeAppointmentCard,
            binding.typeAppointmentIcon,
            binding.typeAppointmentTitle,
            binding.typeAppointmentDescription,
            type == ReminderType.APPOINTMENT
        )
    }

    private fun updateTypeCard(
        card: MaterialCardView,
        iconView: ImageView,
        titleView: TextView,
        descriptionView: TextView,
        selected: Boolean,
    ) {
        val backgroundColor = ContextCompat.getColor(
            this,
            if (selected) R.color.ezivia_primary else R.color.ezivia_surface
        )
        val contentColor = ContextCompat.getColor(
            this,
            if (selected) R.color.ezivia_on_primary else R.color.ezivia_on_surface
        )
        val outlineColor = ContextCompat.getColor(
            this,
            if (selected) R.color.ezivia_primary else R.color.ezivia_outline
        )
        card.setCardBackgroundColor(backgroundColor)
        card.strokeColor = outlineColor
        iconView.imageTintList = ContextCompat.getColorStateList(this, if (selected) R.color.ezivia_on_primary else R.color.ezivia_primary)
        titleView.setTextColor(contentColor)
        descriptionView.setTextColor(contentColor)
    }

    private fun showTypeStep() {
        currentStep = Step.TYPE
        binding.typeStepGroup.isVisible = true
        binding.detailsStepGroup.isVisible = false
        binding.secondaryActionButton.isVisible = false
        binding.primaryActionButton.setText(R.string.reminders_step_continue)
        binding.stepIndicator.text = getString(R.string.reminders_step_indicator, 1, 2)
        binding.stepTitle.setText(R.string.reminders_step_type_title)
        binding.stepDescription.setText(R.string.reminders_step_type_description)
    }

    private fun showDetailsStep() {
        currentStep = Step.DETAILS
        binding.typeStepGroup.isVisible = false
        binding.detailsStepGroup.isVisible = true
        binding.secondaryActionButton.isVisible = true
        binding.primaryActionButton.setText(R.string.reminders_dialog_save)
        binding.stepIndicator.text = getString(R.string.reminders_step_indicator, 2, 2)
        binding.stepTitle.setText(R.string.reminders_step_schedule_title)
        binding.stepDescription.setText(R.string.reminders_step_schedule_description)
        binding.selectedTypeLabel.text = getString(
            when (selectedType) {
                ReminderType.MEDICATION -> R.string.reminders_selected_type_medication
                ReminderType.APPOINTMENT -> R.string.reminders_selected_type_appointment
            }
        )
    }

    private fun toggleNotesVisibility() {
        val showNotes = !binding.reminderNotesInput.isVisible
        binding.reminderNotesInput.isVisible = showNotes
        binding.toggleNotesButton.setText(
            if (showNotes) R.string.reminders_notes_optional_hide else R.string.reminders_notes_optional_toggle
        )
        if (showNotes) {
            binding.reminderNotesInput.editText?.requestFocus()
        }
    }

    private fun saveReminder() {
        val name = binding.reminderTitleInput.editText?.text?.toString()?.trim().orEmpty()
        val notes = binding.reminderNotesInput.editText?.text?.toString()?.trim().orEmpty()
        if (name.isEmpty()) {
            binding.reminderTitleInput.error = getString(R.string.reminders_dialog_error_name)
            return
        } else {
            binding.reminderTitleInput.error = null
        }

        val selectedDateTime = binding.reminderDatePicker.toLocalDateTime(binding.reminderTimePicker)
        if (selectedDateTime.isBefore(LocalDateTime.now())) {
            showErrorFeedback(R.string.reminders_dialog_error_time)
            return
        }

        val reminder = reminderRepository.create(
            title = name,
            notes = notes.takeIf { it.isNotBlank() },
            dateTime = selectedDateTime,
            type = selectedType,
        )
        reminderAlarmScheduler.schedule(reminder)
        showSuccessFeedback(R.string.reminders_saved)
        setResult(Activity.RESULT_OK)
        finish()
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

    private fun DatePicker.toLocalDateTime(timePicker: TimePicker): LocalDateTime {
        val date = toLocalDate()
        val time = timePicker.toLocalTime()
        return LocalDateTime.of(date, time)
    }

    private enum class Step { TYPE, DETAILS }
}
