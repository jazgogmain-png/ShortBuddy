package com.shor.tbuddy.models

/**
 * Defines a specific viral strategy (e.g., Adoption Hook, Cozy Comfort)
 */
data class ViralTemplate(
    val name: String,         // "🥹 Emotional Melt"
    val systemPrompt: String, // The specific G3 instruction for this vibe
    val coreEmoji: String,    // "🐾✨"
    val isActive: Boolean = true
)