package com.shor.tbuddy.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * THE BLACKBOX ARCHIVE
 * This entity persists every AI 'chew' and performance metric.
 */
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val videoUri: String,
    val timestamp: Long = System.currentTimeMillis(),

    // 🧠 AI BRAIN DATA (Synced with GeminiChewer output)
    val detectionLog: String,
    val aiCaption: String,
    val aiOverlay: String,
    val aiHashtags: String,
    val aiSeoTags: String,   // 👈 New: For YouTube algorithm optimization
    val aiPrompt: String,    // The Veo/Image generation prompt
    val aiMusic: String,     // Recommended mood/genre

    // 📈 PERFORMANCE DATA
    val scheduledTime: String? = null,
    val bestPostingTime: String? = null, // 👈 New: From Analytics screenshot scan
    val viewCount: Int = 0,
    val viralScore: Float = 0f,

    // 🏗️ SYSTEM ARCHITECTURE
    val templateId: String = "DEFAULT",      // 👈 New: Which blueprint was used?
    val targetChannelId: String = "PRIMARY"  // 👈 New: Which YouTube account?
)