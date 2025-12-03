package com.ezivia.communication.whatsapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.core.content.ContextCompat
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

        val dataId = findWhatsAppVideoCallIdForNumber(activity, sanitizedNumber)
        if (dataId == null) {
            DiagnosticsLog.record(
                source = "WhatsAppLauncher",
                message = "No se encontró dataId de videollamada para el contacto"
            )
            showToast("No se ha encontrado la opción de videollamada de WhatsApp para este contacto.")
            return false
        }

        return launchVideoCall(dataId, installedPackage)
    }

    private fun launchVideoCall(dataId: Long, packageName: String): Boolean {
        DiagnosticsLog.record(
            source = "WhatsAppLauncher",
            message = "Lanzando videollamada con paquete $packageName y dataId=$dataId"
        )
        return when (val result = startWhatsAppVideoCall(activity, dataId, packageName)) {
            LaunchResult.Success -> true
            LaunchResult.PackageMissing -> {
                DiagnosticsLog.record(
                    source = "WhatsAppLauncher",
                    message = "WhatsApp no respondió al intento de abrir la videollamada"
                )
                showToast("No se pudo abrir la videollamada de WhatsApp. Revisa el número o inténtalo de nuevo.")
                false
            }
            is LaunchResult.LaunchError -> {
                DiagnosticsLog.record(
                    source = "WhatsAppLauncher",
                    message = "Falló el intento de videollamada: ${result.reason}"
                )
                showToast("No se pudo abrir la videollamada de WhatsApp. Revisa el número o inténtalo de nuevo.")
                false
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

    /**
     * Busca el identificador de videollamada de WhatsApp para un número concreto usando
     * el [ContactsContract.Data]. Este método se apoya en un MIME type no documentado
     * oficialmente por WhatsApp y podría dejar de funcionar si cambian su integración
     * con la agenda.
     */
    fun findWhatsAppVideoCallIdForNumber(
        context: Context,
        phoneNumber: String
    ): Long? {
        val sanitizedNumber = sanitizePhoneNumber(phoneNumber)
        if (sanitizedNumber.isEmpty()) {
            DiagnosticsLog.record(
                source = "WhatsAppLauncher",
                message = "No hay número válido para resolver videollamada"
            )
            return null
        }

        if (!hasContactsPermission(context)) {
            DiagnosticsLog.record(
                source = "WhatsAppLauncher",
                message = "Sin permiso READ_CONTACTS para resolver videollamada"
            )
            return null
        }

        val (selection, selectionArgs) = buildVideoCallLookup(sanitizedNumber)
        return context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.Data._ID),
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

    /**
     * Lanza un intent directo hacia la acción de videollamada de WhatsApp para el
     * [dataId] de agenda proporcionado. También usa el MIME type interno de WhatsApp
     * para abrir la pantalla de videollamada.
     */
    fun startWhatsAppVideoCall(
        context: Context,
        dataId: Long,
        packageName: String = PRIMARY_WHATSAPP_PACKAGE
    ): LaunchResult {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(buildContactDataUri(dataId), WHATSAPP_VIDEO_CALL_MIME_TYPE)
            setPackage(packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            LaunchResult.Success
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "No se pudo abrir WhatsApp para la videollamada.",
                Toast.LENGTH_LONG
            ).show()
            LaunchResult.PackageMissing
        } catch (error: Exception) {
            LaunchResult.LaunchError(error.message ?: error::class.java.simpleName)
        }
    }

    private fun resolveRegionIso(): String {
        val telephonyManager = activity.getSystemService(TelephonyManager::class.java)
        return resolveRegionIso(
            simCountryProvider = { telephonyManager?.simCountryIso },
            networkCountryProvider = { telephonyManager?.networkCountryIso },
            defaultCountryProvider = { Locale.getDefault().country }
        )
    }

    companion object {
        private const val PRIMARY_WHATSAPP_PACKAGE = "com.whatsapp"
        // Ordenadas por prioridad; añadir aquí nuevas variantes oficiales o betas cuando surjan.
        private val WHATSAPP_PACKAGES = listOf(
            PRIMARY_WHATSAPP_PACKAGE,
            "com.whatsapp.w4b",
            "com.whatsapp.w4b.smb",
            "com.whatsapp.w4b.beta",
            "com.whatsapp.beta"
        )
        internal const val WHATSAPP_VIDEO_CALL_MIME_TYPE = "vnd.android.cursor.item/vnd.com.whatsapp.video.call"

        internal sealed class LaunchResult {
            data object Success : LaunchResult()
            data object PackageMissing : LaunchResult()
            data class LaunchError(val reason: String) : LaunchResult()
        }

        internal fun selectPreferredPackage(installedPackages: Set<String>): String? {
            return WHATSAPP_PACKAGES.firstOrNull { installedPackages.contains(it) }
        }

        internal fun chooseVideoCallIntent(
            dataId: Long?,
            phoneNumber: String,
            packageName: String,
            regionIso: String?
        ): Intent {
            return if (dataId != null) {
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
                buildVideoCallIntent(phoneNumber, packageName, regionIso)
            }
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

        internal fun buildVideoCallLookup(phoneNumber: String): Pair<String, Array<String>> {
            val sanitized = sanitizePhoneNumber(phoneNumber)
            val likeNumber = "%$sanitized%"

            val selection =
                "${ContactsContract.Data.MIMETYPE}=? AND (${Phone.NORMALIZED_NUMBER} LIKE ? OR ${Phone.NUMBER} LIKE ?)"
            val selectionArgs = arrayOf(WHATSAPP_VIDEO_CALL_MIME_TYPE, likeNumber, likeNumber)

            return selection to selectionArgs
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

        internal fun resolveRegionIso(
            simCountryProvider: () -> String?,
            networkCountryProvider: () -> String?,
            defaultCountryProvider: () -> String,
        ): String {
            val simIso = runCatching { simCountryProvider() }.getOrNull().orEmpty()
            val networkIso = runCatching { networkCountryProvider() }.getOrNull().orEmpty()
            val defaultIso = runCatching { defaultCountryProvider() }.getOrNull().orEmpty()

            val chosenIso = listOf(simIso, networkIso, defaultIso, Locale.US.country)
                .firstOrNull { it.isNotBlank() }
                ?: Locale.US.country

            return chosenIso.uppercase(Locale.US)
        }

        private fun hasContactsPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
