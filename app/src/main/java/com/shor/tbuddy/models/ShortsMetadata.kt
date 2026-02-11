package com.shor.tbuddy.models

/**
 * The data structure for our "Chewed" video data.
 */
data class ShortsMetadata(
    val title: String,
    val description: String,
    val tags: List<String>,
    val suggestedMusicStyle: String = "Upbeat"
)