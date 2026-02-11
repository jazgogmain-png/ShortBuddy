package com.shor.tbuddy

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.shor.tbuddy.models.ShortsMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiChewer(private val keyVault: KeyVault) {

    suspend fun chewWithRetry(videoBytes: ByteArray, laneVibe: String): ShortsMetadata? {
        val totalKeys = keyVault.getPoolSize()
        SlopLogger.info("PHASE 1: AWAKENING THE GATEKEEPER. Pool size: $totalKeys")

        for (i in 0 until totalKeys) {
            val currentKey = keyVault.getNextKey() ?: break
            SlopLogger.keyRotation(i, currentKey)

            try {
                val result = performChew(currentKey, videoBytes, laneVibe)
                SlopLogger.success("INCOMING SLOP: Metadata generated successfully.")
                return result
            } catch (e: Exception) {
                SlopLogger.error("BARTER FAIL: Key #$i rejected.", e)
            }
        }
        SlopLogger.error("TOTAL BLACKOUT: All keys in the pool exhausted.")
        return null
    }

    private suspend fun performChew(apiKey: String, videoBytes: ByteArray, laneVibe: String): ShortsMetadata = withContext(Dispatchers.IO) {
        val model = GenerativeModel(
            modelName = "gemini-3-flash-preview",
            apiKey = apiKey,
            safetySettings = listOf(
                SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE)
            )
        )

        SlopLogger.info("MAPPING BLOB: ${videoBytes.size / 1024} KB of raw slop being sent.")

        val response = model.generateContent(
            content {
                blob("video/mp4", videoBytes)
                text("Analyze for $laneVibe lane. Return strictly: Title: [Title] Description: [Hook] Tags: [tag1, tag2]")
            }
        )

        val rawText = response.text ?: ""
        SlopLogger.info("RAW_REPLY: $rawText")
        parseMetadata(rawText)
    }

    private fun parseMetadata(rawText: String): ShortsMetadata {
        val lines = rawText.lines()
        val title = lines.find { it.startsWith("Title:") }?.removePrefix("Title:")?.trim() ?: "Viral Clip"
        val desc = lines.find { it.startsWith("Description:") }?.removePrefix("Description:")?.trim() ?: ""
        val tags = lines.find { it.startsWith("Tags:") }?.removePrefix("Tags:")?.trim()?.split(",") ?: emptyList()
        return ShortsMetadata(title, desc, tags.map { it.trim() })
    }
}