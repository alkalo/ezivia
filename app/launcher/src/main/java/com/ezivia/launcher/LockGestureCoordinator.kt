package com.ezivia.launcher

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes

private const val LOCK_GESTURE_GRACE_PERIOD_MS = 1500L

/**
 * Detecta cuando el usuario mantiene presionado el gesto de hardware dedicado
 * para autorizar acciones protegidas como salir del modo o abrir ajustes.
 */
class LockGestureCoordinator(
    private val context: Context,
    private val holdView: View,
    private val statusTextView: TextView
) {

    private val handler = Handler(Looper.getMainLooper())
    private var activeUntil: Long = 0L
    private var currentState: State = State.Ready

    private val releaseRunnable = Runnable { setState(State.Idle) }

    init {
        holdView.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> startHold()
                MotionEvent.ACTION_POINTER_DOWN -> startHold()
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> endHold()
            }
            true
        }
        setState(State.Idle)
    }

    fun canTriggerProtectedAction(): Boolean {
        val now = SystemClock.elapsedRealtime()
        val engaged = currentState != State.Idle && (activeUntil == Long.MAX_VALUE || now <= activeUntil)
        if (!engaged) {
            setState(State.Idle)
        }
        return engaged
    }

    fun consumeGesture() {
        setState(State.Idle)
    }

    fun startHold() {
        handler.removeCallbacks(releaseRunnable)
        activeUntil = Long.MAX_VALUE
        setState(State.Holding)
    }

    fun endHold() {
        if (currentState == State.Holding) {
            activeUntil = SystemClock.elapsedRealtime() + LOCK_GESTURE_GRACE_PERIOD_MS
            setState(State.Ready)
            scheduleRelease()
        }
    }

    private fun scheduleRelease() {
        handler.removeCallbacks(releaseRunnable)
        val delay = (activeUntil - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
        handler.postDelayed(releaseRunnable, delay)
    }

    private fun setState(state: State) {
        currentState = state
        when (state) {
            State.Idle -> {
                handler.removeCallbacks(releaseRunnable)
                activeUntil = 0L
                updateStatus(R.string.home_lock_hold_status_idle, isPressed = false)
            }
            State.Holding -> updateStatus(R.string.home_lock_hold_status_holding, isPressed = true)
            State.Ready -> updateStatus(R.string.home_lock_hold_status_ready, isPressed = false)
        }
    }

    private fun updateStatus(@StringRes textRes: Int, isPressed: Boolean) {
        statusTextView.text = context.getString(textRes)
        holdView.isPressed = isPressed
    }

    private enum class State { Idle, Holding, Ready }
}
