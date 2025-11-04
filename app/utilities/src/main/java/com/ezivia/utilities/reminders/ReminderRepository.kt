package com.ezivia.utilities.reminders

import android.content.Context
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlin.math.min
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Stores medication and appointment reminders using SharedPreferences.
 * The repository keeps data simple, persistent and easy to announce in a UI
 * tailored for seniors.
 */
class ReminderRepository(context: Context) {

    private val storage = ReminderStorage(context.applicationContext)

    /** Returns all reminders ordered by date and time. */
    fun getReminders(): List<Reminder> = storage.load().sortedBy { it.dateTime }

    /** Saves or updates a reminder. */
    fun save(reminder: Reminder): Reminder {
        val reminders = storage.load().toMutableList()
        val index = reminders.indexOfFirst { it.id == reminder.id }
        if (index >= 0) {
            reminders[index] = reminder
        } else {
            reminders.add(reminder)
        }
        storage.store(reminders)
        return reminder
    }

    /** Creates and saves a reminder with a new identifier. */
    fun create(
        title: String,
        notes: String?,
        dateTime: LocalDateTime,
        type: ReminderType
    ): Reminder {
        val reminder = Reminder(
            id = UUID.randomUUID().toString(),
            title = title,
            notes = notes,
            dateTime = dateTime,
            type = type
        )
        return save(reminder)
    }

    /** Marks a reminder as completed or pending again. */
    fun updateCompletion(reminderId: String, completed: Boolean) {
        val reminders = storage.load().toMutableList()
        val index = reminders.indexOfFirst { it.id == reminderId }
        if (index >= 0) {
            reminders[index] = reminders[index].copy(isCompleted = completed)
            storage.store(reminders)
        }
    }

    /** Deletes a reminder permanently. */
    fun remove(reminderId: String) {
        val reminders = storage.load().filterNot { it.id == reminderId }
        storage.store(reminders)
    }

    /** Clears all reminders. */
    fun clear() {
        storage.store(emptyList())
    }

    /**
     * Returns the reminders that will trigger soon, ideal for home screen cards.
     */
    fun getUpcomingReminders(
        now: LocalDateTime = LocalDateTime.now(),
        withinHours: Long = DEFAULT_UPCOMING_WINDOW_HOURS
    ): List<Reminder> {
        val upperBound = now.plusHours(withinHours)
        return storage.load()
            .filter { it.dateTime.isAfter(now.minusMinutes(1)) && it.dateTime.isBefore(upperBound) }
            .sortedBy { it.dateTime }
    }

    /**
     * Generates a warm summary that can be spoken or displayed for seniors.
     */
    fun buildFriendlySummary(now: LocalDateTime = LocalDateTime.now()): String {
        val reminders = getUpcomingReminders(now)
        if (reminders.isEmpty()) {
            return "No hay recordatorios pendientes por ahora."
        }
        val formatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'a las' HH:mm", Locale.getDefault())
        val limit = min(reminders.size, SUMMARY_LIMIT)
        val builder = StringBuilder("Próximos recordatorios: ")
        for (index in 0 until limit) {
            val reminder = reminders[index]
            builder.append(reminder.type.toFriendlyText())
                .append(' ')
                .append(reminder.title)
                .append(" el ")
                .append(reminder.dateTime.format(formatter))
            if (index < limit - 1) {
                builder.append(". ")
            }
        }
        if (reminders.size > limit) {
            builder.append(". Y hay más recordatorios guardados.")
        }
        return builder.toString()
    }

    companion object {
        private const val SUMMARY_LIMIT = 2
        private const val DEFAULT_UPCOMING_WINDOW_HOURS = 24L
    }
}

/** Types supported by the simplified reminder system. */
enum class ReminderType {
    MEDICATION,
    APPOINTMENT;

    fun toFriendlyText(): String = when (this) {
        MEDICATION -> "medicación"
        APPOINTMENT -> "cita"
    }
}

data class Reminder(
    val id: String,
    val title: String,
    val notes: String?,
    val dateTime: LocalDateTime,
    val type: ReminderType,
    val isCompleted: Boolean = false
)

private class ReminderStorage(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun load(): List<Reminder> {
        val raw = preferences.getString(KEY_REMINDERS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            MutableList(array.length()) { index ->
                array.getJSONObject(index).toReminder()
            }
        } catch (_: JSONException) {
            emptyList()
        }
    }

    fun store(reminders: List<Reminder>) {
        val array = JSONArray()
        reminders.forEach { reminder ->
            array.put(reminder.toJson())
        }
        preferences.edit().putString(KEY_REMINDERS, array.toString()).apply()
    }

    private fun Reminder.toJson(): JSONObject = JSONObject().apply {
        put(KEY_ID, id)
        put(KEY_TITLE, title)
        put(KEY_NOTES, notes)
        put(KEY_DATE_TIME, dateTime.format(formatter))
        put(KEY_TYPE, type.name)
        put(KEY_COMPLETED, isCompleted)
    }

    private fun JSONObject.toReminder(): Reminder {
        return Reminder(
            id = optString(KEY_ID),
            title = optString(KEY_TITLE),
            notes = if (has(KEY_NOTES) && !isNull(KEY_NOTES)) optString(KEY_NOTES) else null,
            dateTime = runCatching { LocalDateTime.parse(optString(KEY_DATE_TIME), formatter) }
                .getOrElse { LocalDateTime.now() },
            type = runCatching { ReminderType.valueOf(optString(KEY_TYPE)) }
                .getOrElse { ReminderType.MEDICATION },
            isCompleted = optBoolean(KEY_COMPLETED, false)
        )
    }

    companion object {
        private const val PREFS_NAME = "ezivia_reminders"
        private const val KEY_REMINDERS = "reminders"
        private const val KEY_ID = "id"
        private const val KEY_TITLE = "title"
        private const val KEY_NOTES = "notes"
        private const val KEY_DATE_TIME = "dateTime"
        private const val KEY_TYPE = "type"
        private const val KEY_COMPLETED = "completed"
    }
}
