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
        SlopLogger.info("PHASE 1: AWAKENING THE GATEKEEPER. Pool: $totalKeys")

        for (i in 0 until totalKeys) {
            val currentKey = keyVault.getNextKey() ?: break
            try {
                return performChew(currentKey, videoBytes, template)
            } catch (e: Exception) {
                SlopLogger.error("KEY FAIL: Rotating pool...", e)
            }
        }
        return null
    }

    private suspend fun performChew(apiKey: String, videoBytes: ByteArray, template: ChannelTemplate): ShortsMetadata = withContext(Dispatchers.IO) {
        val model = GenerativeModel(
            modelName = "gemini-3-flash-preview",
            apiKey = apiKey,
            safetySettings = listOf(
                SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE)
            )
        )

        val masterPrompt = """
            Act as a Senior Social Media Strategist for the '${template.channelName}' brand.
            
            BRAND VOICE: ${template.brandVoice}
            TARGET AUDIENCE: ${template.targetAudience}
            
            Analyze this video and return strictly in English:
            Title: [Classy Viral Title]
            Description: [2-sentence hook] + [Appended: ${template.baseDescription}]
            Tags: [${template.mandatoryTags.joinToString(", ")}, AI_Generated_Tag1, AI_Generated_Tag2]
        """.trimIndent()

        val response = model.generateContent(
            content {
                blob("video/mp4", videoBytes)
                text(masterPrompt)
            }
        )

        parseMetadata(response.text ?: "")
    }

    // FIXED: Moved outside of performChew so it can be private
    private fun parseMetadata(rawText: String): ShortsMetadata {
        val lines = rawText.lines()
        val title = lines.find { it.startsWith("Title:") }?.removePrefix("Title:")?.trim() ?: "Viral Clip"
        val desc = lines.find { it.startsWith("Description:") }?.removePrefix("Description:")?.trim() ?: ""
        val tags = lines.find { it.startsWith("Tags:") }?.removePrefix("Tags:")?.trim()?.split(",") ?: emptyList()

        return ShortsMetadata(
            title = title,
            description = desc,
            tags = tags.map { it.trim() }
        )
    }
}