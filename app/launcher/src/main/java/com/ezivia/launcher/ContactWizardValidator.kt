package com.ezivia.launcher

import android.telephony.PhoneNumberUtils
import java.util.Locale

object ContactWizardValidator {

    private val confirmationKeywords = listOf(
        "confirm",
        "confirmo",
        "guardar",
        "si",
        "sí",
        "ok",
        "listo"
    )

    fun isValidName(name: String): Boolean {
        return name.trim().length >= 3
    }

    fun isValidPhone(phone: String): Boolean {
        val digits = phone.filter { it.isDigit() }
        return digits.length >= 7
    }

    fun cleanedPhone(phone: String): String {
        val normalized = phone.filter { it.isDigit() || it == '+' || it == ' ' }
        return PhoneNumberUtils.formatNumber(normalized, Locale.getDefault().country) ?: normalized
    }

    fun matchesVoiceConfirmation(transcript: String): Boolean {
        val lowered = transcript.lowercase(Locale.getDefault())
        return confirmationKeywords.any { keyword -> lowered.contains(keyword) }
    }
}
