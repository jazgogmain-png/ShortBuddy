package com.shor.tbuddy.ui

import android.content.Context

class KeyVault(context: Context) {
    private val prefs = context.getSharedPreferences("ShortBuddyPrefs", Context.MODE_PRIVATE)
    private var currentKeyIndex = 0

    // Gets all keys from the multi-line text field, one per line
    private fun getKeys(): List<String> {
        val rawKeys = prefs.getString("gemini_api_key", "") ?: ""
        return rawKeys.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    fun getActiveKey(): String? {
        val keys = getKeys()
        if (keys.isEmpty()) return null
        // Safety wrap if the list size changed
        if (currentKeyIndex >= keys.size) currentKeyIndex = 0
        return keys[currentKeyIndex]
    }

    fun rotate(): Boolean {
        val keys = getKeys()
        if (keys.size <= 1) return false // Nowhere to rotate
        currentKeyIndex = (currentKeyIndex + 1) % keys.size
        return true
    }

    fun getIndexInfo(): String {
        val size = getKeys().size
        return if (size > 0) "[KEY_${currentKeyIndex + 1}_OF_$size]" else "[NO_KEYS_LOADED]"
    }
}