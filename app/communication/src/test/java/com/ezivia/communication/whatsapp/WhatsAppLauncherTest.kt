package com.ezivia.communication.whatsapp

import android.content.Intent
import android.provider.ContactsContract
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WhatsAppLauncherTest {

    @Test
    fun sanitizePhoneNumber_removesFormattingCharacters() {
        val sanitized = WhatsAppLauncher.sanitizePhoneNumber(" +34 600-123-456 ")

        assertThat(sanitized).isEqualTo("+34600123456")
    }

    @Test
    fun sanitizePhoneNumber_discardsLettersAndSymbols() {
        val sanitized = WhatsAppLauncher.sanitizePhoneNumber("(+34) 600 ABC 789")

        assertThat(sanitized).isEqualTo("+34600789")
    }

    @Test
    fun sanitizePhoneNumber_handlesPlusOnlyWhenLeading() {
        val sanitized = WhatsAppLauncher.sanitizePhoneNumber("++34 600+123")

        assertThat(sanitized).isEqualTo("+34600123")
    }

    @Test
    fun sanitizePhoneNumber_returnsEmptyWhenNoDigits() {
        val sanitized = WhatsAppLauncher.sanitizePhoneNumber("   +   ")

        assertThat(sanitized).isEmpty()
    }

    @Test
    fun selectPreferredPackage_prioritizesConsumerVariantWhenBothInstalled() {
        val installed = setOf("com.whatsapp", "com.whatsapp.w4b")

        val resolved = WhatsAppLauncher.selectPreferredPackage(installed)

        assertThat(resolved).isEqualTo("com.whatsapp")
    }

    @Test
    fun selectPreferredPackage_returnsBusinessWhenOnlyBusinessIsPresent() {
        val installed = setOf("com.whatsapp.w4b")

        val resolved = WhatsAppLauncher.selectPreferredPackage(installed)

        assertThat(resolved).isEqualTo("com.whatsapp.w4b")
    }

    @Test
    fun selectPreferredPackage_returnsNullWhenNoOptionsInstalled() {
        val resolved = WhatsAppLauncher.selectPreferredPackage(emptySet())

        assertThat(resolved).isNull()
    }

    @Test
    fun buildVideoCallUri_convertsLeadingPlusToPhoneParameter() {
        val uri = WhatsAppLauncher.buildVideoCallUri("+34600123456", "ES")

        assertThat(uri.toString()).isEqualTo("whatsapp://call?phone=+34600123456&video=true")
    }

    @Test
    fun buildVideoCallUri_convertsPlainDigitsToPhoneParameter() {
        val uri = WhatsAppLauncher.buildVideoCallUri("34600123456", "ES")

        assertThat(uri.toString()).isEqualTo("whatsapp://call?phone=+34600123456&video=true")
    }

    @Test
    fun buildVideoCallUri_formatsLocalNumberToE164UsingRegion() {
        val uri = WhatsAppLauncher.buildVideoCallUri("600123456", "ES")

        assertThat(uri.toString()).isEqualTo("whatsapp://call?phone=+34600123456&video=true")
    }

    @Test
    fun buildVideoCallUri_normalizesLeadingPlusAndKeepsCallValid() {
        val uri = WhatsAppLauncher.buildVideoCallUri("+12 34 567", "ES")

        assertThat(uri.toString()).isEqualTo("whatsapp://call?phone=+1234567&video=true")
    }

    @Test
    fun buildVideoCallUri_handlesUnknownRegionByFallingBackToPlusPrefix() {
        val uri = WhatsAppLauncher.buildVideoCallUri("987654321", "ZZ")

        assertThat(uri.toString()).isEqualTo("whatsapp://call?phone=+987654321&video=true")
    }

    @Test
    fun buildContactDataUri_pointsToContactsDataEntry() {
        val uri = WhatsAppLauncher.buildContactDataUri(42)

        assertThat(uri.toString()).isEqualTo("content://com.android.contacts/data/42")
    }

    @Test
    fun buildContactVideoCallIntent_targetsWhatsAppMimeAndPackage() {
        val intent = WhatsAppLauncher.buildContactVideoCallIntent(10, "com.whatsapp")

        assertThat(intent.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(intent.data.toString()).isEqualTo("content://com.android.contacts/data/10")
        assertThat(intent.type).isEqualTo(WhatsAppLauncher.WHATSAPP_VIDEO_CALL_MIME_TYPE)
        assertThat(intent.`package`).isEqualTo("com.whatsapp")
        assertThat(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0)
    }

    @Test
    fun buildVideoCallLookup_buildsSelectionAndArgsWithSanitizedNumber() {
        val (selection, args) = WhatsAppLauncher.buildVideoCallLookup(" +34 600 123 456 ")

        assertThat(selection).contains(ContactsContract.Data.MIMETYPE)
        assertThat(selection).contains(ContactsContract.CommonDataKinds.Phone.NUMBER)
        assertThat(args[0]).isEqualTo(WhatsAppLauncher.WHATSAPP_VIDEO_CALL_MIME_TYPE)
        assertThat(args[1]).isEqualTo("%+34600123456%")
        assertThat(args[2]).isEqualTo("%+34600123456%")
    }

    @Test
    fun buildVideoCallIntent_setsExplicitPackageAndViewAction() {
        val intent = WhatsAppLauncher.buildVideoCallIntent("34600123456", "com.whatsapp", "ES")

        assertThat(intent.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(intent.data.toString()).isEqualTo("whatsapp://call?phone=+34600123456&video=true")
        assertThat(intent.`package`).isEqualTo("com.whatsapp")
        assertThat(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0)
    }

    @Test
    fun buildWebVideoCallUri_usesWaMeWithNormalizedDigits() {
        val uri = WhatsAppLauncher.buildWebVideoCallUri("+34 600 123 456", "ES")

        assertThat(uri.toString()).isEqualTo("https://wa.me/34600123456?call=true&video=true")
    }

    @Test
    fun buildWebVideoCallIntent_setsPackageWhenProvided() {
        val intent = WhatsAppLauncher.buildWebVideoCallIntent("34600123456", "com.whatsapp", "ES")

        assertThat(intent.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(intent.data.toString()).isEqualTo("https://wa.me/34600123456?call=true&video=true")
        assertThat(intent.`package`).isEqualTo("com.whatsapp")
        assertThat(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0)
    }

    @Test
    fun buildWebVideoCallIntent_omitsPackageWhenNull() {
        val intent = WhatsAppLauncher.buildWebVideoCallIntent("34600123456", null, "ES")

        assertThat(intent.`package`).isNull()
    }

    @Test
    fun chooseVideoCallIntent_usesContactDataWhenAvailable() {
        val intent = WhatsAppLauncher.chooseVideoCallIntent(25, "34600123456", "com.whatsapp", "ES")

        assertThat(intent.data.toString()).isEqualTo("content://com.android.contacts/data/25")
        assertThat(intent.type).isEqualTo(WhatsAppLauncher.WHATSAPP_VIDEO_CALL_MIME_TYPE)
    }

    @Test
    fun chooseVideoCallIntent_fallsBackToUriWhenNoDataId() {
        val intent = WhatsAppLauncher.chooseVideoCallIntent(null, "34600123456", "com.whatsapp", "ES")

        assertThat(intent.data.toString()).isEqualTo("whatsapp://call?phone=+34600123456&video=true")
        assertThat(intent.type).isNull()
    }

    @Test
    fun resolveRegionIso_prefersSimAndUppercases() {
        val region = WhatsAppLauncher.resolveRegionIso(
            simCountryProvider = { "es" },
            networkCountryProvider = { "mx" },
            defaultCountryProvider = { "ar" },
        )

        assertThat(region).isEqualTo("ES")
    }

    @Test
    fun resolveRegionIso_usesNetworkWhenSimEmpty() {
        val region = WhatsAppLauncher.resolveRegionIso(
            simCountryProvider = { "" },
            networkCountryProvider = { "us" },
            defaultCountryProvider = { "ar" },
        )

        assertThat(region).isEqualTo("US")
    }

    @Test
    fun resolveRegionIso_fallsBackToDefaultWhenTelephonyMissing() {
        val region = WhatsAppLauncher.resolveRegionIso(
            simCountryProvider = { null },
            networkCountryProvider = { null },
            defaultCountryProvider = { "br" },
        )

        assertThat(region).isEqualTo("BR")
    }

    @Test
    fun resolveRegionIso_handlesErrorsAndUsesSafeFallback() {
        val region = WhatsAppLauncher.resolveRegionIso(
            simCountryProvider = { throw SecurityException("carrier locked") },
            networkCountryProvider = { throw SecurityException("no signal") },
            defaultCountryProvider = { "" },
        )

        assertThat(region).isEqualTo("US")
    }
}
