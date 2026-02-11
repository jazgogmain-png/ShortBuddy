package com.shor.tbuddy

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.*
import com.shor.tbuddy.models.ShortsMetadata
import com.shor.tbuddy.models.ChannelTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiChewer(private val keyVault: KeyVault) {

    suspend fun chewWithRetry(videoBytes: ByteArray, template: ChannelTemplate): ShortsMetadata? {
        val totalKeys = keyVault.getPoolSize()
        SlopLogger.info("CHEWER: G3 Mission Start. Keys: $totalKeys")

        for (i in 0 until totalKeys) {
            val currentKey = keyVault.getNextKey() ?: break
            SlopLogger.keyRotation(i, currentKey)

            try {
                SlopLogger.info("CHEWER: Engaging G3 King Strategist...")
                val result = performChew(currentKey, videoBytes, template)
                SlopLogger.success("CHEWER: Viral strategy generated.")
                return result
            } catch (e: Exception) {
                SlopLogger.error("CHEWER: REJECTION: ${e.localizedMessage}")
            }
        }
        return null
    }

    private suspend fun performChew(apiKey: String, videoBytes: ByteArray, template: ChannelTemplate): ShortsMetadata = withContext(Dispatchers.IO) {

        val options = RequestOptions(apiVersion = "v1beta")
        val model = GenerativeModel(
            modelName = "gemini-3-flash-preview",
            apiKey = apiKey,
            requestOptions = options,
            safetySettings = listOf(
                SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE)
            )
        )

        val masterPrompt = """
            Act as a Viral YouTube Shorts Content Strategist for '${template.channelName}'.
            Target Audience: Animal lovers (Baby Schema Trigger).
            Goal: Maximize emotional attachment and loop replay.
            
            Analyze this video and return:
            1) Title: Viral-style caption (max 12 words).
            2) Overlay: Scroll-stopping text (max 6 words, tier 1 engagement).
            3) Tags: 3-5 hashtags including #shorts #viralshorts #aibaby and the animal niche.
            4) Music: Suggested emotional style (e.g., Lofi, Ghibli Piano).
        """.trimIndent()

        val response = model.generateContent(
            content {
                blob("video/mp4", videoBytes)
                text(masterPrompt)
            }
        )

        val rawText = response.text ?: ""
        SlopLogger.info("CHEWER: G3 RAW OUTPUT: ${rawText.take(100)}...")
        parseMetadata(rawText)
    }

    private fun parseMetadata(rawText: String): ShortsMetadata {
        val lines = rawText.lines()
        val title = lines.find { it.contains("Title:") }?.substringAfter("Title:")?.trim() ?: "Viral Clip"
        val overlay = lines.find { it.contains("Overlay:") }?.substringAfter("Overlay:")?.trim() ?: ""
        val tags = lines.find { it.contains("Tags:") }?.substringAfter("Tags:")?.trim()?.split(",") ?: emptyList()

        return ShortsMetadata(
            title = title,
            description = overlay, // We use description field for Overlay Text for now
            tags = tags.map { it.trim() }
        )
    }
}