package com.ezivia.launcher

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ContactWizardValidatorTest {

    @Test
    fun `valid name requires at least three characters`() {
        assertThat(ContactWizardValidator.isValidName("Ana")).isTrue()
        assertThat(ContactWizardValidator.isValidName(" A ")).isFalse()
        assertThat(ContactWizardValidator.isValidName("An")).isFalse()
    }

    @Test
    fun `valid phone requires seven digits`() {
        assertThat(ContactWizardValidator.isValidPhone("600123456")).isTrue()
        assertThat(ContactWizardValidator.isValidPhone("+34 600 12 34 56")).isTrue()
        assertThat(ContactWizardValidator.isValidPhone("12345")).isFalse()
    }

    @Test
    fun `voice confirmation matches friendly keywords`() {
        assertThat(ContactWizardValidator.matchesVoiceConfirmation("sí, confirmar datos"))
            .isTrue()
        assertThat(ContactWizardValidator.matchesVoiceConfirmation("ok guardar contacto"))
            .isTrue()
        assertThat(ContactWizardValidator.matchesVoiceConfirmation("no todavía"))
            .isFalse()
    }
}
