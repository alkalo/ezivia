package com.ezivia.communication.telephony

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat

/**
 * Coordinates launching the native telephony experiences provided by Android
 * such as phone calls and SMS.
 */
class NativeTelephonyController(private val context: Context) {

    /** Attempts to start a phone call. Returns true when an app handled it. */
    fun startCall(phoneNumber: String): Boolean {
        val sanitizedNumber = phoneNumber.trim()
        if (sanitizedNumber.isEmpty()) return false

        val callIntent = Intent(Intent.ACTION_CALL, buildPhoneUri(sanitizedNumber))
        if (hasCallPermission() && launch(callIntent)) {
            return true
        }

        return startDial(sanitizedNumber)
    }

    /** Opens the dialer pre-filled with the [phoneNumber]. */
    fun startDial(phoneNumber: String): Boolean {
        val sanitizedNumber = phoneNumber.trim()
        if (sanitizedNumber.isEmpty()) return false

        val dialIntent = Intent(Intent.ACTION_DIAL, buildPhoneUri(sanitizedNumber))
        return launch(dialIntent)
    }

    /** Launches the SMS composer for the [phoneNumber] and optional [initialBody]. */
    fun startSms(phoneNumber: String, initialBody: String? = null): Boolean {
        val sanitizedNumber = phoneNumber.trim()
        if (sanitizedNumber.isEmpty()) return false

        val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + Uri.encode(sanitizedNumber))).apply {
            if (!initialBody.isNullOrBlank()) {
                putExtra("sms_body", initialBody)
            }
        }
        return launch(smsIntent)
    }

    private fun hasCallPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
    }

    private fun launch(intent: Intent): Boolean {
        val resolved = intent.resolveActivity(context.packageManager) ?: return false
        intent.setPackage(resolved.packageName)
        return try {
            context.startActivity(intent)
            true
        } catch (error: ActivityNotFoundException) {
            false
        }
    }

    private fun buildPhoneUri(phoneNumber: String): Uri {
        return Uri.parse("tel:" + Uri.encode(phoneNumber))
    }
}
