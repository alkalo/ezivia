package com.ezivia.launcher

import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ezivia.launcher.databinding.DialogPinPromptBinding
import com.ezivia.utilities.security.PinStorage

/**
 * Coordinates the protection PIN that keeps Ezivia running in modo simplificado.
 * It exposes helper functions so activities can request a verification before
 * allowing the user to abandon the launcher or abrir ajustes restringidos.
 */
class ProtectionManager(activity: AppCompatActivity) {

    private val appCompatActivity = activity
    private val pinStorage = PinStorage(activity)

    fun isProtectionActive(): Boolean = pinStorage.isPinConfigured()

    fun requireUnlocked(onUnlocked: () -> Unit) {
        if (!isProtectionActive()) {
            onUnlocked()
            return
        }
        showPinDialog(onUnlocked)
    }

    private fun showPinDialog(onUnlocked: () -> Unit) {
        val binding = DialogPinPromptBinding.inflate(LayoutInflater.from(appCompatActivity))
        val dialog = AlertDialog.Builder(appCompatActivity)
            .setTitle(R.string.protection_enter_pin_title)
            .setView(binding.root)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.protection_confirm_button, null)
            .create()

        dialog.setOnShowListener {
            val confirmButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            confirmButton.setOnClickListener {
                val pin = binding.pinTextField.editText?.text?.toString()?.trim().orEmpty()
                if (pinStorage.verifyPin(pin)) {
                    binding.pinTextField.error = null
                    dialog.dismiss()
                    onUnlocked()
                } else {
                    binding.pinTextField.error = appCompatActivity.getString(R.string.protection_invalid_pin)
                }
            }
        }

        dialog.show()
    }
}
