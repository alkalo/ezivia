package com.ezivia.communication

import java.util.ArrayDeque

/**
 * Registro temporal de eventos de comunicación para mostrar en la app.
 */
object DiagnosticsLog {
    private const val MAX_LOGS = 100
    private val logs = ArrayDeque<LogEntry>(MAX_LOGS)

    @Synchronized
    fun record(source: String, message: String) {
        if (logs.size >= MAX_LOGS) {
            logs.removeFirst()
        }
        logs.addLast(LogEntry(System.currentTimeMillis(), source, message))
    }

    @Synchronized
    fun snapshot(): List<LogEntry> = logs.toList()

    @Synchronized
    fun clear() {
        logs.clear()
    }

    data class LogEntry(
        val timestampMillis: Long,
        val source: String,
        val message: String
    )
}
