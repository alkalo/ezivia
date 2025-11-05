package com.ezivia.utilities.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PinStorageInstrumentedTest {

    private lateinit var context: Context
    private lateinit var storage: PinStorage

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        storage = PinStorage(context)
        storage.clearPin()
    }

    @After
    fun tearDown() {
        storage.clearPin()
    }

    @Test
    fun setPin_and_verify_successfulRoundTrip() {
        assertFalse(storage.isPinConfigured())

        storage.setPin("1234")

        assertTrue(storage.isPinConfigured())
        assertTrue(storage.verifyPin("1234"))
        assertFalse(storage.verifyPin("0000"))
    }

    @Test
    fun clearPin_removesStoredHash() {
        storage.setPin("4321")
        assertTrue(storage.isPinConfigured())

        storage.clearPin()

        assertFalse(storage.isPinConfigured())
        assertFalse(storage.verifyPin("4321"))
    }
}
