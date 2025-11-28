package com.ezivia.communication.whatsapp

import android.content.Intent
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
    fun buildVideoCallUri_stripsLeadingPlusAndUsesJidParameter() {
        val uri = WhatsAppLauncher.buildVideoCallUri("+34600123456")

        assertThat(uri.toString()).isEqualTo("whatsapp://call?phone=34600123456&video=true")
    }

    @Test
    fun buildVideoCallUri_keepsPlainDigitsInJid() {
        val uri = WhatsAppLauncher.buildVideoCallUri("34600123456")

        assertThat(uri.toString()).isEqualTo("whatsapp://call?phone=34600123456&video=true")
    }

    @Test
    fun buildVideoCallIntent_setsExplicitPackageAndViewAction() {
        val intent = WhatsAppLauncher.buildVideoCallIntent("34600123456", "com.whatsapp")

        assertThat(intent.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(intent.data.toString()).isEqualTo("whatsapp://call?phone=34600123456&video=true")
        assertThat(intent.`package`).isEqualTo("com.whatsapp")
        assertThat(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0)
    }
}
