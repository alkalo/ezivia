package com.ezivia.utilities.caregiver

import android.content.ContextWrapper
import android.content.SharedPreferences
import android.test.mock.MockContext
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CaregiverPreferencesTest {

    private val preferences = InMemorySharedPreferences()
    private val context = ContextWrapper(MockContext())
    private val caregiverPreferences = CaregiverPreferences(context, preferences)

    @Test
    fun saveAndLoadCaregivers_roundTripsData() {
        val caregivers = listOf(
            CaregiverInfo(name = "Alice", phoneNumber = "12345", relationship = "Sister"),
            CaregiverInfo(name = "Bob", phoneNumber = "67890", relationship = "Neighbour"),
        )

        caregiverPreferences.saveCaregivers(caregivers)

        assertEquals(caregivers, caregiverPreferences.loadCaregivers())
    }

    @Test
    fun loadCaregivers_returnsEmptyListWhenCorrupted() {
        preferences.edit().putString("caregivers", "not json").apply()

        assertTrue(caregiverPreferences.loadCaregivers().isEmpty())
    }

    private class InMemorySharedPreferences : SharedPreferences {
        private val data = mutableMapOf<String, Any?>()
        private val listeners = mutableSetOf<SharedPreferences.OnSharedPreferenceChangeListener>()

        override fun getAll(): MutableMap<String, *> = HashMap(data)

        override fun getString(key: String?, defValue: String?): String? =
            data[key] as? String ?: defValue

        @Suppress("UNCHECKED_CAST")
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
            val value = data[key] as? Set<String>
            return value?.toMutableSet() ?: defValues
        }

        override fun getInt(key: String?, defValue: Int): Int = data[key] as? Int ?: defValue

        override fun getLong(key: String?, defValue: Long): Long = data[key] as? Long ?: defValue

        override fun getFloat(key: String?, defValue: Float): Float = data[key] as? Float ?: defValue

        override fun getBoolean(key: String?, defValue: Boolean): Boolean =
            data[key] as? Boolean ?: defValue

        override fun contains(key: String?): Boolean = key in data

        override fun edit(): SharedPreferences.Editor = Editor()

        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
            if (listener != null) {
                listeners += listener
            }
        }

        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
            if (listener != null) {
                listeners -= listener
            }
        }

        private inner class Editor : SharedPreferences.Editor {
            private val updates = mutableMapOf<String, Any?>()
            private val removals = mutableSetOf<String>()
            private var clearRequested = false

            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                if (key != null) {
                    updates[key] = value
                    removals -= key
                }
                return this
            }

            override fun putStringSet(
                key: String?,
                values: MutableSet<String>?,
            ): SharedPreferences.Editor {
                if (key != null) {
                    updates[key] = values?.toSet()
                    removals -= key
                }
                return this
            }

            override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
                if (key != null) {
                    updates[key] = value
                    removals -= key
                }
                return this
            }

            override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
                if (key != null) {
                    updates[key] = value
                    removals -= key
                }
                return this
            }

            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
                if (key != null) {
                    updates[key] = value
                    removals -= key
                }
                return this
            }

            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
                if (key != null) {
                    updates[key] = value
                    removals -= key
                }
                return this
            }

            override fun remove(key: String?): SharedPreferences.Editor {
                if (key != null) {
                    removals += key
                    updates -= key
                }
                return this
            }

            override fun clear(): SharedPreferences.Editor {
                clearRequested = true
                updates.clear()
                removals.clear()
                return this
            }

            override fun commit(): Boolean {
                val changedKeys = mutableSetOf<String>()
                if (clearRequested) {
                    changedKeys += data.keys
                    data.clear()
                }

                for (key in removals) {
                    if (data.remove(key) != null) {
                        changedKeys += key
                    }
                }

                for ((key, value) in updates) {
                    if (data[key] != value) {
                        changedKeys += key
                    }
                    if (value == null) {
                        data.remove(key)
                    } else {
                        data[key] = value
                    }
                }

                for (key in changedKeys) {
                    for (listener in listeners) {
                        listener.onSharedPreferenceChanged(this@InMemorySharedPreferences, key)
                    }
                }

                return true
            }

            override fun apply() {
                commit()
            }
        }
    }
}
