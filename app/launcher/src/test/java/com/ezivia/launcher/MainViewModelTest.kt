package com.ezivia.launcher

import com.ezivia.communication.ConversationCoordinator
import org.junit.Assert.assertSame
import org.junit.Test
import org.mockito.Mockito.mock

class MainViewModelTest {

    @Test
    fun `initialize keeps first coordinator instance`() {
        val viewModel = MainViewModel()
        val first = mock(ConversationCoordinator::class.java)
        val second = mock(ConversationCoordinator::class.java)

        viewModel.initialize(first)
        viewModel.initialize(second)

        val coordinatorField = MainViewModel::class.java.getDeclaredField("coordinator")
        coordinatorField.isAccessible = true
        val storedCoordinator = coordinatorField.get(viewModel)

        assertSame(first, storedCoordinator)
    }
}
