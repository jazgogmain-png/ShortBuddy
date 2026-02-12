package com.shor.tbuddy.models

import android.net.Uri
import java.io.File

data class ShortsProject(
    val id: String = java.util.UUID.randomUUID().toString(),
    val rawVideoUri: Uri?,
    var squeezedFile: File? = null,
    var viralityScore: Int = 0,
    // THE STYLES
    var captionMelt: String = "",
    var captionAdopt: String = "",
    var captionCozy: String = "",
    var activeCaption: String = "", // The one currently selected
    var overlayText: String = "",
    var veoPrompt: String = "",
    var hashtags: String = "",
    var isDeployed: Boolean = false
)