package com.ezivia.launcher

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ezivia.launcher.R
import com.ezivia.launcher.databinding.ItemReminderBinding
import com.ezivia.utilities.reminders.Reminder
import java.time.format.DateTimeFormatter
import java.util.Locale

class RemindersAdapter(
    private val onToggleCompletion: (Reminder, Boolean) -> Unit,
) : ListAdapter<Reminder, RemindersAdapter.ReminderViewHolder>(DiffCallback) {

    private val timeFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM • HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemReminderBinding.inflate(inflater, parent, false)
        return ReminderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReminderViewHolder(
        private val binding: ItemReminderBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(reminder: Reminder) {
            binding.reminderTitle.text = reminder.title
            binding.reminderSubtitle.text = timeFormatter.format(reminder.dateTime)
            binding.reminderStatus.text = binding.root.context.getString(
                if (reminder.isCompleted) R.string.reminders_completed else R.string.reminders_pending
            )
            binding.reminderAction.text = binding.root.context.getString(
                if (reminder.isCompleted) R.string.reminders_mark_pending else R.string.reminders_mark_done
            )
            binding.reminderAction.setOnClickListener {
                onToggleCompletion(reminder, !reminder.isCompleted)
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
            return oldItem == newItem
        }
    }
}
