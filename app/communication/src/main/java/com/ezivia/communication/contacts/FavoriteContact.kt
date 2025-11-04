package com.ezivia.communication.contacts

/**
 * Represents a simplified view of a favourite contact that Ezivia can display
 * and interact with.
 */
data class FavoriteContact(
    val id: Long,
    val displayName: String,
    val phoneNumber: String
)
