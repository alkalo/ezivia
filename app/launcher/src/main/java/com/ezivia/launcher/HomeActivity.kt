package com.ezivia.launcher

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import com.ezivia.launcher.R
import com.ezivia.launcher.databinding.ActivityHomeBinding
import com.ezivia.settings.RestrictedSettingsActivity
import com.ezivia.utilities.caregiver.CaregiverPreferences
import com.ezivia.utilities.camera.CameraCaptureRequest
import com.ezivia.utilities.camera.SimpleCameraCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Home screen for Ezivia that exposes the key actions older adults need in a
 * simplified layout.
 */
class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var contactsAdapter: FavoriteContactsAdapter
    private lateinit var quickActionsAdapter: HomeQuickActionsAdapter
    private lateinit var telephonyController: NativeTelephonyController
    private lateinit var onboardingPreferences: LauncherOnboardingPreferences
    private lateinit var whatsAppLauncher: WhatsAppLauncher
    private lateinit var cameraCoordinator: SimpleCameraCoordinator
    private lateinit var caregiverPreferences: CaregiverPreferences
    private lateinit var conversationCoordinator: ConversationCoordinator
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private val favoriteContactsSynchronizer by lazy { FavoriteContactsSynchronizer(contentResolver) }
    private var contactsJob: Job? = null
    private var pendingCallNumber: String? = null
    private var pendingCameraRequest: CameraCaptureRequest? = null
    private var pendingVideoCallAction: (() -> Unit)? = null
    private val fadeScaleIn by lazy { AnimationUtils.loadAnimation(this, R.anim.fade_scale_in) }
    private var isVolumeUpPressed: Boolean = false

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

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val request = pendingCameraRequest ?: return@registerForActivityResult
            val saved = cameraCoordinator.recordCameraResult(result.resultCode == Activity.RESULT_OK, request)
            pendingCameraRequest = null
            if (saved != null) {
                showSuccessFeedback(R.string.quick_action_camera_saved)
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                cameraCoordinator.recordGalleryResult(result.data)
            }
        }

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

    private val settingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode != Activity.RESULT_OK) {
                return@registerForActivityResult
            }
            val shouldExit =
                result.data?.getBooleanExtra(RestrictedSettingsActivity.EXTRA_REQUEST_EXIT, false) == true
            if (shouldExit) {
                startExitFlow()
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
            onEditClick = ::onEditClicked,
        )
        quickActionsAdapter = HomeQuickActionsAdapter(::handleQuickAction)
        whatsAppLauncher = WhatsAppLauncher(this)
        cameraCoordinator = SimpleCameraCoordinator(this)
        caregiverPreferences = CaregiverPreferences(this)
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmation()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        binding.favoriteContactsList.apply {
            adapter = contactsAdapter
            layoutManager = LinearLayoutManager(this@HomeActivity)
            layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_fade_scale_in)
            itemAnimator = ScaleInItemAnimator().apply {
                supportsChangeAnimations = false
            }
            setHasFixedSize(false)
        }

        binding.quickActionsList.apply {
            adapter = quickActionsAdapter
            layoutManager = LinearLayoutManager(this@HomeActivity)
            layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_fade_scale_in)
            itemAnimator = ScaleInItemAnimator()
            setHasFixedSize(true)
        }

        quickActionsAdapter.submitList(HomeQuickActions.defaultActions())

        binding.primaryCallButton.apply {
            applyPressScaleEffect()
            setOnClickListener { startDialer() }
        }
        binding.primaryVideoButton.apply {
            applyPressScaleEffect()
            setOnClickListener { startVideoQuickAction() }
        }
        binding.sosFab.apply {
            applyPressScaleEffect()
            setOnClickListener { startSosQuickAction() }
        }

        binding.bottomNavigation.selectedItemId = R.id.navigation_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_settings -> {
                    handleProtectedSettingsAccess()
                    binding.bottomNavigation.selectedItemId = R.id.navigation_home
                    true
                }
                else -> false
            }
        }

        binding.quickActionsList.scheduleLayoutAnimation()
        animateEntryViews()

        ensureContactsPermission()
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            isVolumeUpPressed = true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            isVolumeUpPressed = false
        }
        return super.onKeyUp(keyCode, event)
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
        if (hasContactsPermission()) {
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
        contactsAdapter.submitList(contacts) {
            binding.favoriteContactsList.scheduleLayoutAnimation()
        }
        binding.emptyContactsView.isVisible = contacts.isEmpty()
        if (contacts.isEmpty()) {
            binding.emptyContactsView.text = getString(R.string.home_contacts_empty)
        }
    }

    private fun showPermissionRequiredMessage() {
        contactsJob?.cancel()
        contactsJob = null
        contactsAdapter.submitList(emptyList()) {
            binding.favoriteContactsList.scheduleLayoutAnimation()
        }
        binding.emptyContactsView.isVisible = true
        binding.emptyContactsView.text = getString(R.string.home_contacts_permission_needed)
    }

    private fun handleQuickAction(action: HomeQuickAction) {
        when (action.type) {
            HomeQuickActionType.CALL -> startDialer()
            HomeQuickActionType.VIDEO_CALL -> startVideoQuickAction()
            HomeQuickActionType.MESSAGE -> startMessageQuickAction()
            HomeQuickActionType.PHOTOS -> startPhotosQuickAction()
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
            showErrorFeedback(R.string.quick_action_no_contacts)
            return
        }
        showContactPicker(R.string.quick_action_video_choose_contact, favorites) { contact ->
            val result = whatsAppLauncher.startFavoriteVideoCall(contact)
            handleVideoCallResult(result)
        }
    }

    private fun startMessageQuickAction() {
        val favorites = contactsAdapter.currentList
        if (favorites.isEmpty()) {
            showErrorFeedback(R.string.quick_action_no_contacts)
            return
        }
        showContactPicker(R.string.quick_action_message_choose_contact, favorites) { contact ->
            showMessageTemplateDialog(contact)
        }
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
            showErrorFeedback(R.string.quick_action_camera_error)
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
            showErrorFeedback(R.string.quick_action_gallery_unavailable)
        }
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

    private fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestContactsPermissionForVideoCall(onGranted: () -> Unit) {
        if (hasContactsPermission()) {
            onGranted()
        } else {
            pendingVideoCallAction = onGranted
            videoCallPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
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
        if (!hasContactsPermission()) {
            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            showErrorFeedback(R.string.quick_action_contacts_permission_needed)
            return
        }
        val result = whatsAppLauncher.startFavoriteVideoCall(contact)
        handleVideoCallResult(result)
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
            showErrorFeedback(R.string.quick_action_sos_call_failed)
        }
    }

    private fun notifyCaregiver() {
        val caregiver = caregiverPreferences.loadCaregivers().firstOrNull()
        if (caregiver == null) {
            showErrorFeedback(R.string.quick_action_sos_no_caregiver)
            return
        }
        if (telephonyController.startSms(caregiver.phoneNumber, getString(R.string.sos_message_body))) {
            showSuccessFeedback(R.string.quick_action_sos_sms_sent)
        } else {
            showTelephonyUnavailableToast()
        }
    }

    private fun showTelephonyUnavailableToast() {
            showErrorFeedback(R.string.telephony_not_available)
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

    private fun handleProtectedSettingsAccess() {
        if (isVolumeUpPressed) {
            openProtectedSettings()
        } else {
            showErrorFeedback(R.string.home_settings_volume_required)
        }
    }

    private fun openProtectedSettings() {
        AlertDialog.Builder(this)
            .setTitle(R.string.home_settings_confirmation_title)
            .setMessage(R.string.home_settings_confirmation_message)
            .setPositiveButton(R.string.home_settings_confirmation_confirm) { _, _ ->
                settingsLauncher.launch(Intent(this, RestrictedSettingsActivity::class.java))
                binding.bottomNavigation.selectedItemId = R.id.navigation_home
            }
            .setNeutralButton(R.string.home_settings_open_diagnostics) { _, _ ->
                startActivity(Intent(this, DiagnosticsActivity::class.java))
                binding.bottomNavigation.selectedItemId = R.id.navigation_home
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                binding.bottomNavigation.selectedItemId = R.id.navigation_home
            }
            .setOnCancelListener {
                binding.bottomNavigation.selectedItemId = R.id.navigation_home
            }
            .show()
    }

    private fun startExitFlow() {
        val intent = Intent(this, DefaultLauncherSetupActivity::class.java).apply {
            putExtra(DefaultLauncherSetupActivity.EXTRA_FORCE_RECONFIGURE, true)
        }
        startActivity(intent)
        finish()
    }

    private fun animateEntryViews() {
        listOf(
            binding.primaryCallButton,
            binding.primaryVideoButton,
            binding.sosFab,
            binding.bottomNavigation
        ).forEach { view ->
            view.startAnimation(fadeScaleIn)
        }
    }

    private fun showExitConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.home_exit_confirmation_title)
            .setMessage(R.string.home_exit_confirmation_message)
            .setPositiveButton(R.string.home_exit_confirmation_confirm) { _, _ -> finish() }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
