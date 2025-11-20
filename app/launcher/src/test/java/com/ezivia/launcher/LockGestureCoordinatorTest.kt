package com.ezivia.launcher

import android.content.Context
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import java.time.Duration
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowSystemClock

@RunWith(RobolectricTestRunner::class)
class LockGestureCoordinatorTest {

    private lateinit var context: Context
    private lateinit var holdView: android.view.View
    private lateinit var statusTextView: TextView
    private lateinit var coordinator: LockGestureCoordinator

    private var pressedState = false
    private var statusText: CharSequence? = null

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        holdView = mock {
            on { isPressed } doAnswer { pressedState }
            on { setPressed(org.mockito.kotlin.any()) } doAnswer {
                pressedState = it.getArgument(0)
                null
            }
        }
        statusTextView = mock {
            on { text } doAnswer { statusText }
            on { setText(org.mockito.kotlin.any<CharSequence>()) } doAnswer {
                statusText = it.getArgument(0)
                null
            }
        }

        coordinator = LockGestureCoordinator(context, holdView, statusTextView)
    }

    @Test
    fun startHold_marksPressedAndUpdatesStatus() {
        coordinator.startHold()

        assertThat(pressedState).isTrue()
        assertThat(statusText?.toString())
            .isEqualTo(context.getString(R.string.home_lock_hold_status_holding))
        assertThat(coordinator.canTriggerProtectedAction()).isTrue()
    }

    @Test
    fun endHold_allowsGracePeriodBeforeExpiry() {
        coordinator.startHold()
        ShadowSystemClock.setUptimeMillis(1000)

        coordinator.endHold()

        assertThat(pressedState).isFalse()
        assertThat(statusText?.toString())
            .isEqualTo(context.getString(R.string.home_lock_hold_status_ready))
        assertThat(coordinator.canTriggerProtectedAction()).isTrue()

        Shadows.shadowOf(android.os.Looper.getMainLooper())
            .idleFor(Duration.ofMillis(1500))

        assertThat(coordinator.canTriggerProtectedAction()).isFalse()
        assertThat(pressedState).isFalse()
        assertThat(statusText?.toString())
            .isEqualTo(context.getString(R.string.home_lock_hold_status_idle))
    }

    @Test
    fun consumeGesture_resetsStateImmediately() {
        coordinator.startHold()

        coordinator.consumeGesture()

        assertThat(coordinator.canTriggerProtectedAction()).isFalse()
        assertThat(pressedState).isFalse()
        assertThat(statusText?.toString())
            .isEqualTo(context.getString(R.string.home_lock_hold_status_idle))
    }

    @Test
    fun protectedActionRejectedAfterGracePeriod() {
        coordinator.startHold()
        coordinator.endHold()

        assertThat(coordinator.canTriggerProtectedAction()).isTrue()

        Shadows.shadowOf(android.os.Looper.getMainLooper())
            .idleFor(Duration.ofMillis(1490))

        assertThat(coordinator.canTriggerProtectedAction()).isTrue()

        Shadows.shadowOf(android.os.Looper.getMainLooper())
            .idleFor(Duration.ofMillis(20))

        assertThat(coordinator.canTriggerProtectedAction()).isFalse()
    }
}
