package com.shor.tbuddy

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.*
import com.shor.tbuddy.models.ShortsMetadata
import com.shor.tbuddy.models.ChannelTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.shor.tbuddy.ui.KeyVault

class GeminiChewer(private val keyVault: KeyVault) {

    suspend fun chewWithRetry(
        videoBytes: ByteArray,
        onUpdate: (String) -> Unit
    ): Map<String, String>? {
        var attempts = 0
        val maxAttempts = 3

        while (attempts < maxAttempts) {
            val currentKey = keyVault.getActiveKey() ?: return null

            try {
                return performChew(currentKey, videoBytes)
            } catch (e: Exception) {
                attempts++
                val isTrafficJam = e.message?.contains("503") == true || e.message?.contains("high demand") == true

                if (isTrafficJam && attempts < maxAttempts) {
                    onUpdate("🛑 G3_TRAFFIC_JAM: Retrying in 2s...")
                    delay(2000)
                    keyVault.rotate()
                } else {
                    onUpdate("❌ ANALYSIS_FAILED: ${e.localizedMessage}")
                    return null
                }
            }
        }
        return null
    }

    private suspend fun performChew(apiKey: String, videoBytes: ByteArray): Map<String, String> = withContext(Dispatchers.IO) {
        val model = GenerativeModel(
            modelName = "gemini-3-flash-preview",
            apiKey = apiKey,
            requestOptions = RequestOptions(apiVersion = "v1beta")
        )

        val ninjaPrompt = """
            Analyze this video for a viral animal channel. 
            Return ONLY these labels with their data:
            
            DETECTION: [Summarize animal, emotion, motion, lighting, and loop potential]
            CAPTION: [Emoji-rich viral caption]
            OVERLAY: [5-word max on-screen hook]
            HASHTAGS: [#shorts plus 4 niche tags]
            SEO_TAGS: [Comma separated SEO keywords]
            MUSIC: [Recommended mood/genre]
            VEO_PROMPT: [Ultra-detailed 9:16 generation prompt]
        """.trimIndent()

        val response = model.generateContent(content {
            blob("video/mp4", videoBytes)
            text(ninjaPrompt)
        })

        parseNeuralOutput(response.text ?: "")
    }

    private fun parseNeuralOutput(rawText: String): Map<String, String> {
        val cleanText = rawText.replace("**", "").replace("##", "")
        val lines = cleanText.lines()

        fun extract(label: String) = lines
            .find { it.startsWith(label, ignoreCase = true) }
            ?.substringAfter(":")?.trim() ?: ""

        return mapOf(
            "detection" to extract("DETECTION"),
            "caption" to extract("CAPTION"),
            "overlay" to extract("OVERLAY"),
            "hashtags" to extract("HASHTAGS"),
            "seo" to extract("SEO_TAGS"),
            "music" to extract("MUSIC"),
            "prompt" to extract("VEO_PROMPT")
        )
    }
}