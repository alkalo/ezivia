package com.ezivia.launcher

import androidx.lifecycle.ViewModel
import com.ezivia.communication.ConversationCoordinator

class MainViewModel : ViewModel() {
    private var coordinator: ConversationCoordinator? = null

    fun initialize(conversationCoordinator: ConversationCoordinator) {
        if (coordinator == null) {
            coordinator = conversationCoordinator
        }
    }
}
