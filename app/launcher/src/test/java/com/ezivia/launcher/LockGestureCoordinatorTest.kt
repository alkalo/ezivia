package com.ezivia.launcher

import android.content.Context
import android.os.Looper
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import java.util.concurrent.TimeUnit
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class LockGestureCoordinatorTest {

    private lateinit var context: Context
    private lateinit var holdView: LinearLayout
    private lateinit var statusTextView: TextView
    private lateinit var coordinator: LockGestureCoordinator

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        holdView = LinearLayout(context)
        statusTextView = TextView(context)
        coordinator = LockGestureCoordinator(context, holdView, statusTextView)
    }

    @Test
    fun hardwareHoldTransitionsThroughStates() {
        assertThat(holdView.isPressed).isFalse()
        assertThat(statusTextView.text.toString())
            .isEqualTo(context.getString(R.string.home_lock_hold_status_idle))

        coordinator.startHold()

        assertThat(holdView.isPressed).isTrue()
        assertThat(statusTextView.text.toString())
            .isEqualTo(context.getString(R.string.home_lock_hold_status_holding))
        assertThat(coordinator.canTriggerProtectedAction()).isTrue()

        coordinator.endHold()

        assertThat(holdView.isPressed).isFalse()
        assertThat(statusTextView.text.toString())
            .isEqualTo(context.getString(R.string.home_lock_hold_status_ready))
        assertThat(coordinator.canTriggerProtectedAction()).isTrue()

        Shadows.shadowOf(Looper.getMainLooper()).idleFor(2, TimeUnit.SECONDS)

        assertThat(coordinator.canTriggerProtectedAction()).isFalse()
        assertThat(holdView.isPressed).isFalse()
        assertThat(statusTextView.text.toString())
            .isEqualTo(context.getString(R.string.home_lock_hold_status_idle))
    }

    @Test
    fun consumeGestureResetsStateImmediately() {
        coordinator.startHold()
        coordinator.consumeGesture()

        assertThat(coordinator.canTriggerProtectedAction()).isFalse()
        assertThat(holdView.isPressed).isFalse()
        assertThat(statusTextView.text.toString())
            .isEqualTo(context.getString(R.string.home_lock_hold_status_idle))
    }
}
