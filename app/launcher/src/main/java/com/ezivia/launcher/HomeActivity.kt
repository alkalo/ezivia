package com.ezivia.launcher

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezivia.communication.ConversationCoordinator
import com.ezivia.communication.contacts.FavoriteContact
import com.ezivia.communication.contacts.FavoriteContactsSynchronizer
import com.ezivia.communication.telephony.NativeTelephonyController
import com.ezivia.communication.whatsapp.WhatsAppLauncher
import com.ezivia.launcher.databinding.ActivityHomeBinding
import com.ezivia.settings.RestrictedSettingsActivity
import com.ezivia.utilities.caregiver.CaregiverPreferences
import com.ezivia.utilities.camera.CameraCaptureRequest
import com.ezivia.utilities.camera.SimpleCameraCoordinator
import com.ezivia.utilities.reminders.ReminderRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Home screen for Ezivia that exposes the key actions older adults need in a
 * simplified layout.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var contactsAdapter: FavoriteContactsAdapter
    private lateinit var quickActionsAdapter: HomeQuickActionsAdapter
    private lateinit var telephonyController: NativeTelephonyController
    private lateinit var protectionManager: ProtectionManager
    private lateinit var onboardingPreferences: LauncherOnboardingPreferences
    private lateinit var whatsAppLauncher: WhatsAppLauncher
    private lateinit var cameraCoordinator: SimpleCameraCoordinator
    private lateinit var reminderRepository: ReminderRepository
    private lateinit var caregiverPreferences: CaregiverPreferences
    private lateinit var conversationCoordinator: ConversationCoordinator
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private lateinit var lockGestureCoordinator: LockGestureCoordinator
    private val favoriteContactsSynchronizer by lazy { FavoriteContactsSynchronizer(contentResolver) }
    private var contactsJob: Job? = null
    private var pendingCallNumber: String? = null
    private var pendingCameraRequest: CameraCaptureRequest? = null

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

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val request = pendingCameraRequest ?: return@registerForActivityResult
            val saved = cameraCoordinator.recordCameraResult(result.resultCode == Activity.RESULT_OK, request)
            pendingCameraRequest = null
            if (saved != null) {
                Toast.makeText(this, R.string.quick_action_camera_saved, Toast.LENGTH_SHORT).show()
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                cameraCoordinator.recordGalleryResult(result.data)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onboardingPreferences = LauncherOnboardingPreferences(this)

        if (!DefaultLauncherHelper.isDefaultLauncher(this)) {
            onboardingPreferences.setDefaultLauncherCompleted(false)
            startActivity(Intent(this, DefaultLauncherSetupActivity::class.java))
            finish()
            return
        }

        onboardingPreferences.setDefaultLauncherCompleted(true)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        telephonyController = NativeTelephonyController(this)
        conversationCoordinator = ConversationCoordinator().also { it.prepareConversationTemplates() }
        contactsAdapter = FavoriteContactsAdapter(
            onCallClick = ::onCallClicked,
            onMessageClick = ::onMessageClicked,
            onVideoCallClick = ::onVideoCallClicked,
        )
        quickActionsAdapter = HomeQuickActionsAdapter(::handleQuickAction)
        protectionManager = ProtectionManager(this)
        whatsAppLauncher = WhatsAppLauncher(this)
        cameraCoordinator = SimpleCameraCoordinator(this)
        reminderRepository = ReminderRepository(this)
        caregiverPreferences = CaregiverPreferences(this)
        lockGestureCoordinator = LockGestureCoordinator(
            context = this,
            holdView = binding.lockGestureHoldView,
            statusTextView = binding.lockGestureStatus
        )

        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                protectionManager.requireUnlocked {
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        binding.favoriteContactsList.apply {
            adapter = contactsAdapter
            layoutManager = LinearLayoutManager(this@HomeActivity)
        }

        binding.quickActionsList.apply {
            adapter = quickActionsAdapter
            layoutManager = GridLayoutManager(this@HomeActivity, 2)
        }

        quickActionsAdapter.submitList(HomeQuickActions.defaultActions())

        binding.settingsButton.setOnClickListener {
            if (!lockGestureCoordinator.canTriggerProtectedAction()) {
                Toast.makeText(this, R.string.home_lock_hold_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            protectionManager.requireUnlocked {
                lockGestureCoordinator.consumeGesture()
                startActivity(Intent(this, RestrictedSettingsActivity::class.java))
            }
        }

        binding.exitButton.setOnClickListener {
            if (!lockGestureCoordinator.canTriggerProtectedAction()) {
                Toast.makeText(this, R.string.home_lock_hold_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            protectionManager.requireUnlocked {
                lockGestureCoordinator.consumeGesture()
                finishAffinity()
            }
        }

        binding.bottomNavigation.selectedItemId = R.id.navigation_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_contacts -> {
                    startActivity(Intent(this, ContactsActivity::class.java))
                    binding.bottomNavigation.selectedItemId = R.id.navigation_home
                    true
                }
                R.id.navigation_health -> {
                    startActivity(Intent(this, RemindersOverviewActivity::class.java))
                    binding.bottomNavigation.selectedItemId = R.id.navigation_home
                    true
                }
                R.id.navigation_sos -> {
                    startActivity(Intent(this, SosActivity::class.java))
                    binding.bottomNavigation.selectedItemId = R.id.navigation_home
                    true
                }
                else -> false
            }
        }

        ensureContactsPermission()

        maybeShowLockGestureTutorial()
    }

    override fun onResume() {
        super.onResume()

        if (!DefaultLauncherHelper.isDefaultLauncher(this)) {
            onboardingPreferences.setDefaultLauncherCompleted(false)
            startActivity(Intent(this, DefaultLauncherSetupActivity::class.java))
            finish()
            return
        } else {
            onboardingPreferences.setDefaultLauncherCompleted(true)
        }

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
        if (::onBackPressedCallback.isInitialized) {
            onBackPressedCallback.remove()
        }
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

    private fun handleQuickAction(action: HomeQuickAction) {
        when (action.type) {
            HomeQuickActionType.CALL -> startDialer()
            HomeQuickActionType.VIDEO_CALL -> startVideoQuickAction()
            HomeQuickActionType.MESSAGE -> startMessageQuickAction()
            HomeQuickActionType.PHOTOS -> startPhotosQuickAction()
            HomeQuickActionType.REMINDERS -> showRemindersSummary()
            HomeQuickActionType.SOS -> startSosQuickAction()
        }
    }

    private fun startDialer() {
        val dialIntent = Intent(Intent.ACTION_DIAL)
        runCatching { startActivity(dialIntent) }
            .onFailure { showTelephonyUnavailableToast() }
    }

    private fun startVideoQuickAction() {
        val favorites = contactsAdapter.currentList
        if (favorites.isEmpty()) {
            Toast.makeText(this, R.string.quick_action_no_contacts, Toast.LENGTH_SHORT).show()
            return
        }
        showContactPicker(R.string.quick_action_video_choose_contact, favorites) { contact ->
            val handled = whatsAppLauncher.startFavoriteVideoCall(contact)
            if (!handled) {
                Toast.makeText(this, R.string.quick_action_no_whatsapp, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startMessageQuickAction() {
        val favorites = contactsAdapter.currentList
        if (favorites.isEmpty()) {
            Toast.makeText(this, R.string.quick_action_no_contacts, Toast.LENGTH_SHORT).show()
            return
        }
        showContactPicker(R.string.quick_action_message_choose_contact, favorites) { contact ->
            showMessageTemplateDialog(contact)
        }
    }

    private fun startPhotosQuickAction() {
        AlertDialog.Builder(this)
            .setTitle(R.string.quick_action_photos_title)
            .setItems(
                arrayOf(
                    getString(R.string.quick_action_take_photo),
                    getString(R.string.quick_action_open_gallery)
                )
            ) { dialog, which ->
                when (which) {
                    0 -> launchCameraCapture()
                    1 -> launchGalleryPicker()
                }
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun launchCameraCapture() {
        val request = cameraCoordinator.createCameraCaptureRequest()
        if (request == null) {
            Toast.makeText(this, R.string.quick_action_camera_error, Toast.LENGTH_SHORT).show()
            return
        }
        pendingCameraRequest = request
        cameraLauncher.launch(request.intent)
    }

    private fun launchGalleryPicker() {
        val intent = cameraCoordinator.createGalleryPickerIntent()
        try {
            galleryLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.quick_action_gallery_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRemindersSummary() {
        val now = LocalDateTime.now()
        val upcoming = reminderRepository.getUpcomingReminders(now)
        if (upcoming.isEmpty()) {
            Toast.makeText(this, R.string.quick_action_reminders_empty, Toast.LENGTH_SHORT).show()
            return
        }
        val formatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM HH:mm", Locale.getDefault())
        val message = buildString {
            upcoming.forEachIndexed { index, reminder ->
                append("• ")
                append(reminder.title)
                append(" – ")
                append(reminder.dateTime.format(formatter))
                if (index < upcoming.lastIndex) {
                    append('\n')
                }
            }
            if (reminderRepository.getReminders().size > upcoming.size) {
                append('\n')
                append(getString(R.string.quick_action_reminders_summary_more))
            }
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.quick_action_reminders_summary_title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(R.string.reminders_add) { _, _ ->
                startActivity(Intent(this, RemindersOverviewActivity::class.java))
            }
            .show()
    }

    private fun startSosQuickAction() {
        AlertDialog.Builder(this)
            .setTitle(R.string.quick_action_sos_title)
            .setMessage(R.string.quick_action_sos_confirmation)
            .setPositiveButton(R.string.quick_action_sos_call_number) { _, _ ->
                triggerEmergencyCall()
                notifyCaregiver()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showContactPicker(
        titleRes: Int,
        favorites: List<FavoriteContact>,
        onSelected: (FavoriteContact) -> Unit
    ) {
        val names = favorites.map { it.displayName }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(titleRes)
            .setItems(names) { dialog, which ->
                val contact = favorites.getOrNull(which)
                if (contact != null) {
                    onSelected(contact)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun showMessageTemplateDialog(contact: FavoriteContact) {
        val templates = conversationCoordinator.prepareConversationTemplates()
        val templateArray = templates.toTypedArray()
        AlertDialog.Builder(this)
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
        showMessageTemplateDialog(contact)
    }

    private fun onVideoCallClicked(contact: FavoriteContact) {
        val handled = whatsAppLauncher.startFavoriteVideoCall(contact)
        if (!handled) {
            Toast.makeText(this, R.string.quick_action_no_whatsapp, Toast.LENGTH_SHORT).show()
        }
    }

    private fun triggerEmergencyCall() {
        val emergencyNumber = "112"
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
        val handled = if (hasPermission) {
            telephonyController.startCall(emergencyNumber)
        } else {
            telephonyController.startDial(emergencyNumber)
        }
        if (!handled) {
            Toast.makeText(this, R.string.quick_action_sos_call_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun notifyCaregiver() {
        val caregiver = caregiverPreferences.loadCaregivers().firstOrNull()
        if (caregiver == null) {
            Toast.makeText(this, R.string.quick_action_sos_no_caregiver, Toast.LENGTH_SHORT).show()
            return
        }
        if (telephonyController.startSms(caregiver.phoneNumber, getString(R.string.sos_message_body))) {
            Toast.makeText(this, R.string.quick_action_sos_sms_sent, Toast.LENGTH_SHORT).show()
        } else {
            showTelephonyUnavailableToast()
        }
    }

    private fun showTelephonyUnavailableToast() {
        Toast.makeText(this, R.string.telephony_not_available, Toast.LENGTH_SHORT).show()
    }

    private fun maybeShowLockGestureTutorial() {
        if (!onboardingPreferences.shouldShowLockGestureTutorial()) {
            return
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.home_lock_tutorial_title)
            .setMessage(R.string.home_lock_tutorial_message)
            .setPositiveButton(R.string.home_lock_tutorial_button) { dialog, _ ->
                onboardingPreferences.markLockGestureTutorialShown()
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
}
