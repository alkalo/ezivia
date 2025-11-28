package com.ezivia.communication.whatsapp

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import com.ezivia.communication.contacts.FavoriteContact

/**
 * Encapsula la lógica necesaria para iniciar videollamadas de WhatsApp
 * pensadas para contactos favoritos dentro de Ezivia.
 */
class WhatsAppLauncher(private val activity: Activity) {

    /**
     * Solicita confirmación al usuario e intenta iniciar una videollamada de WhatsApp
     * con el [contact]. Devuelve `true` si se mostró alguna interfaz relacionada con
     * la acción (confirmación o tienda) y `false` si no se pudo continuar.
     */
    fun startFavoriteVideoCall(contact: FavoriteContact): Boolean {
        val sanitizedNumber = sanitizePhoneNumber(contact.phoneNumber)
        if (sanitizedNumber.isEmpty() || sanitizedNumber == "+") {
            showToast("El contacto no tiene un número válido.")
            return false
        }

        val installedPackage = resolveInstalledWhatsAppPackage()
        if (installedPackage == null) {
            showInstallFallback()
            return false
        }

        showConfirmationDialog(contact, sanitizedNumber, installedPackage)
        return true
    }

    private fun showConfirmationDialog(contact: FavoriteContact, phoneNumber: String, packageName: String) {
        AlertDialog.Builder(activity)
            .setTitle("Videollamada por WhatsApp")
            .setMessage("¿Deseas iniciar una videollamada con ${contact.displayName}?")
            .setPositiveButton("Videollamar") { _, _ ->
                launchVideoCall(phoneNumber, packageName)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun launchVideoCall(phoneNumber: String, packageName: String) {
        val videoCallUri = Uri.parse("whatsapp://call?phone=" + Uri.encode(phoneNumber) + "&call_type=video")
        val intent = Intent(Intent.ACTION_VIEW, videoCallUri).apply {
            setPackage(packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            activity.startActivity(intent)
        } catch (error: ActivityNotFoundException) {
            showToast("No se pudo abrir WhatsApp para la videollamada.")
            showInstallFallback()
        }
    }

    private fun resolveInstalledWhatsAppPackage(): String? {
        val installedPackages = WHATSAPP_PACKAGES.filter { isWhatsAppInstalled(it) }.toSet()
        return selectPreferredPackage(installedPackages)
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

    companion object {
        private const val PRIMARY_WHATSAPP_PACKAGE = "com.whatsapp"
        private val WHATSAPP_PACKAGES = listOf(PRIMARY_WHATSAPP_PACKAGE, "com.whatsapp.w4b")

        internal fun selectPreferredPackage(installedPackages: Set<String>): String? {
            return WHATSAPP_PACKAGES.firstOrNull { installedPackages.contains(it) }
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
    }
}
