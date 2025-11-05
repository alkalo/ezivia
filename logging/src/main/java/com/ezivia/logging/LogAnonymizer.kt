package com.ezivia.logging

import java.security.MessageDigest
import java.util.Locale

/**
 * Utility mirroring [com.ezivia.analytics.DataAnonymizer] but scoped to the logging module to
 * keep dependencies decoupled.
 */
class LogAnonymizer(private val salt: String = "") {

    fun anonymize(value: String?): String? {
        if (value.isNullOrBlank()) return null
        val digest = MessageDigest.getInstance("SHA-256")
        val hashed = digest.digest((value + salt).toByteArray())
        return hashed.joinToString(separator = "") { byte ->
            String.format(Locale.US, "%02x", byte)
        }
    }

    fun anonymizeAttributes(
        attributes: Map<String, String>,
        keysToAnonymize: Set<String>
    ): Map<String, String> = buildMap {
        attributes.forEach { (key, value) ->
            val sanitized = if (key in keysToAnonymize) anonymize(value) else value
            sanitized?.let { put(key, it) }
        }
    }
}
