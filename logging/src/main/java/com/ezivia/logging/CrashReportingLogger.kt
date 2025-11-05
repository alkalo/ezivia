package com.ezivia.logging

import android.util.Log

/**
 * Centralises how we log application failures and forwards them to the configured
 * [CrashReporter] implementation.
 */
class CrashReportingLogger(
    private val crashReporter: CrashReporter,
    private val anonymizer: LogAnonymizer = LogAnonymizer(),
    private val tag: String = "EziviaLogger",
    private val sensitiveKeys: Set<String> = setOf("phone_number", "contact_name")
) {

    fun log(
        message: String,
        level: LogLevel = LogLevel.INFO,
        attributes: Map<String, String> = emptyMap(),
        sanitize: Boolean = true
    ) {
        val safeAttributes = if (sanitize) {
            anonymizer.anonymizeAttributes(attributes, sensitiveKeys)
        } else {
            attributes
        }

        safeAttributes.takeIf { it.isNotEmpty() }?.forEach { (key, value) ->
            crashReporter.log(
                message = "attribute_$key=$value",
                attributes = emptyMap(),
                level = level
            )
        }

        logToAndroid(level, message)
        crashReporter.log(message, safeAttributes, level)
    }

    fun logAndReport(throwable: Throwable, message: String? = null, attributes: Map<String, String> = emptyMap()) {
        val safeAttributes = anonymizer.anonymizeAttributes(attributes, sensitiveKeys)
        message?.let { logToAndroid(LogLevel.ERROR, it) }
        crashReporter.recordException(throwable, safeAttributes)
    }

    private fun logToAndroid(level: LogLevel, message: String) {
        when (level) {
            LogLevel.DEBUG -> Log.d(tag, message)
            LogLevel.INFO -> Log.i(tag, message)
            LogLevel.WARNING -> Log.w(tag, message)
            LogLevel.ERROR -> Log.e(tag, message)
        }
    }
}
