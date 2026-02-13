package com.shor.tbuddy.models

import android.net.Uri
import java.io.File
import java.util.UUID

/**
 * The 'Suitcase' for the Byte Buds workflow.
 * Carries data from initial input to final YouTube upload.
 */
data class ShortsProject(
    val id: String = UUID.randomUUID().toString(),
    val rawVideoUri: Uri?,
    var squeezedFile: File? = null,

    // 🧠 AI ENGINE DATA (The "Chew")
    var detectionLog: String = "",
    var activeCaption: String = "",
    var overlayText: String = "",
    var hashtags: String = "",
    var seoTags: String = "",
    var musicGenre: String = "",
    var veoPrompt: String = "",

    // 📊 PERFORMANCE & ANALYTICS DATA
    var viralityScore: Int = 0,
    var bestPostingTime: String = "08:00", // Extracted from screenshot
    var targetChannelId: String = "PRIMARY",

    // STATUS FLAGS
    var isDeployed: Boolean = false
)