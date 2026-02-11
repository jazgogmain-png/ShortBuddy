package com.shor.tbuddy

import android.util.Log

/**
 * Matrix Telemetry for ShortBuddy.
 * Use the 'lola_gemini' tag in Logcat to filter.
 */
object SlopLogger {
    private const val TAG = "lola_gemini"

    fun info(message: String) {
        Log.i(TAG, "🟢 [INFO] $message")
    }

    fun success(message: String) {
        Log.d(TAG, "🐲 [GEMINI_READY] $message")
    }

    fun error(message: String, throwable: Throwable? = null) {
        Log.e(TAG, "🛑 [CRITICAL_FAIL] $message", throwable)
    }

    fun keyRotation(keyIndex: Int, keySnippet: String) {
        Log.w(TAG, "🌀 [KEY_ROTATION] Swapping to Key #$keyIndex (...${keySnippet.takeLast(4)})")
    }
}