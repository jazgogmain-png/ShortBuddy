package com.shor.tbuddy.models

/**
 * The 'Brand Bible' for a specific channel.
 */
data class ChannelBlueprint(
    val id: String,              // Unique ID (e.g., "lane_animals")
    val displayName: String,     // e.g., "Premium Paws"
    val brandVoice: String,      // e.g., "Heartwarming, sophisticated, minimal slang"
    val mandatoryHashtags: String, // e.g., "#nature #animals #classy"
    val baseDescription: String,  // Your permanent "About" text
    val linkedAccount: String? = null // The email of the authorized YouTube account
)