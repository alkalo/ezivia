package com.ezivia.logging

/**
 * Helper used during local development or unit tests.
 */
object NoOpCrashReporter : CrashReporter {
    override fun recordException(throwable: Throwable, customKeys: Map<String, String>) = Unit

    override fun log(message: String, attributes: Map<String, String>, level: LogLevel) = Unit
}
