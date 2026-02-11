package com.shor.tbuddy

import android.content.Context

/**
 * Manages a rolling pool of API keys to avoid rate limits.
 */
class KeyVault(context: Context) {
    private val prefs = context.getSharedPreferences("ShortBuddyKeys", Context.MODE_PRIVATE)

    // This is where you paste your keys (one per line) in the Engine Room UI
    // For now, we store them in a list
    private var keyPool: List<String> = emptyList()
    private var lastUsedIndex: Int
        get() = prefs.getInt("last_key_index", -1)
        set(value) = prefs.edit().putInt("last_key_index", value).apply()

    fun updatePool(rawKeys: String) {
        keyPool = rawKeys.lines().map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun getNextKey(): String? {
        if (keyPool.isEmpty()) return null

        // Pick a starting index that isn't the last used one
        var nextIndex = (lastUsedIndex + 1) % keyPool.size

        // Update the index for next time
        lastUsedIndex = nextIndex
        return keyPool[nextIndex]
    }

    fun getPoolSize(): Int = keyPool.size
}