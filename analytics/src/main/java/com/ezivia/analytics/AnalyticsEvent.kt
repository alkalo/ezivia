package com.ezivia.analytics

/**
 * Describes the payload that is ready to be dispatched to every analytics provider.
 */
data class AnalyticsPayload(
    val name: String,
    val parameters: Map<String, Any?> = emptyMap()
)

/**
 * Represents a domain event that we want to track in the app. Each event specifies the
 * keys that should be anonymised before dispatching to providers.
 */
sealed class AnalyticsEvent(
    val name: String,
    val parameters: Map<String, Any?> = emptyMap(),
    private val sensitiveKeys: Set<String> = emptySet()
) {

    fun sanitize(anonymizer: DataAnonymizer): AnalyticsPayload {
        val safeParams = anonymizer.anonymizeMap(parameters, sensitiveKeys)
        return AnalyticsPayload(name = name, parameters = safeParams)
    }

    data object AppOpened : AnalyticsEvent(name = "app_opened")

    data class FeatureAccessed(
        val featureName: String,
        val entryPoint: String?
    ) : AnalyticsEvent(
        name = "feature_accessed",
        parameters = mapOf(
            "feature_name" to featureName,
            "entry_point" to entryPoint
        )
    )

    data class ContactDialed(
        val contactName: String,
        val hasPhoto: Boolean
    ) : AnalyticsEvent(
        name = "contact_dialed",
        parameters = mapOf(
            "contact_name" to contactName,
            "has_photo" to hasPhoto
        ),
        sensitiveKeys = setOf("contact_name")
    )

    data class EmergencyTriggered(
        val method: String,
        val wasCancelled: Boolean
    ) : AnalyticsEvent(
        name = "emergency_triggered",
        parameters = mapOf(
            "method" to method,
            "was_cancelled" to wasCancelled
        )
    )

    data class SupportRequested(
        val topic: String,
        val channel: String
    ) : AnalyticsEvent(
        name = "support_requested",
        parameters = mapOf(
            "topic" to topic,
            "channel" to channel
        )
    )

    data class AccessibilityAdjusted(
        val textSize: String,
        val contrastEnabled: Boolean
    ) : AnalyticsEvent(
        name = "accessibility_adjusted",
        parameters = mapOf(
            "text_size" to textSize,
            "contrast_enabled" to contrastEnabled
        )
    )
}
