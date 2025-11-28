package com.ezivia.launcher

import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

fun View.applyPressScaleEffect(scale: Float = 1.04f, duration: Long = 170L) {
    val interpolator = OvershootInterpolator()
    setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                ViewCompat.animate(v)
                    .scaleX(scale)
                    .scaleY(scale)
                    .setDuration(duration)
                    .setInterpolator(interpolator)
                    .start()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE -> {
                ViewCompat.animate(v)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(duration)
                    .setInterpolator(interpolator)
                    .start()
            }
        }
        false
    }
}

class ScaleInItemAnimator : DefaultItemAnimator() {

    init {
        addDuration = 180
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder?): Boolean {
        holder ?: return false
        dispatchAddStarting(holder)
        holder.itemView.apply {
            scaleX = 0.94f
            scaleY = 0.94f
            alpha = 0f
            animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(addDuration)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .withEndAction { dispatchAddFinished(holder) }
                .start()
        }
        return true
    }

    override fun endAnimation(item: RecyclerView.ViewHolder) {
        item.itemView.animate().cancel()
        item.itemView.scaleX = 1f
        item.itemView.scaleY = 1f
        item.itemView.alpha = 1f
        super.endAnimation(item)
    }
}
