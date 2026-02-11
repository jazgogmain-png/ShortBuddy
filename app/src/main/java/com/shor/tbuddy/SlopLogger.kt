package com.shor.tbuddy

import android.util.Log

object SlopLogger {
    private const val TAG = "lola_gemini"

    fun info(msg: String) {
        Log.e(TAG, "ℹ️ [LOLA_INFO] $msg")
    }

    fun success(msg: String) {
        Log.e(TAG, "✅ [LOLA_SUCCESS] $msg")
    }

    fun error(msg: String, e: Throwable? = null) {
        Log.e(TAG, "🛑 [LOLA_ERROR] $msg")
        e?.printStackTrace()
    }

    fun keyRotation(index: Int, key: String) {
        val masked = if (key.length > 10) key.take(4) + "..." + key.takeLast(4) else "****"
        Log.e(TAG, "🔑 [LOLA_KEY] Slot #$index Active ($masked)")
    }
}