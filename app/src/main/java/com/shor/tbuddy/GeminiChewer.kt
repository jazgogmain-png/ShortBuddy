package com.shor.tbuddy

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.shor.tbuddy.ui.KeyVault

class GeminiChewer(private val keyVault: KeyVault) {

    suspend fun chewWithRetry(
        context: Context,
        videoBytes: ByteArray,
        onUpdate: (String) -> Unit
    ): Map<String, String>? {
        var attempts = 0
        val maxAttempts = 3

        while (attempts < maxAttempts) {
            val currentKey = keyVault.getActiveKey() ?: return null

            try {
                return performChew(context, currentKey, videoBytes)
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

    private suspend fun performChew(context: Context, apiKey: String, videoBytes: ByteArray): Map<String, String> = withContext(Dispatchers.IO) {
        val model = GenerativeModel(
            modelName = "gemini-3-flash-preview",
            apiKey = apiKey,
            requestOptions = RequestOptions(apiVersion = "v1beta")
        )

        val prefs = context.getSharedPreferences("ShortBuddyPrefs", Context.MODE_PRIVATE)
        val masterPrompt = prefs.getString("master_system_prompt", "Analyze this video for a viral animal channel.")
        val staticTags = prefs.getString("static_tags", "#shorts #babyanimals")
        val automationLevel = prefs.getFloat("automation_level", 0f)

        val ninjaPrompt = """
            $masterPrompt
            
            CONTEXTUAL_INSTRUCTIONS:
            - Automation Level is $automationLevel%. If high, be more assertive in your creative choices.
            - Always include these base tags: $staticTags
            
            Return ONLY these labels with their data:
            DETECTION: [Summarize animal, emotion, motion, lighting, and loop potential]
            CAPTION: [Emoji-rich viral caption using current viral trends]
            OVERLAY: [5-word max on-screen hook]
            HASHTAGS: [Include base tags + 4 niche tags]
            SEO_TAGS: [Comma separated SEO keywords]
            MUSIC: [Recommended mood/genre]
            VEO_PROMPT: [Ultra-detailed 9:16 generation prompt for future variations]
        """.trimIndent()

        val response = model.generateContent(content {
            blob("video/mp4", videoBytes)
            text(ninjaPrompt)
        })

        parseNeuralOutput(response.text ?: "")
    }

    // 🧠 THE LAZY NINJA ANALYTICS SCANNER
    suspend fun chewAnalytics(imageBytes: ByteArray): String? = withContext(Dispatchers.IO) {
        val currentKey = keyVault.getActiveKey() ?: return@withContext null

        val model = GenerativeModel(
            modelName = "gemini-3-flash-preview",
            apiKey = currentKey,
            requestOptions = RequestOptions(apiVersion = "v1beta")
        )

        val prompt = """
            Look at this YouTube Analytics screenshot of 'When your viewers are on YouTube'.
            Analyze the heatmap/bars and determine the single peak time for audience activity.
            Return ONLY the time in 24-hour format (HH:mm). 
            Example: 08:00
        """.trimIndent()

        return@withContext try {
            val response = model.generateContent(content {
                blob("image/jpeg", imageBytes)
                text(prompt)
            })
            response.text?.trim()?.take(5) // Just take the HH:mm
        } catch (e: Exception) {
            null
        }
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