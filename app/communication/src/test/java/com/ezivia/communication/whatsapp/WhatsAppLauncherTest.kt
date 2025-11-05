package com.ezivia.communication.whatsapp

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
}
