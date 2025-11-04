package com.ezivia.utilities

import android.content.Context

/**
 * Provides basic information about the device owner so screens can display
 * friendly, human-centric messaging for seniors.
 */
class DeviceProfileProvider {
    fun createProfile(context: Context): DeviceProfile {
        val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val ownerName = sharedPreferences.getString(KEY_OWNER_NAME, DEFAULT_OWNER_NAME) ?: DEFAULT_OWNER_NAME
        return DeviceProfile(ownerName = ownerName)
    }

    data class DeviceProfile(
        val ownerName: String
    )

    companion object {
        private const val PREFERENCES_NAME = "ezivia_preferences"
        private const val KEY_OWNER_NAME = "owner_name"
        private const val DEFAULT_OWNER_NAME = "la persona mayor"
    }
}
