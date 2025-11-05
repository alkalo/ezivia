package com.ezivia.analytics

/**
 * Basic contract that every analytics provider must fulfil. Keeping the API narrow makes
 * it easy to provide a no-op implementation during instrumentation tests.
 */
fun interface AnalyticsTracker {
    fun track(payload: AnalyticsPayload)
}
