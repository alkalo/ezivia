package com.ezivia.utilities.flashlight

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Controls the device flashlight with a senior-friendly API.
 * The controller keeps track of the flashlight availability and notifies
 * listeners whenever the state changes so the UI can remain clear and calm.
 */
class FlashlightController(context: Context) {

    private val appContext = context.applicationContext
    private val cameraManager: CameraManager =
        appContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val handler = Handler(Looper.getMainLooper())
    private val listeners = CopyOnWriteArraySet<Listener>()

    @Volatile
    private var torchCameraId: String? = findTorchCameraId()

    @Volatile
    private var isTorchAvailable: Boolean = torchCameraId != null

    @Volatile
    private var isTorchOn: Boolean = false

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            if (cameraId == torchCameraId) {
                isTorchOn = enabled
                notifyStateChanged()
            }
        }

        override fun onTorchModeUnavailable(cameraId: String) {
            if (cameraId == torchCameraId) {
                isTorchAvailable = false
                notifyAvailabilityChanged()
            }
        }
    }

    init {
        cameraManager.registerTorchCallback(torchCallback, handler)
        if (torchCameraId == null) {
            notifyAvailabilityChanged()
        }
    }

    /** Returns true when the flashlight hardware can be used. */
    fun isAvailable(): Boolean = isTorchAvailable

    /** Returns the latest known flashlight state. */
    fun isOn(): Boolean = isTorchOn

    /**
     * Turns the flashlight on. Returns true on success, false if the hardware
     * is missing or a system error occurred.
     */
    fun turnOn(): Boolean = setTorchMode(true)

    /**
     * Turns the flashlight off. Returns true on success, false otherwise.
     */
    fun turnOff(): Boolean = setTorchMode(false)

    /**
     * Toggles the flashlight. Returns true when the state could be updated.
     */
    fun toggle(): Boolean = setTorchMode(!isTorchOn)

    /** Registers a listener for availability and state changes. */
    fun addListener(listener: Listener) {
        listeners.add(listener)
        listener.onAvailabilityChanged(isTorchAvailable)
        listener.onFlashlightStateChanged(isTorchOn)
    }

    /** Removes a previously registered listener. */
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    /**
     * Cleans up callbacks when the controller is no longer needed.
     */
    fun release() {
        listeners.clear()
        cameraManager.unregisterTorchCallback(torchCallback)
    }

    private fun setTorchMode(enabled: Boolean): Boolean {
        val cameraId = torchCameraId ?: run {
            isTorchAvailable = false
            notifyAvailabilityChanged()
            return false
        }

        return try {
            cameraManager.setTorchMode(cameraId, enabled)
            isTorchOn = enabled
            isTorchAvailable = true
            notifyStateChanged()
            true
        } catch (error: CameraAccessException) {
            isTorchAvailable = false
            notifyAvailabilityChanged()
            false
        } catch (error: SecurityException) {
            // The calling app might not have camera permission yet.
            isTorchAvailable = false
            notifyAvailabilityChanged()
            false
        }
    }

    private fun findTorchCameraId(): String? {
        val ids = try {
            cameraManager.cameraIdList
        } catch (error: CameraAccessException) {
            emptyArray()
        }

        for (cameraId in ids) {
            val characteristics = try {
                cameraManager.getCameraCharacteristics(cameraId)
            } catch (error: CameraAccessException) {
                continue
            }
            val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            if (hasFlash) {
                return cameraId
            }
        }
        return null
    }

    private fun notifyAvailabilityChanged() {
        for (listener in listeners) {
            listener.onAvailabilityChanged(isTorchAvailable)
        }
    }

    private fun notifyStateChanged() {
        for (listener in listeners) {
            listener.onFlashlightStateChanged(isTorchOn)
        }
    }

    interface Listener {
        fun onAvailabilityChanged(isAvailable: Boolean)
        fun onFlashlightStateChanged(isOn: Boolean)
    }
}
