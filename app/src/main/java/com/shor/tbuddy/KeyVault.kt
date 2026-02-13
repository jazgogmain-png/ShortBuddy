package com.shor.tbuddy.ui

import android.content.Context
import com.shor.tbuddy.SlopLogger

class KeyVault(context: Context) {
    private val prefs = context.getSharedPreferences("ShortBuddyPrefs", Context.MODE_PRIVATE)
    private var currentKeyIndex = 0

    private fun getKeys(): List<String> {
        val rawKeys = prefs.getString("gemini_api_key", "") ?: ""
        return rawKeys.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    // 🧠 Renamed to getApiKey() to match the Chewer's expectations
    fun getApiKey(): String {
        val keys = getKeys()
        if (keys.isEmpty()) {
            SlopLogger.error("KeyVault: No keys found in SharedPreferences!")
            return ""
        }

        if (currentKeyIndex >= keys.size) currentKeyIndex = 0
        val key = keys[currentKeyIndex]

        // Log the active slot to the Nerd Window/Logcat
        SlopLogger.keyRotation(currentKeyIndex, key)
        return key
    }

    fun rotate(): Boolean {
        val keys = getKeys()
        if (keys.size <= 1) {
            SlopLogger.info("KeyVault: Rotation skipped (Only ${keys.size} key available)")
            return false
        }

        currentKeyIndex = (currentKeyIndex + 1) % keys.size
        SlopLogger.success("KeyVault: Rotated to Slot #$currentKeyIndex")
        return true
    }

    fun getIndexInfo(): String {
        val size = getKeys().size
        return if (size > 0) "[KEY_${currentKeyIndex + 1}_OF_$size]" else "[NO_KEYS_LOADED]"
    }
}