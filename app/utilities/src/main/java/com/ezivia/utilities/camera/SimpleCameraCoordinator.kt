package com.ezivia.utilities.camera

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.contentValuesOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Coordinates a simplified camera and gallery flow aimed at seniors.
 * The coordinator prepares intents to take pictures, persists friendly
 * metadata and offers a short history that can be surfaced in the UI.
 */
class SimpleCameraCoordinator(context: Context) {

    private val appContext = context.applicationContext
    private val resolver: ContentResolver = appContext.contentResolver
    private val storage = SeniorGalleryStorage(appContext)

    /**
     * Creates an intent to capture an image with the system camera.
     * Returns a [CameraCaptureRequest] describing the intent and the
     * [Uri] where the image will be stored, or null when storage is
     * unavailable.
     */
    fun createCameraCaptureRequest(displayName: String? = null): CameraCaptureRequest? {
        val outputUri = createImageUri(displayName) ?: return null
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return CameraCaptureRequest(intent, outputUri)
    }

    /** Prepares an intent so the user can choose an existing photo. */
    fun createGalleryPickerIntent(): Intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
        type = "image/*"
    }

    /** Stores the captured photo when the operation succeeds. */
    fun recordCameraResult(successful: Boolean, request: CameraCaptureRequest): CapturedPhoto? {
        return if (successful) {
            CapturedPhoto(
                uri = request.outputUri,
                takenAtMillis = System.currentTimeMillis(),
                source = PhotoSource.CAMERA
            ).also { storage.savePhoto(it) }
        } else {
            // When capture fails we clean up the unused placeholder.
            resolver.delete(request.outputUri, null, null)
            null
        }
    }

    /** Stores the gallery selection when the user picked an image. */
    fun recordGalleryResult(data: Intent?): CapturedPhoto? {
        val uri = data?.data ?: return null
        return CapturedPhoto(
            uri = uri,
            takenAtMillis = System.currentTimeMillis(),
            source = PhotoSource.GALLERY
        ).also { storage.savePhoto(it) }
    }

    /** Returns the most recent photos the senior interacted with. */
    fun getRecentPhotos(limit: Int = DEFAULT_RECENT_LIMIT): List<CapturedPhoto> =
        storage.getRecentPhotos(limit)

    /** Removes the stored history. */
    fun clearHistory() {
        storage.clear()
    }

    private fun createImageUri(displayName: String?): Uri? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val name = displayName?.takeIf { it.isNotBlank() } ?: "ezivia_foto_$timestamp.jpg"
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val values = contentValuesOf(
            MediaStore.Images.Media.DISPLAY_NAME to name,
            MediaStore.Images.Media.MIME_TYPE to "image/jpeg",
            MediaStore.Images.Media.DATE_ADDED to System.currentTimeMillis() / 1000
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
        }

        return resolver.insert(collection, values)
    }

    companion object {
        private const val DEFAULT_RECENT_LIMIT = 10
    }
}

/** Describes the intent required to take a picture with the system camera. */
data class CameraCaptureRequest(
    val intent: Intent,
    val outputUri: Uri
)

/** Keeps minimal metadata about a photo so seniors can find it later. */
data class CapturedPhoto(
    val uri: Uri,
    val takenAtMillis: Long,
    val source: PhotoSource
)

enum class PhotoSource {
    CAMERA,
    GALLERY
}
