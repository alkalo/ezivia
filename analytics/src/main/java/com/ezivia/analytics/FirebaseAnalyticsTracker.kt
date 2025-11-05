package com.ezivia.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Firebase specific implementation that translates our [AnalyticsPayload] into a Firebase
 * compatible [Bundle].
 */
class FirebaseAnalyticsTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsTracker {

    override fun track(payload: AnalyticsPayload) {
        firebaseAnalytics.logEvent(payload.name, payload.parameters.toBundle())
    }

    private fun Map<String, Any?>.toBundle(): Bundle = Bundle().apply {
        forEach { (key, value) ->
            when (value) {
                null -> Unit
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Double -> putDouble(key, value)
                is Float -> putFloat(key, value)
                is Boolean -> putBoolean(key, value)
                else -> putString(key, value.toString())
            }
        }
    }
}
