package com.ezivia.launcher

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HomeQuickActionsTest {

    @Test
    fun `default actions include all major shortcuts`() {
        val actions = HomeQuickActions.defaultActions()

        val types = actions.map { it.type }

        assertThat(types).containsExactly(
            HomeQuickActionType.CALL,
            HomeQuickActionType.VIDEO_CALL,
            HomeQuickActionType.MESSAGE,
            HomeQuickActionType.PHOTOS,
            HomeQuickActionType.SOS,
        ).inOrder()
    }
}
