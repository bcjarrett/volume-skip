package dev.ben.volumeskip

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

/**
 * Filters volume key events system-wide (including with the screen off).
 *
 * While audio is playing:
 *   - hold volume-up   -> next track
 *   - hold volume-down -> previous track
 *   - short press      -> normal volume step
 * When nothing is playing, volume keys are untouched.
 */
class VolumeSkipService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var audioManager: AudioManager

    private var trackedKeyCode = KeyEvent.KEYCODE_UNKNOWN
    private var pendingSkip: Runnable? = null
    private var skipFired = false

    override fun onServiceConnected() {
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        serviceInfo = serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }
        Log.i(TAG, "connected, filtering volume keys")
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        if (keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
            return false
        }

        when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                // Swallow the auto-repeat stream of a press we already consumed.
                if (event.repeatCount > 0) return keyCode == trackedKeyCode

                if (!audioManager.isMusicActive) return false

                cancelPendingSkip()
                trackedKeyCode = keyCode
                skipFired = false
                pendingSkip = Runnable {
                    pendingSkip = null
                    skipFired = true
                    val mediaKey = if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                        KeyEvent.KEYCODE_MEDIA_NEXT
                    } else {
                        KeyEvent.KEYCODE_MEDIA_PREVIOUS
                    }
                    Log.i(TAG, "long press -> dispatching ${KeyEvent.keyCodeToString(mediaKey)}")
                    dispatchMediaKey(mediaKey)
                    vibrateTick()
                }.also { handler.postDelayed(it, LONG_PRESS_MS) }
                return true
            }

            KeyEvent.ACTION_UP -> {
                if (keyCode != trackedKeyCode) return false
                trackedKeyCode = KeyEvent.KEYCODE_UNKNOWN

                if (skipFired) {
                    skipFired = false
                    return true
                }

                // Released before the hold threshold: apply the volume step we swallowed.
                cancelPendingSkip()
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                        AudioManager.ADJUST_RAISE
                    } else {
                        AudioManager.ADJUST_LOWER
                    },
                    AudioManager.FLAG_SHOW_UI
                )
                return true
            }
        }
        return false
    }

    private fun dispatchMediaKey(mediaKeyCode: Int) {
        val now = SystemClock.uptimeMillis()
        audioManager.dispatchMediaKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, mediaKeyCode, 0))
        audioManager.dispatchMediaKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, mediaKeyCode, 0))
    }

    private fun vibrateTick() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun cancelPendingSkip() {
        pendingSkip?.let { handler.removeCallbacks(it) }
        pendingSkip = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        cancelPendingSkip()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "VolumeSkip"
        private const val LONG_PRESS_MS = 400L
    }
}
