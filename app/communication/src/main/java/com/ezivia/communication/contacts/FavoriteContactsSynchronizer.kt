package com.ezivia.communication.contacts

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

/**
 * Watches the user's favourite contacts stored on the device and keeps Ezivia
 * in sync whenever they change.
 */
class FavoriteContactsSynchronizer(
    private val contentResolver: ContentResolver,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    /** Returns a hot [Flow] that emits the current list of favourites. */
    fun favoriteContacts(): Flow<List<FavoriteContact>> = callbackFlow {
        fun emitFavorites() {
            launch(dispatcher) {
                try {
                    trySend(queryFavoriteContacts())
                } catch (securityException: SecurityException) {
                    Log.w(TAG, "Missing permission to read contacts", securityException)
                    trySend(emptyList())
                }
            }
        }

        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                emitFavorites()
            }

            override fun onChange(selfChange: Boolean, uri: Uri?) {
                emitFavorites()
            }
        }

        contentResolver.registerContentObserver(
            ContactsContract.Contacts.CONTENT_URI,
            true,
            observer
        )

        emitFavorites()

        awaitClose { contentResolver.unregisterContentObserver(observer) }
    }.conflate()

    @Suppress("MissingPermission")
    private fun queryFavoriteContacts(): List<FavoriteContact> {
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.HAS_PHONE_NUMBER,
        )
        val selection = "${ContactsContract.Contacts.STARRED} = ? AND ${ContactsContract.Contacts.IN_VISIBLE_GROUP} = ?"
        val selectionArgs = arrayOf("1", "1")
        val sortOrder = "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} COLLATE NOCASE ASC"

        val results = mutableListOf<FavoriteContact>()

        contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder,
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
            val nameIndex = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            val hasPhoneIndex = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)

            while (cursor.moveToNext()) {
                val hasPhone = cursor.getInt(hasPhoneIndex) > 0
                if (!hasPhone) continue

                val contactId = cursor.getLong(idIndex)
                val displayName = cursor.getString(nameIndex)?.takeIf { it.isNotBlank() } ?: continue
                val phoneNumber = loadPrimaryPhoneNumber(contactId) ?: continue

                results += FavoriteContact(
                    id = contactId,
                    displayName = displayName,
                    phoneNumber = phoneNumber,
                )
            }
        }

        return results
    }

    @Suppress("MissingPermission")
    private fun loadPrimaryPhoneNumber(contactId: Long): String? {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.IS_PRIMARY,
        )
        val selection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?"
        val selectionArgs = arrayOf(contactId.toString())
        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY} DESC, ${ContactsContract.CommonDataKinds.Phone.IS_PRIMARY} DESC"

        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder,
        )?.use { cursor ->
            val numberIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (cursor.moveToNext()) {
                val number = cursor.getString(numberIndex)?.takeIf { it.isNotBlank() }
                if (number != null) {
                    return number
                }
            }
        }

        return null
    }

    companion object {
        private const val TAG = "FavContactsSync"
    }
}
