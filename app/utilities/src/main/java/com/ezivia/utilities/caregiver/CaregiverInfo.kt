package com.ezivia.utilities.caregiver

import java.io.Serializable

/**
 * Represents the essential information for a caregiver contact that Ezivia
 * highlights across the experience.
 */
data class CaregiverInfo(
    val name: String,
    val phoneNumber: String,
    val relationship: String,
) : Serializable {
    fun asDisplayText(): String = "$name • $relationship • $phoneNumber"
}
