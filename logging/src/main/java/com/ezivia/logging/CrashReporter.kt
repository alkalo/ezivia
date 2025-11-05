package com.ezivia.logging

/**
 * Abstraction that allows plugging in any crash reporting backend.
 */
interface CrashReporter {
    fun recordException(throwable: Throwable, customKeys: Map<String, String> = emptyMap())
    fun log(message: String, attributes: Map<String, String> = emptyMap(), level: LogLevel = LogLevel.INFO)
}
