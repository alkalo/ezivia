package com.ezivia.analytics

import java.security.MessageDigest
import java.util.Locale

/**
 * Utility responsible for anonymising sensitive strings before they are sent to
 * third-party analytics or crash reporting backends.
 */
class DataAnonymizer(private val salt: String = "") {

    /**
     * Hashes the provided [value] using SHA-256 while appending an optional [salt].
     * Returns null if the value is null or blank to avoid sending empty hashes.
     */
    fun anonymize(value: String?): String? {
        if (value.isNullOrBlank()) return null
        val digest = MessageDigest.getInstance("SHA-256")
        val hashed = digest.digest((value + salt).toByteArray())
        return hashed.joinToString(separator = "") { byte ->
            String.format(Locale.US, "%02x", byte)
        }
    }

    /**
     * Produces a new map where every entry that matches [keysToAnonymize] is hashed.
     */
    fun anonymizeMap(
        data: Map<String, Any?>,
        keysToAnonymize: Set<String>
    ): Map<String, Any?> = buildMap {
        data.forEach { (key, value) ->
            val sanitizedValue = if (key in keysToAnonymize) {
                anonymize(value as? String)
            } else {
                value
            }
            sanitizedValue?.let { put(key, it) }
        }
    }
}
