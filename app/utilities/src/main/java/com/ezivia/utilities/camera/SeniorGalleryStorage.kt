package com.ezivia.utilities.camera

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Persists the most recent photos for the camera flow using SharedPreferences.
 * Designed so seniors can reopen the app and still find their latest captures.
 */
internal class SeniorGalleryStorage(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun savePhoto(photo: CapturedPhoto) {
        val current = loadMutableList()
        val existingIndex = current.indexOfFirst { it.uri == photo.uri }
        if (existingIndex >= 0) {
            current.removeAt(existingIndex)
        }
        current.add(0, photo)
        if (current.size > MAX_PHOTOS) {
            current.subList(MAX_PHOTOS, current.size).clear()
        }
        persist(current)
    }

    fun getRecentPhotos(limit: Int): List<CapturedPhoto> {
        val list = loadMutableList()
        return if (list.size <= limit) list else list.take(limit)
    }

    fun clear() {
        preferences.edit().remove(KEY_PHOTOS).apply()
    }

    private fun loadMutableList(): MutableList<CapturedPhoto> {
        val stored = preferences.getString(KEY_PHOTOS, null) ?: return mutableListOf()
        return try {
            val array = JSONArray(stored)
            MutableList(array.length()) { index ->
                array.getJSONObject(index).toCapturedPhoto()
            }
        } catch (_: JSONException) {
            mutableListOf()
        }
    }

    private fun persist(list: List<CapturedPhoto>) {
        val array = JSONArray()
        list.forEach { photo ->
            array.put(photo.toJson())
        }
        preferences.edit().putString(KEY_PHOTOS, array.toString()).apply()
    }

    private fun CapturedPhoto.toJson(): JSONObject = JSONObject().apply {
        put(KEY_URI, uri.toString())
        put(KEY_TIMESTAMP, takenAtMillis)
        put(KEY_SOURCE, source.name)
    }

    private fun JSONObject.toCapturedPhoto(): CapturedPhoto {
        val uriString = optString(KEY_URI)
        return CapturedPhoto(
            uri = Uri.parse(uriString),
            takenAtMillis = optLong(KEY_TIMESTAMP),
            source = PhotoSource.valueOf(optString(KEY_SOURCE, PhotoSource.CAMERA.name))
        )
    }

    companion object {
        private const val PREFS_NAME = "ezivia_camera_store"
        private const val KEY_PHOTOS = "photos"
        private const val KEY_URI = "uri"
        private const val KEY_TIMESTAMP = "timestamp"
        private const val KEY_SOURCE = "source"
        private const val MAX_PHOTOS = 20
    }
}
