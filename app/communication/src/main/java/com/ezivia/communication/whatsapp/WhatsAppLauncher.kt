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
        val sanitizedNumber = contact.phoneNumber.trim()
        if (sanitizedNumber.isEmpty()) {
            showToast("El contacto no tiene un número válido.")
            return false
        }

        if (!isWhatsAppInstalled()) {
            showInstallFallback()
            return false
        }

        showConfirmationDialog(contact, sanitizedNumber)
        return true
    }

    private fun showConfirmationDialog(contact: FavoriteContact, phoneNumber: String) {
        AlertDialog.Builder(activity)
            .setTitle("Videollamada por WhatsApp")
            .setMessage("¿Deseas iniciar una videollamada con ${contact.displayName}?")
            .setPositiveButton("Videollamar") { _, _ ->
                launchVideoCall(phoneNumber)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun launchVideoCall(phoneNumber: String) {
        val videoCallUri = Uri.parse("whatsapp://call?phone=" + Uri.encode(phoneNumber) + "&call_type=video")
        val intent = Intent(Intent.ACTION_VIEW, videoCallUri).apply {
            setPackage(WHATSAPP_PACKAGE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            activity.startActivity(intent)
        } catch (error: ActivityNotFoundException) {
            showToast("No se pudo abrir WhatsApp para la videollamada.")
            showInstallFallback()
        }
    }

    private fun isWhatsAppInstalled(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                activity.packageManager.getPackageInfo(
                    WHATSAPP_PACKAGE,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                activity.packageManager.getPackageInfo(WHATSAPP_PACKAGE, 0)
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
        val primaryIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$WHATSAPP_PACKAGE")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (!tryStart(primaryIntent)) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$WHATSAPP_PACKAGE")).apply {
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
        private const val WHATSAPP_PACKAGE = "com.whatsapp"
    }
}
