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
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale

/**
 * Encapsula la lógica necesaria para iniciar videollamadas de WhatsApp
 * pensadas para contactos favoritos dentro de Ezivia.
 */
class WhatsAppLauncher(private val activity: Activity) {

    /**
     * Inicia directamente una videollamada de WhatsApp con el [contact]. Devuelve
     * un [VideoCallResult] con el estado de la acción para poder mostrar feedback
     * específico al usuario en función del fallo.
     */
    fun startFavoriteVideoCall(contact: FavoriteContact): VideoCallResult {
        val sanitizedNumber = sanitizePhoneNumber(contact.phoneNumber)
        if (sanitizedNumber.isEmpty() || sanitizedNumber == "+") {
            DiagnosticsLog.record(
                source = "WhatsAppLauncher",
                message = "Número vacío o inválido para la videollamada"
            )
            showToast("El contacto no tiene un número válido.")
            return VideoCallResult.InvalidNumber
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
            return VideoCallResult.WhatsappNotInstalled
        }

        return when (val lookupResult = findWhatsAppVideoCallIdForNumber(activity, sanitizedNumber)) {
            VideoCallLookupResult.PermissionMissing -> VideoCallResult.ContactsPermissionMissing
            VideoCallLookupResult.InvalidNumber -> VideoCallResult.InvalidNumber
            VideoCallLookupResult.NotFound -> {
                DiagnosticsLog.record(
                    source = "WhatsAppLauncher",
                    message = "No se encontró dataId de videollamada para el contacto"
                )
                DiagnosticsLog.record(
                    source = "WhatsAppLauncher",
                    message = "Intentando fallback web de videollamada con paquete $installedPackage"
                )

                val result = resolveNotFoundWithFallback(
                    fallbackLauncher = { number, packageName ->
                        launchWebVideoCallFallback(number, packageName)
                    },
                    phoneNumber = sanitizedNumber,
                    packageName = installedPackage,
                )

                if (result == VideoCallResult.VideoCallEntryMissing) {
                    showToast("No se ha encontrado la opción de videollamada de WhatsApp para este contacto.")
                }

                result
            }
            is VideoCallLookupResult.Found -> {
                val launchResult = launchVideoCall(lookupResult.dataId, installedPackage)
                mapLaunchResultToVideoCallResult(launchResult)
            }
        }
    }

    private fun launchVideoCall(dataId: Long, packageName: String): LaunchResult {
        DiagnosticsLog.record(
            source = "WhatsAppLauncher",
            message = "Lanzando videollamada con paquete $packageName y dataId=$dataId"
        )
        return when (val result = startWhatsAppVideoCall(activity, dataId, packageName)) {
            LaunchResult.Success -> LaunchResult.Success
            LaunchResult.PackageMissing -> {
                DiagnosticsLog.record(
                    source = "WhatsAppLauncher",
                    message = "WhatsApp no respondió al intento de abrir la videollamada"
                )
                showToast("No se pudo abrir la videollamada de WhatsApp. Revisa el número o inténtalo de nuevo.")
                LaunchResult.PackageMissing
            }
            is LaunchResult.LaunchError -> {
                DiagnosticsLog.record(
                    source = "WhatsAppLauncher",
                    message = "Falló el intento de videollamada: ${result.reason}"
                )
                showToast("No se pudo abrir la videollamada de WhatsApp. Revisa el número o inténtalo de nuevo.")
                result
            }
        }
    }

    private fun launchWebVideoCallFallback(phoneNumber: String, packageName: String): Boolean {
        val regionIso = resolveRegionIso()
        val intentWithPackage = buildWebVideoCallIntent(phoneNumber, packageName, regionIso)
        if (tryStart(intentWithPackage)) {
            DiagnosticsLog.record(
                source = "WhatsAppLauncher",
                message = "Fallback wa.me lanzado con paquete $packageName"
            )
            return true
        }

        DiagnosticsLog.record(
            source = "WhatsAppLauncher",
            message = "El fallback wa.me no pudo lanzarse con paquete; probando sin package"
        )

        val browserIntent = buildWebVideoCallIntent(phoneNumber, packageName = null, regionIso = regionIso)
        if (tryStart(browserIntent)) {
            DiagnosticsLog.record(
                source = "WhatsAppLauncher",
                message = "Fallback wa.me lanzado mediante navegador"
            )
            return true
        }

        DiagnosticsLog.record(
            source = "WhatsAppLauncher",
            message = "No se pudo lanzar el fallback wa.me"
        )

        showToast("No se pudo iniciar la videollamada. Verifica que el contacto tenga WhatsApp.")
        return false
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

    private fun startWhatsAppIntentChain(intents: List<Intent>, packageName: String): Boolean {
        intents.forEachIndexed { index, intent ->
            val started = tryStart(intent)
            if (started) {
                return true
            }

            DiagnosticsLog.record(
                source = "WhatsAppLauncher",
                message = if (index == 0 && intents.size > 1) {
                    "No se pudo abrir la videollamada con dataId; probando fallback whatsapp://call con paquete $packageName"
                } else {
                    "Intent de videollamada de WhatsApp #${index + 1}/${intents.size} falló con paquete $packageName"
                }
            )
        }

        return false
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
    internal fun findWhatsAppVideoCallIdForNumber(
        context: Context,
        phoneNumber: String
    ): VideoCallLookupResult {
        val sanitizedNumber = sanitizePhoneNumber(phoneNumber)
        if (sanitizedNumber.isEmpty()) {
            DiagnosticsLog.record(
                source = "WhatsAppLauncher",
                message = "No hay número válido para resolver videollamada"
            )
            return VideoCallLookupResult.InvalidNumber
        }

        if (!hasContactsPermission(context)) {
            DiagnosticsLog.record(
                source = "WhatsAppLauncher",
                message = "Sin permiso READ_CONTACTS para resolver videollamada"
            )
            return VideoCallLookupResult.PermissionMissing
        }

        val (selection, selectionArgs) = buildVideoCallLookup(sanitizedNumber)
        val dataId = context.contentResolver.query(
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

        return dataId?.let { VideoCallLookupResult.Found(it) } ?: VideoCallLookupResult.NotFound
    }

    /**
     * Lanza un intent directo hacia la acción de videollamada de WhatsApp para el
     * [dataId] de agenda proporcionado. También usa el MIME type interno de WhatsApp
     * para abrir la pantalla de videollamada.
     */
    internal fun startWhatsAppVideoCall(
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
            LaunchResult.PackageMissing
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

        internal fun mapLaunchResultToVideoCallResult(result: LaunchResult): VideoCallResult {
            return when (result) {
                LaunchResult.Success -> VideoCallResult.Success
                LaunchResult.PackageMissing -> VideoCallResult.LaunchError
                is LaunchResult.LaunchError -> VideoCallResult.LaunchError
            }
        }

        internal fun selectPreferredPackage(installedPackages: Set<String>): String? {
            return WHATSAPP_PACKAGES.firstOrNull { installedPackages.contains(it) }
        }

        internal fun chooseVideoCallIntent(
            dataId: Long?,
            phoneNumber: String,
            packageName: String,
            regionIso: String,
        ): Intent {
            return buildVideoCallIntentChain(dataId, phoneNumber, packageName, regionIso).first()
        }

        internal fun buildVideoCallIntentChain(
            dataId: Long?,
            phoneNumber: String,
            packageName: String,
            regionIso: String,
        ): List<Intent> {
            val intents = mutableListOf<Intent>()

            if (dataId != null) {
                DiagnosticsLog.record(
                    source = "WhatsAppLauncher",
                    message = "Usando dato de agenda para videollamada WhatsApp: dataId=$dataId"
                )
                intents += buildContactVideoCallIntent(dataId, packageName)
            } else {
                DiagnosticsLog.record(
                    source = "WhatsAppLauncher",
                    message = "No se encontró dataId de videollamada, usando URI directo"
                )
            }

            intents += buildVideoCallIntent(phoneNumber, packageName, regionIso)

            return intents
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

        internal fun buildVideoCallUri(phoneNumber: String, regionIso: String): Uri {
            val normalizedPhone = normalizeForCall(phoneNumber, regionIso)
            return Uri.parse("whatsapp://call")
                .buildUpon()
                .encodedQuery("phone=$normalizedPhone&video=true")
                .build()
        }

        internal fun buildVideoCallIntent(phoneNumber: String, packageName: String, regionIso: String): Intent {
            return Intent(Intent.ACTION_VIEW, buildVideoCallUri(phoneNumber, regionIso)).apply {
                setPackage(packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        internal fun buildWebVideoCallUri(phoneNumber: String, regionIso: String): Uri {
            val normalizedPhone = normalizeForCall(phoneNumber, regionIso)
            val plainDigits = normalizedPhone.filter { it.isDigit() }
            return Uri.parse("https://wa.me/$plainDigits")
                .buildUpon()
                .encodedQuery("call=true&video=true")
                .build()
        }

        internal fun buildWebVideoCallIntent(
            phoneNumber: String,
            packageName: String?,
            regionIso: String,
        ): Intent {
            return Intent(Intent.ACTION_VIEW, buildWebVideoCallUri(phoneNumber, regionIso)).apply {
                packageName?.let { setPackage(it) }
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

        private fun normalizeForCall(phoneNumber: String, regionIso: String): String {
            val sanitizedNumber = sanitizePhoneNumber(phoneNumber)
            if (sanitizedNumber.isEmpty()) return ""

            val effectiveRegion = resolveFormattingRegion(regionIso)
            val normalizedE164 = PhoneNumberUtils.formatNumberToE164(sanitizedNumber, effectiveRegion)
                ?: formatWithCountryCodeFallback(sanitizedNumber, effectiveRegion)

            return normalizedE164.filter { it == '+' || it.isDigit() }
        }

        private fun resolveFormattingRegion(regionIso: String?): String {
            return regionIso
                ?.takeIf { it.isNotBlank() }
                ?.uppercase(Locale.US)
                ?: Locale.getDefault().country.takeIf { it.isNotBlank() }?.uppercase(Locale.US)
                ?: Locale.US.country
        }

        private fun formatWithCountryCodeFallback(phoneNumber: String, regionIso: String): String {
            if (phoneNumber.startsWith("+")) return phoneNumber

            val phoneUtil = PhoneNumberUtil.getInstance()
            val countryCode = runCatching {
                phoneUtil.getCountryCodeForRegion(regionIso)
            }.getOrNull()?.takeIf { it != 0 }
                ?: phoneUtil.getCountryCodeForRegion(Locale.US.country)
                .takeIf { it != 0 }
                ?: 1

            return "+$countryCode$phoneNumber"
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

        internal fun resolveNotFoundWithFallback(
            fallbackLauncher: (String, String) -> Boolean,
            phoneNumber: String,
            packageName: String,
        ): VideoCallResult {
            DiagnosticsLog.record(
                source = "WhatsAppLauncher",
                message = "Fallback de videollamada web solicitado para $packageName"
            )

            val fallbackStarted = fallbackLauncher(phoneNumber, packageName)
            return if (fallbackStarted) {
                VideoCallResult.Success
            } else {
                VideoCallResult.VideoCallEntryMissing
            }
        }
    }

    sealed class VideoCallResult {
        data object Success : VideoCallResult()
        data object InvalidNumber : VideoCallResult()
        data object WhatsappNotInstalled : VideoCallResult()
        data object VideoCallEntryMissing : VideoCallResult()
        data object ContactsPermissionMissing : VideoCallResult()
        data object LaunchError : VideoCallResult()
    }

    internal sealed class VideoCallLookupResult {
        data class Found(val dataId: Long) : VideoCallLookupResult()
        data object InvalidNumber : VideoCallLookupResult()
        data object PermissionMissing : VideoCallLookupResult()
        data object NotFound : VideoCallLookupResult()
    }
}
