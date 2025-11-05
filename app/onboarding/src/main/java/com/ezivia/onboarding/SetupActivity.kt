package com.ezivia.onboarding

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ezivia.onboarding.databinding.ActivitySetupBinding
import com.ezivia.utilities.caregiver.CaregiverInfo
import com.ezivia.utilities.caregiver.CaregiverPreferences

private const val STATE_STEP = "setup_step"
private const val STATE_CAREGIVERS = "setup_caregivers"

/**
 * Guides family members or caregivers through the initial Ezivia configuration
 * so the launcher can stay focused on the essentials for older adults.
 */
class SetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupBinding
    private lateinit var caregiverPreferences: CaregiverPreferences

    private val caregivers = mutableListOf<CaregiverInfo>()
    private val caregiverSummaries = mutableListOf<String>()
    private lateinit var caregiverAdapter: ArrayAdapter<String>
    private var currentStep: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        caregiverPreferences = CaregiverPreferences(this)

        caregiverAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            caregiverSummaries
        )
        binding.caregiverListView.adapter = caregiverAdapter

        binding.addCaregiverButton.setOnClickListener { addCaregiver() }
        binding.previousButton.setOnClickListener { moveToStep(currentStep - 1) }
        binding.nextButton.setOnClickListener { onNextClicked() }

        if (savedInstanceState != null) {
            currentStep = savedInstanceState.getInt(STATE_STEP, 0)
            @Suppress("UNCHECKED_CAST")
            val restored = savedInstanceState.getSerializable(STATE_CAREGIVERS) as? ArrayList<CaregiverInfo>
            if (!restored.isNullOrEmpty()) {
                caregivers.addAll(restored)
                caregiverSummaries.addAll(restored.map { it.asDisplayText() })
                caregiverAdapter.notifyDataSetChanged()
                updateCaregiverCount()
            }
        }

        moveToStep(currentStep)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_STEP, currentStep)
        outState.putSerializable(STATE_CAREGIVERS, ArrayList(caregivers))
    }

    private fun moveToStep(step: Int) {
        currentStep = step.coerceIn(0, binding.stepsViewFlipper.childCount - 1)
        binding.stepsViewFlipper.displayedChild = currentStep
        binding.previousButton.isEnabled = currentStep > 0

        val nextText = if (currentStep == binding.stepsViewFlipper.childCount - 1) {
            R.string.setup_finish_button
        } else {
            R.string.setup_next_button
        }
        binding.nextButton.setText(nextText)

        binding.stepIndicator.text = getString(
            R.string.setup_step_indicator,
            currentStep + 1,
            binding.stepsViewFlipper.childCount
        )
    }

    private fun onNextClicked() {
        when (currentStep) {
            0 -> moveToStep(currentStep + 1)
            1 -> if (validateCaregivers()) completeSetup()
        }
    }

    private fun addCaregiver() {
        val name = binding.caregiverNameInput.editText?.text?.toString()?.trim().orEmpty()
        val phone = binding.caregiverPhoneInput.editText?.text?.toString()?.trim().orEmpty()
        val relationship = binding.caregiverRelationshipInput.editText?.text?.toString()?.trim().orEmpty()

        var hasError = false
        if (name.isEmpty()) {
            binding.caregiverNameInput.error = getString(R.string.setup_error_required)
            hasError = true
        } else {
            binding.caregiverNameInput.error = null
        }

        if (phone.isEmpty()) {
            binding.caregiverPhoneInput.error = getString(R.string.setup_error_required)
            hasError = true
        } else {
            binding.caregiverPhoneInput.error = null
        }

        if (relationship.isEmpty()) {
            binding.caregiverRelationshipInput.error = getString(R.string.setup_error_required)
            hasError = true
        } else {
            binding.caregiverRelationshipInput.error = null
        }

        if (hasError) return

        val caregiverInfo = CaregiverInfo(name = name, phoneNumber = phone, relationship = relationship)
        caregivers.add(caregiverInfo)
        caregiverSummaries.add(caregiverInfo.asDisplayText())
        caregiverAdapter.notifyDataSetChanged()
        updateCaregiverCount()

        binding.caregiverNameInput.editText?.text?.clear()
        binding.caregiverPhoneInput.editText?.text?.clear()
        binding.caregiverRelationshipInput.editText?.text?.clear()

        Toast.makeText(this, R.string.setup_caregiver_added, Toast.LENGTH_SHORT).show()
    }

    private fun validateCaregivers(): Boolean {
        if (caregivers.isEmpty()) {
            binding.caregiverEmptyState.visibility = View.VISIBLE
            binding.caregiverEmptyState.text = getString(R.string.setup_caregiver_required)
            return false
        }
        binding.caregiverEmptyState.visibility = View.GONE
        return true
    }

    private fun updateCaregiverCount() {
        binding.caregiverEmptyState.visibility = if (caregivers.isEmpty()) View.VISIBLE else View.GONE
        if (caregivers.isEmpty()) {
            binding.caregiverEmptyState.text = getString(R.string.setup_caregiver_hint)
        }
    }

    private fun completeSetup() {
        caregiverPreferences.saveCaregivers(caregivers)

        Toast.makeText(this, R.string.setup_completed_message, Toast.LENGTH_LONG).show()
        setResult(RESULT_OK)
        finish()
    }
}
