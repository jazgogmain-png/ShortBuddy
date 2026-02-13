package com.shor.tbuddy.ui

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.RequestOptions
import com.google.ai.client.generativeai.type.content
import com.shor.tbuddy.SlopLogger
import com.shor.tbuddy.models.ChannelBlueprint
import com.shor.tbuddy.models.ProjectEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlinx.serialization.json.Json

class GeminiChewer(private val keyVault: KeyVault) {

    suspend fun chewWithRetry(
        context: Context,
        videoBytes: ByteArray,
        blueprint: ChannelBlueprint,
        history: List<ProjectEntity>,
        onProgress: (String) -> Unit
    ): Map<String, String>? = withContext(Dispatchers.IO) {

        val currentKey = keyVault.getApiKey()
        SlopLogger.keyRotation(0, currentKey)

        try {
            onProgress("LOLA_DEBUG: PACKAGING_PAYLOAD")
            SlopLogger.info("Payload Size: ${videoBytes.size} bytes")

            val nonce = UUID.randomUUID().toString().take(8)

            val prompt = """
                Task: Analyze video for the channel '${blueprint.displayName}'.
                Reference ID: $nonce
                Brand Voice: ${blueprint.brandVoice}
                Base Strategy: ${blueprint.baseDescription}
                Mandatory Tags: ${blueprint.mandatoryHashtags}
                ${if (history.isNotEmpty()) "Context: Last viral hook was '${history.first().aiCaption}'" else ""}
                
                Return JSON only:
                {
                  "detection": "detailed analysis of subject and motion",
                  "caption": "viral hook in brand voice",
                  "overlay": "text for screen",
                  "hashtags": "3-5 relevant tags plus mandatory ones",
                  "seo": "tags for search",
                  "music": "style fitting the voice",
                  "prompt": "veo prompt"
                }
            """.trimIndent()

            SlopLogger.info("Dispatching Neural Request [ID: $nonce] for ${blueprint.displayName}")

            val generativeModel = GenerativeModel(
                modelName = "gemini-3-flash-preview",
                apiKey = currentKey,
                requestOptions = RequestOptions(apiVersion = "v1beta")
            )

            val inputContent = content {
                part(com.google.ai.client.generativeai.type.BlobPart("video/mp4", videoBytes))
                text(prompt)
            }

            val response = generativeModel.generateContent(inputContent)

            SlopLogger.success("Neural Response Received for $nonce")
            val responseText = response.text?.replace("```json", "")?.replace("```", "")?.trim()
            val successResult = Json.decodeFromString<Map<String, String>>(responseText ?: "{}")

            return@withContext successResult

        } catch (e: Exception) {
            SlopLogger.error("Neural Engine Breakdown during chew", e)
            return@withContext null
        }
    }
}
