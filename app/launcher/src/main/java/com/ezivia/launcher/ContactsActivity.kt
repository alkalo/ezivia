package com.ezivia.launcher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezivia.communication.ConversationCoordinator
import com.ezivia.communication.contacts.FavoriteContact
import com.ezivia.communication.contacts.FavoriteContactsSynchronizer
import com.ezivia.communication.telephony.NativeTelephonyController
import com.ezivia.communication.whatsapp.WhatsAppLauncher
import com.ezivia.launcher.R
import com.ezivia.launcher.databinding.ActivityContactsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ContactsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactsBinding
    private lateinit var contactsAdapter: FavoriteContactsAdapter
    private lateinit var telephonyController: NativeTelephonyController
    private lateinit var whatsAppLauncher: WhatsAppLauncher
    private lateinit var conversationCoordinator: ConversationCoordinator
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
            val number = pendingCallNumber ?: return@registerForActivityResult
            pendingCallNumber = null
            val handled = if (granted) {
                telephonyController.startCall(number)
            } else {
                telephonyController.startDial(number)
            }
            if (!handled) {
                showTelephonyUnavailableToast()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.contactsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.contacts_title)

        telephonyController = NativeTelephonyController(this)
        whatsAppLauncher = WhatsAppLauncher(this)
        conversationCoordinator = ConversationCoordinator()

        contactsAdapter = FavoriteContactsAdapter(
            onCallClick = ::onCallClicked,
            onMessageClick = ::onMessageClicked,
            onVideoCallClick = ::onVideoCallClicked,
        )

        binding.contactsList.apply {
            adapter = contactsAdapter
            layoutManager = LinearLayoutManager(this@ContactsActivity)
        }

        binding.manageFavoritesButton.setOnClickListener {
            runCatching {
                startActivity(Intent(CONTACTS_FAVORITES_ACTION))
            }.recoverCatching {
                startActivity(Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_APP_CONTACTS) })
            }.onFailure {
                Toast.makeText(this, R.string.telephony_not_available, Toast.LENGTH_SHORT).show()
            }
        }

        ensureContactsPermission()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
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
        contactsJob = lifecycleScope.launch(Dispatchers.Main) {
            favoriteContactsSynchronizer.favoriteContacts().collectLatest { contacts ->
                showContacts(contacts)
            }
        }
    }

    private fun showContacts(contacts: List<FavoriteContact>) {
        contactsAdapter.submitList(contacts)
        binding.contactsEmptyView.isVisible = contacts.isEmpty()
    }

    private fun showPermissionRequiredMessage() {
        contactsJob?.cancel()
        contactsAdapter.submitList(emptyList())
        binding.contactsEmptyView.isVisible = true
        binding.contactsEmptyView.text = getString(R.string.contacts_permission_needed)
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
        val templates = conversationCoordinator.prepareConversationTemplates()
        val templateArray = templates.toTypedArray()
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.quick_action_message_choose_template)
            .setItems(templateArray) { dialog, which ->
                val body = templates.getOrNull(which) ?: return@setItems
                if (!telephonyController.startSms(contact.phoneNumber, body)) {
                    showTelephonyUnavailableToast()
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun onVideoCallClicked(contact: FavoriteContact) {
        val handled = whatsAppLauncher.startFavoriteVideoCall(contact)
        if (!handled) {
            Toast.makeText(this, R.string.quick_action_no_whatsapp, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showTelephonyUnavailableToast() {
        Toast.makeText(this, R.string.telephony_not_available, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val CONTACTS_FAVORITES_ACTION = "com.android.contacts.action.LIST_STARRED"
    }
}
