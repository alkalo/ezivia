package com.ezivia.launcher

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.speech.RecognizerIntent
import androidx.activity.result.ActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.ezivia.launcher.databinding.ActivityContactWizardBinding
import java.util.Locale

class ContactWizardActivity : BaseActivity() {

    private lateinit var binding: ActivityContactWizardBinding
    private var currentStep = 0
    private var voiceConfirmed = false
    private var contactPhotoUri: Uri? = null
    private var contactId: Long? = null

    private val photoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            contactPhotoUri = uri
            renderPhoto()
        }

    private val voicePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchVoicePrompt()
            } else {
                updateVoiceValidationMessage(
                    text = getString(R.string.contact_wizard_voice_permission_denied),
                    isPositive = false
                )
            }
        }

    private val voiceConfirmationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ::handleVoiceResult)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactWizardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.contactWizardToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.contact_wizard_title_add)

        setupInputs()
        setupButtons()
        populateFromIntent()
        updateStepUi()
    }

    override fun onSupportNavigateUp(): Boolean {
        if (currentStep > 0) {
            goToStep(currentStep - 1)
        } else {
            finish()
        }
        return true
    }

    private fun setupInputs() {
        binding.nameInput.doAfterTextChanged {
            updateValidationMessages()
            updateSummary()
            updateNavigationState()
        }

        binding.phoneInput.doAfterTextChanged {
            updateValidationMessages()
            updateSummary()
            updateNavigationState()
        }

        binding.whatsappSwitch.setOnCheckedChangeListener { _, _ ->
            updateSummary()
        }

        binding.voiceConfirmationSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.voiceConfirmationButton.isVisible = isChecked
            if (!isChecked) {
                voiceConfirmed = false
                updateVoiceValidationMessage(
                    text = getString(R.string.contact_wizard_voice_disabled),
                    isPositive = true,
                    neutral = true
                )
            } else {
                updateVoiceValidationMessage(
                    text = getString(R.string.contact_wizard_voice_prompt_helper),
                    isPositive = false,
                    neutral = true
                )
            }
            updateNavigationState()
        }
    }

    private fun setupButtons() {
        binding.backButton.applyPressScaleEffect()
        binding.backButton.setOnClickListener {
            if (currentStep == 0) {
                finish()
            } else {
                goToStep(currentStep - 1)
            }
        }

        binding.nextButton.applyPressScaleEffect()
        binding.nextButton.setOnClickListener {
            if (isCurrentStepValid()) {
                goToStep(currentStep + 1)
            } else {
                showErrorFeedback(R.string.contact_wizard_fix_errors)
            }
        }

        binding.choosePhotoButton.applyPressScaleEffect()
        binding.choosePhotoButton.setOnClickListener {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.voiceConfirmationButton.applyPressScaleEffect()
        binding.voiceConfirmationButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                launchVoicePrompt()
            } else {
                voicePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        binding.saveContactButton.applyPressScaleEffect()
        binding.saveContactButton.setOnClickListener {
            if (allStepsValid()) {
                sendResultAndOpenContacts()
            } else {
                showErrorFeedback(R.string.contact_wizard_fix_errors)
            }
        }
    }

    private fun populateFromIntent() {
        val extras = intent
        val maybeId = extras.getLongExtra(EXTRA_CONTACT_ID, -1L)
        contactId = maybeId.takeIf { it > 0 }

        binding.nameInput.setText(extras.getStringExtra(EXTRA_CONTACT_NAME).orEmpty())
        binding.phoneInput.setText(extras.getStringExtra(EXTRA_CONTACT_PHONE).orEmpty())
        binding.whatsappSwitch.isChecked = extras.getBoolean(EXTRA_USE_WHATSAPP, true)
        extras.getStringExtra(EXTRA_CONTACT_PHOTO)?.let { uri ->
            contactPhotoUri = Uri.parse(uri)
            renderPhoto()
        }

        if (contactId != null) {
            supportActionBar?.title = getString(R.string.contact_wizard_title_edit)
        }

        updateValidationMessages()
        updateSummary()
        updateNavigationState()
    }

    private fun updateSummary() {
        val name = binding.nameInput.text?.toString().orEmpty().ifBlank {
            getString(R.string.contact_wizard_placeholder_name)
        }
        val phone = binding.phoneInput.text?.toString().orEmpty().ifBlank {
            getString(R.string.contact_wizard_placeholder_phone)
        }
        binding.summaryName.text = name
        binding.summaryPhone.text = ContactWizardValidator.cleanedPhone(phone)
        binding.summaryWhatsapp.text = if (binding.whatsappSwitch.isChecked) {
            getString(R.string.contact_wizard_whatsapp_enabled)
        } else {
            getString(R.string.contact_wizard_whatsapp_disabled)
        }
    }

    private fun updateValidationMessages() {
        val name = binding.nameInput.text?.toString().orEmpty()
        val phone = binding.phoneInput.text?.toString().orEmpty()

        setValidationMessage(
            isValid = ContactWizardValidator.isValidName(name),
            onView = binding.nameValidationMessage,
            validText = getString(R.string.contact_wizard_name_valid),
            invalidText = getString(R.string.contact_wizard_name_error)
        )

        setValidationMessage(
            isValid = ContactWizardValidator.isValidPhone(phone),
            onView = binding.phoneValidationMessage,
            validText = getString(R.string.contact_wizard_phone_valid),
            invalidText = getString(R.string.contact_wizard_phone_error)
        )
    }

    private fun setValidationMessage(isValid: Boolean, onView: android.widget.TextView, validText: String, invalidText: String) {
        val color = if (isValid) {
            ContextCompat.getColor(this, R.color.ezivia_success)
        } else {
            ContextCompat.getColor(this, R.color.ezivia_critical)
        }
        onView.setTextColor(color)
        onView.text = if (isValid) validText else invalidText
    }

    private fun updateVoiceValidationMessage(text: String, isPositive: Boolean, neutral: Boolean = false) {
        val targetColor = when {
            neutral -> ContextCompat.getColor(this, R.color.ezivia_on_surface_variant)
            isPositive -> ContextCompat.getColor(this, R.color.ezivia_success)
            else -> ContextCompat.getColor(this, R.color.ezivia_critical)
        }
        binding.voiceValidationMessage.setTextColor(targetColor)
        binding.voiceValidationMessage.text = text
    }

    private fun updateStepUi() {
        binding.nameStepGroup.isVisible = currentStep == 0
        binding.phoneStepGroup.isVisible = currentStep == 1
        binding.summaryStepGroup.isVisible = currentStep == 2

        binding.stepIndicator.text = getString(
            R.string.contact_wizard_step_indicator,
            currentStep + 1,
            TOTAL_STEPS
        )

        binding.stepTitle.text = when (currentStep) {
            0 -> getString(R.string.contact_wizard_step_title_name)
            1 -> getString(R.string.contact_wizard_step_title_phone)
            else -> getString(R.string.contact_wizard_step_title_summary)
        }

        val progressValue = ((currentStep + 1) * 100) / TOTAL_STEPS
        binding.stepProgress.setProgressCompat(progressValue, true)

        binding.nextButton.isVisible = currentStep < TOTAL_STEPS - 1
        binding.backButton.isEnabled = currentStep > 0

        updateNavigationState()
    }

    private fun updateNavigationState() {
        binding.nextButton.isEnabled = isCurrentStepValid()
        binding.saveContactButton.isEnabled = allStepsValid()
    }

    private fun isCurrentStepValid(): Boolean {
        return when (currentStep) {
            0 -> ContactWizardValidator.isValidName(binding.nameInput.text?.toString().orEmpty())
            1 -> ContactWizardValidator.isValidPhone(binding.phoneInput.text?.toString().orEmpty())
            else -> allStepsValid()
        }
    }

    private fun allStepsValid(): Boolean {
        val baseValid = ContactWizardValidator.isValidName(binding.nameInput.text?.toString().orEmpty()) &&
            ContactWizardValidator.isValidPhone(binding.phoneInput.text?.toString().orEmpty())
        val voiceReady = !binding.voiceConfirmationSwitch.isChecked || voiceConfirmed
        return baseValid && voiceReady
    }

    private fun goToStep(step: Int) {
        currentStep = step.coerceIn(0, TOTAL_STEPS - 1)
        updateStepUi()
    }

    private fun launchVoicePrompt() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.contact_wizard_voice_prompt))
        }
        voiceConfirmationLauncher.launch(intent)
    }

    private fun handleVoiceResult(result: ActivityResult) {
        if (result.resultCode != RESULT_OK) {
            updateVoiceValidationMessage(
                text = getString(R.string.contact_wizard_voice_retry),
                isPositive = false
            )
            voiceConfirmed = false
            updateNavigationState()
            return
        }

        val transcript = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            .orEmpty()

        if (transcript.isBlank()) {
            updateVoiceValidationMessage(
                text = getString(R.string.contact_wizard_voice_retry),
                isPositive = false
            )
            voiceConfirmed = false
            updateNavigationState()
            return
        }

        voiceConfirmed = ContactWizardValidator.matchesVoiceConfirmation(transcript)
        if (voiceConfirmed) {
            updateVoiceValidationMessage(
                text = getString(R.string.contact_wizard_voice_confirmed, transcript),
                isPositive = true
            )
        } else {
            updateVoiceValidationMessage(
                text = getString(R.string.contact_wizard_voice_not_understood, transcript),
                isPositive = false
            )
        }
        updateNavigationState()
    }

    private fun renderPhoto() {
        if (contactPhotoUri == null) {
            binding.contactPhoto.setImageResource(R.drawable.ic_action_photos)
            binding.contactPhoto.imageTintList =
                ContextCompat.getColorStateList(this, R.color.ezivia_on_surface_variant)
        } else {
            binding.contactPhoto.setImageURI(contactPhotoUri)
            binding.contactPhoto.imageTintList = null
        }
    }

    private fun sendResultAndOpenContacts() {
        val name = binding.nameInput.text?.toString().orEmpty().trim()
        val phone = ContactWizardValidator.cleanedPhone(binding.phoneInput.text?.toString().orEmpty())
        val useWhatsApp = binding.whatsappSwitch.isChecked

        val resultIntent = Intent().apply {
            putExtra(EXTRA_CONTACT_NAME, name)
            putExtra(EXTRA_CONTACT_PHONE, phone)
            putExtra(EXTRA_USE_WHATSAPP, useWhatsApp)
            putExtra(EXTRA_CONTACT_PHOTO, contactPhotoUri?.toString())
        }
        setResult(RESULT_OK, resultIntent)

        val note = if (useWhatsApp) {
            getString(R.string.contact_wizard_contact_note)
        } else {
            ""
        }

        val actionIntent = if (contactId == null) {
            Intent(ContactsContract.Intents.Insert.ACTION).apply {
                type = ContactsContract.RawContacts.CONTENT_TYPE
                putExtra(ContactsContract.Intents.Insert.NAME, name)
                putExtra(ContactsContract.Intents.Insert.PHONE, phone)
                putExtra(
                    ContactsContract.Intents.Insert.PHONE_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                )
                putExtra(ContactsContract.Intents.Insert.STARRED, true)
                if (note.isNotEmpty()) putExtra(ContactsContract.Intents.Insert.NOTES, note)
            }
        } else {
            Intent(Intent.ACTION_EDIT).apply {
                data = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId!!)
                putExtra("finishActivityOnSaveCompleted", true)
                if (note.isNotEmpty()) putExtra(ContactsContract.Intents.Insert.NOTES, note)
            }
        }

        runCatching {
            startActivity(actionIntent)
            finish()
        }.onFailure {
            showErrorFeedback(R.string.contact_wizard_save_error)
        }
    }

    override fun onBackPressed() {
        if (currentStep > 0) {
            goToStep(currentStep - 1)
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val TOTAL_STEPS = 3
        const val EXTRA_CONTACT_ID = "extra_contact_id"
        const val EXTRA_CONTACT_NAME = "extra_contact_name"
        const val EXTRA_CONTACT_PHONE = "extra_contact_phone"
        const val EXTRA_USE_WHATSAPP = "extra_use_whatsapp"
        const val EXTRA_CONTACT_PHOTO = "extra_contact_photo"
    }
}
