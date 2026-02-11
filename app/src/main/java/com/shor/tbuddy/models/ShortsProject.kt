package com.shor.tbuddy.models

import android.net.Uri
import java.io.File

data class ShortsProject(
    val id: String = java.util.UUID.randomUUID().toString(),
    val rawVideoUri: Uri?,
    var squeezedFile: File? = null,
    var aiAnalysis: String = "",
    var viralityScore: Int = 0,
    var caption: String = "",
    var overlayText: String = "",
    var hashtags: String = "",
    var isDeployed: Boolean = false
)