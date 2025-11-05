package com.ezivia.analytics

import android.util.Log

/**
 * Coordinates every registered [AnalyticsTracker] while guaranteeing that sensitive data
 * is anonymised and that a failure in one provider does not block the others.
 */
class AnalyticsManager(
    private val trackers: List<AnalyticsTracker>,
    private val anonymizer: DataAnonymizer = DataAnonymizer(),
    private val loggerTag: String = "AnalyticsManager"
) {

    fun track(event: AnalyticsEvent) {
        val payload = event.sanitize(anonymizer)
        trackers.forEach { tracker ->
            runCatching {
                tracker.track(payload)
            }.onFailure { throwable ->
                Log.w(loggerTag, "Unable to forward analytics event ${payload.name}", throwable)
            }
        }
    }

    fun track(events: Collection<AnalyticsEvent>) {
        events.forEach(::track)
    }
}

/**
 * Convenience factory to create an [AnalyticsManager] that does nothing.
 */
fun emptyAnalyticsManager(): AnalyticsManager = AnalyticsManager(trackers = emptyList())
