package com.shor.tbuddy.models

data class ChannelTemplate(
    val channelName: String,
    val brandVoice: String,      // e.g., "Professional, heartwarming, sophisticated"
    val baseDescription: String, // Your "About the Channel" text
    val mandatoryTags: List<String>,
    val targetAudience: String,
    val youtubeAccountEmail: String // For multi-account switching
)