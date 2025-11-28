package com.ezivia.communication

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class DiagnosticsLogTest {

    @Before
    fun setUp() {
        DiagnosticsLog.clear()
    }

    @After
    fun tearDown() {
        DiagnosticsLog.clear()
    }

    @Test
    fun `record stores entries in order`() {
        DiagnosticsLog.record("source1", "first")
        DiagnosticsLog.record("source2", "second")

        val entries = DiagnosticsLog.snapshot()

        assertThat(entries).hasSize(2)
        assertThat(entries[0].source).isEqualTo("source1")
        assertThat(entries[0].message).isEqualTo("first")
        assertThat(entries[1].source).isEqualTo("source2")
        assertThat(entries[1].message).isEqualTo("second")
    }

    @Test
    fun `record trims list when exceeding maximum`() {
        repeat(105) { index ->
            DiagnosticsLog.record("src", "log_$index")
        }

        val entries = DiagnosticsLog.snapshot()

        assertThat(entries).hasSize(100)
        assertThat(entries.first().message).isEqualTo("log_5")
        assertThat(entries.last().message).isEqualTo("log_104")
    }
}
