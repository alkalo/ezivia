package com.ezivia.communication.whatsapp

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.widget.Toast
import com.ezivia.communication.DiagnosticsLog
import com.ezivia.communication.contacts.FavoriteContact
import java.util.Locale

/**
 * Encapsula la lógica necesaria para iniciar videollamadas de WhatsApp
 * pensadas para contactos favoritos dentro de Ezivia.
 */
class WhatsAppLauncher(private val activity: Activity) {

    /**
     * Inicia directamente una videollamada de WhatsApp con el [contact]. Devuelve
     * `true` si se pudo lanzar la acción o mostrar la tienda, y `false` cuando no
     * hay número válido o no se detecta WhatsApp instalado.
     */
    fun startFavoriteVideoCall(contact: FavoriteContact): Boolean {
        val sanitizedNumber = sanitizePhoneNumber(contact.phoneNumber)
        if (sanitizedNumber.isEmpty() || sanitizedNumber == "+") {
            DiagnosticsLog.record(
                source = "WhatsAppLauncher",
                message = "Número vacío o inválido para la videollamada"
            )
            showToast("El contacto no tiene un número válido.")
            return false
        }

        DiagnosticsLog.record(
            source = "WhatsAppLauncher",
            message = "Número sanitizado para videollamada: $sanitizedNumber"
        )

        val installedPackage = resolveInstalledWhatsAppPackage()
        if (installedPackage == null) {
            DiagnosticsLog.record(
                source = "WhatsAppLauncher",
                message = "No se encontró WhatsApp instalado"
            )
            showInstallFallback()
            return false
        }

        launchVideoCall(contact, sanitizedNumber, installedPackage)
        return true
    }

    private fun launchVideoCall(contact: FavoriteContact, phoneNumber: String, packageName: String) {
        val dataId = findWhatsAppVideoCallDataId(contact.id)
        val intent = if (dataId != null) {
            DiagnosticsLog.record(
                source = "WhatsAppLauncher",
                message = "Usando dato de agenda para videollamada WhatsApp: dataId=$dataId"
            )
            buildContactVideoCallIntent(dataId, packageName)
        } else {
            DiagnosticsLog.record(
                source = "WhatsAppLauncher",
                message = "No se encontró dataId de videollamada, usando URI directo"
            )
            buildVideoCallIntent(phoneNumber, packageName, resolveRegionIso())
        }
        DiagnosticsLog.record(
            source = "WhatsAppLauncher",
            message = "Lanzando videollamada con URI ${intent.data} y paquete $packageName"
        )
        try {
            activity.startActivity(intent)
        } catch (error: ActivityNotFoundException) {
            DiagnosticsLog.record(
                source = "WhatsAppLauncher",
                message = "WhatsApp no respondió al intento de abrir la videollamada"
            )
            showToast("No se pudo abrir WhatsApp para la videollamada.")
            showInstallFallback()
        }
    }

    private fun findWhatsAppVideoCallDataId(contactId: Long): Long? {
        val projection = arrayOf(ContactsContract.Data._ID)
        val selection = "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?"
        val selectionArgs = arrayOf(contactId.toString(), WHATSAPP_VIDEO_CALL_MIME_TYPE)

        return activity.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(ContactsContract.Data._ID)
            if (cursor.moveToFirst()) {
                cursor.getLong(idIndex)
            } else {
                null
            }
        }
    }

    private fun resolveInstalledWhatsAppPackage(): String? {
        val installedPackages = WHATSAPP_PACKAGES.filter { isWhatsAppInstalled(it) }.toSet()
        val selected = selectPreferredPackage(installedPackages)
        DiagnosticsLog.record(
            source = "WhatsAppLauncher",
            message = "Paquetes detectados: $installedPackages; seleccionado: ${selected ?: "ninguno"}"
        )
        return selected
    }

    private fun isWhatsAppInstalled(packageName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                activity.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                activity.packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun showInstallFallback() {
        AlertDialog.Builder(activity)
            .setTitle("Instalar WhatsApp")
            .setMessage("Para realizar la videollamada necesitas instalar o activar WhatsApp.")
            .setPositiveButton("Abrir Play Store") { _, _ ->
                DiagnosticsLog.record(
                    source = "WhatsAppLauncher",
                    message = "Abriendo ficha de WhatsApp en Play Store"
                )
                openStoreListing()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun openStoreListing() {
        val primaryIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$PRIMARY_WHATSAPP_PACKAGE")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (!tryStart(primaryIntent)) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$PRIMARY_WHATSAPP_PACKAGE")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (!tryStart(webIntent)) {
                showToast("No se pudo abrir la tienda de aplicaciones.")
            }
        }
    }

    private fun tryStart(intent: Intent): Boolean {
        return try {
            activity.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }

    private fun resolveRegionIso(): String {
        val telephonyManager = activity.getSystemService(TelephonyManager::class.java)
        val fromSim = telephonyManager?.simCountryIso?.takeIf { it.isNotBlank() }
        val fromNetwork = telephonyManager?.networkCountryIso?.takeIf { it.isNotBlank() }

        return (fromSim ?: fromNetwork ?: Locale.getDefault().country).uppercase(Locale.US)
    }

    companion object {
        private const val PRIMARY_WHATSAPP_PACKAGE = "com.whatsapp"
        private val WHATSAPP_PACKAGES = listOf(PRIMARY_WHATSAPP_PACKAGE, "com.whatsapp.w4b")
        internal const val WHATSAPP_VIDEO_CALL_MIME_TYPE = "vnd.android.cursor.item/vnd.com.whatsapp.video.call"

        internal fun selectPreferredPackage(installedPackages: Set<String>): String? {
            return WHATSAPP_PACKAGES.firstOrNull { installedPackages.contains(it) }
        }

        internal fun buildContactVideoCallIntent(dataId: Long, packageName: String): Intent {
            return Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(buildContactDataUri(dataId), WHATSAPP_VIDEO_CALL_MIME_TYPE)
                setPackage(packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        internal fun buildContactDataUri(dataId: Long): Uri {
            return Uri.withAppendedPath(ContactsContract.Data.CONTENT_URI, dataId.toString())
        }

        internal fun buildVideoCallUri(phoneNumber: String, regionIso: String? = null): Uri {
            val normalizedPhone = normalizeForCall(phoneNumber, regionIso)
            return Uri.parse("whatsapp://call")
                .buildUpon()
                .encodedQuery("phone=$normalizedPhone&video=true")
                .build()
        }

        internal fun buildVideoCallIntent(phoneNumber: String, packageName: String, regionIso: String? = null): Intent {
            return Intent(Intent.ACTION_VIEW, buildVideoCallUri(phoneNumber, regionIso)).apply {
                setPackage(packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        internal fun sanitizePhoneNumber(raw: String): String {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) {
                return ""
            }

            val builder = StringBuilder(trimmed.length)
            trimmed.forEach { character ->
                when {
                    character.isDigit() -> builder.append(character)
                    character == '+' && builder.isEmpty() -> builder.append(character)
                }
            }

            if (builder.length == 1 && builder[0] == '+') {
                return ""
            }

            return builder.toString()
        }

        private fun normalizeForCall(phoneNumber: String, regionIso: String?): String {
            val normalizedE164 = PhoneNumberUtils.formatNumberToE164(phoneNumber, regionIso)
                ?: if (phoneNumber.startsWith("+")) phoneNumber else "+$phoneNumber"

            return normalizedE164.filter { it == '+' || it.isDigit() }
        }
    }
}
