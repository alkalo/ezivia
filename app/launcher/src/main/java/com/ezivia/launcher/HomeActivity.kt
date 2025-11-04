package com.ezivia.launcher

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezivia.communication.contacts.FavoriteContact
import com.ezivia.communication.contacts.FavoriteContactsSynchronizer
import com.ezivia.communication.telephony.NativeTelephonyController
import com.ezivia.launcher.databinding.ActivityHomeBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Home screen for Ezivia that exposes the key actions older adults need in a
 * simplified layout.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var contactsAdapter: FavoriteContactsAdapter
    private lateinit var telephonyController: NativeTelephonyController
    private val favoriteContactsSynchronizer by lazy { FavoriteContactsSynchronizer(contentResolver) }
    private var contactsJob: Job? = null
    private var pendingCallNumber: String? = null

    private val contactsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startContactsSync()
            } else {
                showPermissionRequiredMessage()
            }
        }

    private val callPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val phoneNumber = pendingCallNumber ?: return@registerForActivityResult
            pendingCallNumber = null

            val handled = if (granted) {
                telephonyController.startCall(phoneNumber)
            } else {
                telephonyController.startDial(phoneNumber)
            }

            if (!handled) {
                showTelephonyUnavailableToast()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        telephonyController = NativeTelephonyController(this)
        contactsAdapter = FavoriteContactsAdapter(
            onCallClick = ::onCallClicked,
            onMessageClick = ::onMessageClicked,
        )

        binding.favoriteContactsList.apply {
            adapter = contactsAdapter
            layoutManager = LinearLayoutManager(this@HomeActivity)
        }

        binding.settingsButton.setOnClickListener {
            // Placeholder for future settings experience.
        }

        ensureContactsPermission()
    }

    override fun onResume() {
        super.onResume()
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission && contactsJob == null) {
            startContactsSync()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        contactsJob?.cancel()
        contactsJob = null
    }

    private fun ensureContactsPermission() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            startContactsSync()
        } else {
            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun startContactsSync() {
        contactsJob?.cancel()
        contactsJob = lifecycleScope.launch {
            favoriteContactsSynchronizer.favoriteContacts().collectLatest { contacts ->
                showContacts(contacts)
            }
        }
    }

    private fun showContacts(contacts: List<FavoriteContact>) {
        contactsAdapter.submitList(contacts)
        binding.emptyContactsView.isVisible = contacts.isEmpty()
        if (contacts.isEmpty()) {
            binding.emptyContactsView.text = getString(R.string.home_contacts_empty)
        }
    }

    private fun showPermissionRequiredMessage() {
        contactsJob?.cancel()
        contactsJob = null
        contactsAdapter.submitList(emptyList())
        binding.emptyContactsView.isVisible = true
        binding.emptyContactsView.text = getString(R.string.home_contacts_permission_needed)
    }

    private fun onCallClicked(contact: FavoriteContact) {
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            if (!telephonyController.startCall(contact.phoneNumber)) {
                showTelephonyUnavailableToast()
            }
        } else {
            pendingCallNumber = contact.phoneNumber
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    private fun onMessageClicked(contact: FavoriteContact) {
        if (!telephonyController.startSms(contact.phoneNumber)) {
            showTelephonyUnavailableToast()
        }
    }

    private fun showTelephonyUnavailableToast() {
        Toast.makeText(this, R.string.telephony_not_available, Toast.LENGTH_SHORT).show()
    }
}
