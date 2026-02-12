package com.shor.tbuddy

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.*
import com.shor.tbuddy.models.ShortsMetadata
import com.shor.tbuddy.models.ChannelTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiChewer(private val keyVault: KeyVault) {

    suspend fun chewWithRetry(videoBytes: ByteArray, template: ChannelTemplate): ShortsMetadata? {
        val currentKey = keyVault.getNextKey() ?: return null

        return try {
            performChew(currentKey, videoBytes, template)
        } catch (e: Exception) {
            SlopLogger.error("CHEWER: Strike Failed: ${e.localizedMessage}")
            null
        }
    }

    private suspend fun performChew(apiKey: String, videoBytes: ByteArray, template: ChannelTemplate): ShortsMetadata = withContext(Dispatchers.IO) {

        val model = GenerativeModel(
            modelName = "gemini-3-flash-preview", // Kept your specific model
            apiKey = apiKey,
            requestOptions = RequestOptions(apiVersion = "v1beta") // Kept your v1beta requirement
        )

        val ninjaPrompt = """
            You are the Master Creative Director for a Viral AI Animal Channel. 
            Analyze the video and return ONLY these labels with no markdown, no asterisks, and no extra text:
            
            STYLE_MELT: [Emotional caption]
            STYLE_ADOPT: [Adoption question]
            STYLE_COZY: [Peaceful caption]
            OVERLAY: [5 word max overlay text]
            VEO_PROMPT: [Detailed 9:16 prompt]
            VIRAL_TAGS: [tag1, tag2, tag3]
        """.trimIndent()

        val response = model.generateContent(
            content {
                blob("video/mp4", videoBytes)
                text(ninjaPrompt)
            }
        )

        val rawText = response.text ?: ""
        parseNinjaMetadata(rawText)
    }

    private fun parseNinjaMetadata(rawText: String): ShortsMetadata {
        // Clean up markdown before parsing (Removes ** and ##)
        val cleanText = rawText.replace("**", "").replace("##", "")
        val lines = cleanText.lines()

        fun extract(key: String) = lines.find { it.contains(key, ignoreCase = true) }
            ?.substringAfter(":")?.trim() ?: ""

        val title = extract("STYLE_MELT")
        val adopt = extract("STYLE_ADOPT")
        val cozy = extract("STYLE_COZY")
        val overlay = extract("OVERLAY")
        val veo = extract("VEO_PROMPT")
        val tags = extract("VIRAL_TAGS").split(",").map { it.trim() }

        // Stashing all styles in the description so MainActivity can split them later
        val combinedDesc = "OVERLAY: $overlay | ADOPT: $adopt | COZY: $cozy | VEO: $veo"

        return ShortsMetadata(
            title = if (title.isEmpty()) "Cute Baby" else title,
            description = combinedDesc,
            tags = tags
        )
    }
}