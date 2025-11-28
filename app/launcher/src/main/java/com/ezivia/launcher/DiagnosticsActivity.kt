package com.ezivia.launcher

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ezivia.communication.DiagnosticsLog
import com.ezivia.launcher.databinding.ActivityDiagnosticsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Panel temporal para revisar y compartir los eventos de comunicación.
 */
class DiagnosticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiagnosticsBinding
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiagnosticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        binding.refreshButton.setOnClickListener { renderLogs() }
        binding.shareButton.setOnClickListener { shareLogs() }
        binding.closeButton.setOnClickListener { finish() }

        renderLogs()
    }

    private fun renderLogs() {
        val entries = DiagnosticsLog.snapshot()
        if (entries.isEmpty()) {
            binding.logsContent.text = getString(R.string.diagnostics_empty_state)
            return
        }

        val formattedLogs = entries.joinToString(separator = "\n") { entry ->
            val timestamp = dateFormatter.format(Date(entry.timestampMillis))
            "$timestamp · [${entry.source}] ${entry.message}"
        }

        binding.logsContent.text = formattedLogs
    }

    private fun shareLogs() {
        val content = binding.logsContent.text.toString().ifBlank {
            getString(R.string.diagnostics_empty_state)
        }

        val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:98alkalo@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.diagnostics_email_subject))
            putExtra(Intent.EXTRA_TEXT, content)
        }

        try {
            startActivity(Intent.createChooser(mailIntent, getString(R.string.diagnostics_email_chooser_title)))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.diagnostics_email_unavailable, Toast.LENGTH_LONG).show()
        }
    }
}
