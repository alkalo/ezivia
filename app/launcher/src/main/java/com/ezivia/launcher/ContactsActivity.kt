package com.ezivia.launcher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
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
import com.ezivia.launcher.ContactWizardActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ContactsActivity : BaseActivity() {

    private lateinit var binding: ActivityContactsBinding
    private lateinit var contactsAdapter: FavoriteContactsAdapter
    private lateinit var telephonyController: NativeTelephonyController
    private lateinit var whatsAppLauncher: WhatsAppLauncher
    private lateinit var conversationCoordinator: ConversationCoordinator
    private val favoriteContactsSynchronizer by lazy { FavoriteContactsSynchronizer(contentResolver) }
    private var contactsJob: Job? = null
    private var pendingCallNumber: String? = null
    private var pendingVideoCallAction: (() -> Unit)? = null

    private val contactWizardLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val name = result.data?.getStringExtra(ContactWizardActivity.EXTRA_CONTACT_NAME)
                val toastText = name?.let {
                    getString(R.string.contact_wizard_result_toast, it)
                } ?: getString(R.string.contact_wizard_result_generic)
                showSuccessFeedback(toastText)
            }
        }

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

    private val videoCallPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val action = pendingVideoCallAction
            pendingVideoCallAction = null
            if (granted) {
                action?.invoke()
            } else {
                showErrorFeedback(R.string.quick_action_contacts_permission_needed)
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
            onEditClick = ::onEditClicked,
        )

        binding.contactsList.apply {
            adapter = contactsAdapter
            layoutManager = LinearLayoutManager(this@ContactsActivity)
            itemAnimator = ScaleInItemAnimator()
        }

        binding.addContactButton.apply {
            applyPressScaleEffect()
            setOnClickListener {
                contactWizardLauncher.launch(Intent(this@ContactsActivity, ContactWizardActivity::class.java))
            }
        }

        binding.manageFavoritesButton.apply {
            applyPressScaleEffect()
            setOnClickListener {
                runCatching {
                    startActivity(Intent(CONTACTS_FAVORITES_ACTION))
                }.recoverCatching {
                    startActivity(Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_APP_CONTACTS) })
                }.onFailure {
                    showErrorFeedback(R.string.telephony_not_available)
                }
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
        if (hasContactsPermission()) {
            startContactsSync()
        } else {
            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
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
        val result = whatsAppLauncher.startFavoriteVideoCall(contact)
        handleVideoCallResult(result)
    }

    private fun onEditClicked(contact: FavoriteContact) {
        val intent = Intent(this, ContactWizardActivity::class.java).apply {
            putExtra(ContactWizardActivity.EXTRA_CONTACT_ID, contact.id)
            putExtra(ContactWizardActivity.EXTRA_CONTACT_NAME, contact.displayName)
            putExtra(ContactWizardActivity.EXTRA_CONTACT_PHONE, contact.phoneNumber)
            putExtra(ContactWizardActivity.EXTRA_USE_WHATSAPP, true)
        }
        contactWizardLauncher.launch(intent)
    }

    private fun handleVideoCallResult(result: WhatsAppLauncher.VideoCallResult) {
        when (result) {
            WhatsAppLauncher.VideoCallResult.Success -> Unit
            WhatsAppLauncher.VideoCallResult.InvalidNumber -> showErrorFeedback(R.string.quick_action_invalid_phone_number)
            WhatsAppLauncher.VideoCallResult.VideoCallEntryMissing -> showErrorFeedback(R.string.quick_action_whatsapp_video_unavailable)
            WhatsAppLauncher.VideoCallResult.ContactsPermissionMissing -> showErrorFeedback(R.string.quick_action_contacts_permission_needed)
            WhatsAppLauncher.VideoCallResult.LaunchError -> showErrorFeedback(R.string.quick_action_whatsapp_launch_error)
            WhatsAppLauncher.VideoCallResult.WhatsappNotInstalled -> showErrorFeedback(R.string.quick_action_no_whatsapp)
        }
    }

    private fun showTelephonyUnavailableToast() {
        showErrorFeedback(R.string.telephony_not_available)
    }

    companion object {
        private const val CONTACTS_FAVORITES_ACTION = "com.android.contacts.action.LIST_STARRED"
    }
}
