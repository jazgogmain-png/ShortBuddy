package com.shor.tbuddy

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.shor.tbuddy.ui.KeyVault
import com.shor.tbuddy.models.ChannelBlueprint
import com.shor.tbuddy.models.ProjectEntity

class GeminiChewer(private val keyVault: KeyVault) {

    /**
     * The primary entry point for video analysis.
     * Includes a retry mechanism that rotates API keys if a 'Traffic Jam' occurs.
     */
    suspend fun chewWithRetry(
        context: Context,
        videoBytes: ByteArray,
        blueprint: ChannelBlueprint,
        successStories: List<ProjectEntity>,
        onUpdate: (String) -> Unit
    ): Map<String, String>? {
        var attempts = 0
        val maxAttempts = 3

        while (attempts < maxAttempts) {
            val currentKey = keyVault.getActiveKey() ?: return null

            try {
                return performChew(context, currentKey, videoBytes, blueprint, successStories)
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

    private suspend fun performChew(
        context: Context,
        apiKey: String,
        videoBytes: ByteArray,
        blueprint: ChannelBlueprint,
        successStories: List<ProjectEntity>
    ): Map<String, String> = withContext(Dispatchers.IO) {
        val model = GenerativeModel(
            modelName = "gemini-3-flash-preview",
            apiKey = apiKey,
            requestOptions = RequestOptions(apiVersion = "v1beta")
        )

        val prefs = context.getSharedPreferences("ShortBuddyPrefs", Context.MODE_PRIVATE)
        val masterPrompt = prefs.getString("master_system_prompt", "Analyze this video for a viral animal channel.")
        val automationLevel = prefs.getFloat("automation_level", 0f)

        // 🧠 FEEDBACK LOOP: Format the top 5 winning hooks from the Blackbox for AI study
        val winContext = if (successStories.isEmpty()) {
            "No historical data yet. Use standard viral patterns."
        } else {
            successStories.joinToString("\n") {
                "- WINNING_HOOK: ${it.aiCaption} (Viral Score: ${it.viralScore})"
            }
        }

        val ninjaPrompt = """
            $masterPrompt
            
            CHANNEL_IDENTITY:
            - Brand Voice: ${blueprint.brandVoice}
            - Base Tags: ${blueprint.mandatoryHashtags}
            - Context: ${blueprint.baseDescription}
            
            HISTORICAL_SUCCESS_DATA (Study these winning hooks from our Blackbox):
            $winContext
            
            CONTEXTUAL_INSTRUCTIONS:
            - Automation Level is $automationLevel%.
            - Study the SUCCESS_DATA to identify patterns in captions that work for this specific audience.
            
            Return ONLY these labels with their data:
            DETECTION: [Summarize animal, emotion, motion, lighting, and loop potential]
            CAPTION: [Emoji-rich viral caption mimicking winning brand voice]
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

    /**
     * 🧠 THE LAZY NINJA ANALYTICS SCANNER
     * Scans YouTube Analytics screenshots to determine optimal posting windows.
     */
    suspend fun chewAnalytics(imageBytes: ByteArray): String? = withContext(Dispatchers.IO) {
        val currentKey = keyVault.getActiveKey() ?: return@withContext null
        val model = GenerativeModel(
            modelName = "gemini-3-flash-preview",
            apiKey = currentKey,
            requestOptions = RequestOptions(apiVersion = "v1beta")
        )

        val prompt = "Analyze this YouTube Analytics heatmap. Return ONLY the single peak time for audience activity in HH:mm format (24-hour). Example: 14:00"

        return@withContext try {
            val response = model.generateContent(content {
                blob("image/jpeg", imageBytes)
                text(prompt)
            })
            response.text?.trim()?.take(5)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseNeuralOutput(rawText: String): Map<String, String> {
        // Cleaning up potential Markdown formatting from AI
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