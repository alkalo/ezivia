package com.ezivia.utilities.caregiver

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

private const val CAREGIVER_PREFS_NAME = "ezivia_caregiver_prefs"
private const val KEY_CAREGIVERS = "caregivers"

/**
 * Persists the caregiver contacts that are configured during onboarding so the
 * launcher and settings experiences can surface them consistently.
 */
class CaregiverPreferences(context: Context) {

    private val preferences =
        context.getSharedPreferences(CAREGIVER_PREFS_NAME, Context.MODE_PRIVATE)

    fun saveCaregivers(caregivers: List<CaregiverInfo>) {
        val jsonArray = JSONArray()
        caregivers.forEach { caregiver ->
            val jsonObject = JSONObject().apply {
                put("name", caregiver.name)
                put("phoneNumber", caregiver.phoneNumber)
                put("relationship", caregiver.relationship)
            }
            jsonArray.put(jsonObject)
        }
        preferences.edit().putString(KEY_CAREGIVERS, jsonArray.toString()).apply()
    }

    fun loadCaregivers(): List<CaregiverInfo> {
        val serialized = preferences.getString(KEY_CAREGIVERS, null) ?: return emptyList()
        return runCatching {
            val jsonArray = JSONArray(serialized)
            buildList(jsonArray.length()) { index ->
                val jsonObject = jsonArray.getJSONObject(index)
                add(
                    CaregiverInfo(
                        name = jsonObject.getString("name"),
                        phoneNumber = jsonObject.getString("phoneNumber"),
                        relationship = jsonObject.getString("relationship"),
                    )
                )
            }
        }.getOrDefault(emptyList())
    }
}
