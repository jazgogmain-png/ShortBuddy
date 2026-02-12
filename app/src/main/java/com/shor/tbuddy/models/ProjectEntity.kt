package com.shor.tbuddy.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val videoUri: String,
    val timestamp: Long = System.currentTimeMillis(),

    // 🧠 AI BRAIN DATA
    val detectionLog: String,
    val aiCaption: String,
    val aiOverlay: String,
    val aiHashtags: String,
    val aiPrompt: String,
    val aiMusic: String,

    // 📈 PERFORMANCE DATA
    val scheduledTime: String? = null,
    val viewCount: Int = 0,
    val viralScore: Float = 0f
)