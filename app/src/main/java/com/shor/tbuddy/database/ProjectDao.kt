package com.shor.tbuddy.database

import androidx.room.*
import com.shor.tbuddy.models.ProjectEntity
import kotlinx.coroutines.flow.Flow

/**
 * THE NEURAL INTERFACE
 * Handles all communication between the app and the Blackbox Archive.
 */
@Dao
interface ProjectDao {

    // 📂 DASHBOARD: Get everything for the Recent Projects list
    @Query("SELECT * FROM projects ORDER BY timestamp DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    // 📈 THE BRAIN FEED: Fetch the 'Success Stories' to train the AI
    // Pulls the top 5 videos with a high viral score to use as context
    @Query("SELECT * FROM projects WHERE viralScore >= :minScore ORDER BY viralScore DESC LIMIT 5")
    suspend fun getHighPerformers(minScore: Float = 80f): List<ProjectEntity>

    // 🔍 SEO LOOKUP: Find the exact tags and captions that worked before
    @Query("SELECT aiCaption FROM projects WHERE viralScore >= 90 LIMIT 3")
    suspend fun getWinningCaptions(): List<String>

    // 🛠️ PROJECT MANAGEMENT
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    // 💣 THE NUKE: For when you want to reset a channel experiment
    @Query("DELETE FROM projects")
    suspend fun nukeBlackbox()

    // 🎯 SPECIFIC LOOKUP: To load a project back into the workflow
    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProjectById(projectId: Int): ProjectEntity?
}