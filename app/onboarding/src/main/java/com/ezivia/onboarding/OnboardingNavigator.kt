package com.ezivia.onboarding

import android.content.Context
import android.content.Intent

class OnboardingNavigator(private val context: Context) {
    fun launchOnboarding() {
        val intent = Intent(context, OnboardingActivity::class.java)
        context.startActivity(intent)
    }
}
