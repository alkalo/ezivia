package com.ezivia.communication

import android.util.Log

/**
 * Coordinates simplified conversations, such as large-text messaging or quick replies,
 * ensuring communication remains accessible for older adults.
 */
class ConversationCoordinator {
    fun warmUp() {
        Log.d(TAG, "Conversation system ready")
    }

    fun prepareConversationTemplates(): List<String> {
        warmUp()
        return listOf(
            "Estoy bien, gracias.",
            "Llamaré más tarde.",
            "¿Puedes ayudarme con el teléfono?"
        )
    }

    companion object {
        private const val TAG = "ConversationCoordinator"
    }
}
