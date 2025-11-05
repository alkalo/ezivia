package com.ezivia.utilities.security

import android.content.Context
import java.security.MessageDigest

private const val PROTECTION_PREFS_NAME = "ezivia_protection_prefs"
private const val KEY_PIN_HASH = "pin_hash"

/**
 * Stores a hashed representation of the protection PIN that guards the Ezivia
 * launcher and restricted settings. Hashing avoids keeping the plain value on
 * disk while remaining lightweight for offline comparison.
 */
class PinStorage(context: Context) {

    private val preferences =
        context.applicationContext.getSharedPreferences(PROTECTION_PREFS_NAME, Context.MODE_PRIVATE)

    fun isPinConfigured(): Boolean = preferences.contains(KEY_PIN_HASH)

    fun setPin(pin: String) {
        preferences.edit().putString(KEY_PIN_HASH, hash(pin)).apply()
    }

    fun clearPin() {
        preferences.edit().remove(KEY_PIN_HASH).apply()
    }

    fun verifyPin(candidate: String): Boolean {
        val stored = preferences.getString(KEY_PIN_HASH, null) ?: return false
        return hash(candidate) == stored
    }

    private fun hash(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return bytes.joinToString(separator = "") { byte ->
            val unsigned = byte.toInt() and 0xFF
            unsigned.toString(16).padStart(2, '0')
        }
    }
}
