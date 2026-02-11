package com.shor.tbuddy

import android.content.Context

/**
 * Manages a rolling pool of API keys to avoid rate limits.
 * Professional version: Persists keys and handles the 'Key Rotation' logic.
 */
class KeyVault(private val context: Context) {

    // Unified preference file for the whole project
    private val prefs = context.getSharedPreferences("ShortBuddyPrefs", Context.MODE_PRIVATE)

    private var keyPool: List<String> = emptyList()

    private var lastUsedIndex: Int
        get() = prefs.getInt("last_key_index", -1)
        set(value) = prefs.edit().putInt("last_key_index", value).apply()

    init {
        // Auto-load keys on initialization so the pool isn't 0 on start
        updatePool()
    }

    /**
     * Reads the raw keys from storage and refreshes the live pool.
     */
    fun updatePool() {
        val rawKeys = prefs.getString("raw_key_pool", "") ?: ""
        keyPool = rawKeys.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        SlopLogger.info("KEY_VAULT: Pool refreshed. Total keys: ${keyPool.size}")
    }

    /**
     * Overloaded version for direct updates from the Engine Room UI.
     */
    fun updatePool(rawKeys: String) {
        keyPool = rawKeys.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        SlopLogger.info("KEY_VAULT: Direct update. Total keys: ${keyPool.size}")
    }

    /**
     * Rotates to the next available key in the pool.
     */
    fun getNextKey(): String? {
        if (keyPool.isEmpty()) {
            // Last ditch attempt: try to reload from storage
            updatePool()
            if (keyPool.isEmpty()) return null
        }

        val nextIndex = (lastUsedIndex + 1) % keyPool.size
        lastUsedIndex = nextIndex

        return keyPool[nextIndex]
    }

    fun getPoolSize(): Int = keyPool.size
}