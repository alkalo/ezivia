package com.ezivia.logging

import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Wrapper around Firebase Crashlytics so that the rest of the code-base depends on the
 * lightweight [CrashReporter] abstraction.
 */
class FirebaseCrashlyticsReporter(
    private val crashlytics: FirebaseCrashlytics
) : CrashReporter {

    override fun recordException(throwable: Throwable, customKeys: Map<String, String>) {
        customKeys.forEach { (key, value) ->
            crashlytics.setCustomKey(key, value)
        }
        crashlytics.recordException(throwable)
    }

    override fun log(message: String, attributes: Map<String, String>, level: LogLevel) {
        crashlytics.log("${level.name}: $message")
        attributes.forEach { (key, value) ->
            crashlytics.setCustomKey(key, value)
        }
    }
}
