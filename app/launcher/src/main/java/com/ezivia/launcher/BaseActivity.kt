package com.ezivia.launcher

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ezivia.launcher.databinding.ViewFeedbackToastBinding

open class BaseActivity : AppCompatActivity() {

    private val vibrator: Vibrator? by lazy { ContextCompat.getSystemService(this, Vibrator::class.java) }
    private val toneGenerator: ToneGenerator by lazy { ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80) }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun startActivity(intent: Intent?, options: Bundle?) {
        super.startActivity(intent, options)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    protected fun showSuccessFeedback(@StringRes messageRes: Int) {
        showFeedback(getString(messageRes), isError = false)
    }

    protected fun showErrorFeedback(@StringRes messageRes: Int) {
        showFeedback(getString(messageRes), isError = true)
    }

    protected fun showSuccessFeedback(message: CharSequence) {
        showFeedback(message, isError = false)
    }

    protected fun showErrorFeedback(message: CharSequence) {
        showFeedback(message, isError = true)
    }

    private fun showFeedback(message: CharSequence, isError: Boolean) {
        vibrate(isError)
        playTone(isError)
        showLargeToast(message, isError)
    }

    private fun vibrate(isError: Boolean) {
        val effect = if (isError) {
            VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 120), -1)
        } else {
            VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE)
        }
        vibrator?.vibrate(effect)
    }

    private fun playTone(isError: Boolean) {
        val tone = if (isError) {
            ToneGenerator.TONE_PROP_NACK
        } else {
            ToneGenerator.TONE_PROP_ACK
        }
        toneGenerator.startTone(tone, 120)
    }

    private fun showLargeToast(message: CharSequence, isError: Boolean) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as android.view.LayoutInflater
        val binding = ViewFeedbackToastBinding.inflate(inflater)
        binding.feedbackText.text = message
        val backgroundRes = if (isError) R.drawable.bg_feedback_toast_error else R.drawable.bg_feedback_toast_success
        binding.feedbackContainer.setBackgroundResource(backgroundRes)

        Toast(this).apply {
            duration = Toast.LENGTH_SHORT
            view = binding.root
        }.show()
    }
}
