package com.shor.tbuddy

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.google.android.material.tabs.TabLayout
import com.shor.tbuddy.databinding.ActivityMainBinding
import com.shor.tbuddy.models.ShortsProject
import com.shor.tbuddy.models.ChannelBlueprint
import com.shor.tbuddy.models.ProjectEntity
import com.shor.tbuddy.database.AppDatabase
import com.shor.tbuddy.ui.SettingsActivity
import com.shor.tbuddy.ui.EngineRoomActivity
import com.shor.tbuddy.ui.PerformanceActivity
import com.shor.tbuddy.ui.KeyVault
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var chewer: GeminiChewer
    private var currentProject: ShortsProject? = null
    private var player: ExoPlayer? = null
    private var isMuted = false

    // Tab-specific view references
    private var etTabCaption: EditText? = null
    private var etTabOverlay: EditText? = null
    private var etTabHashtags: EditText? = null
    private var etTabTags: EditText? = null
    private var etTabPrompt: EditText? = null
    private var tvMusicSuggestion: TextView? = null

    private val videoPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            currentProject = ShortsProject(rawVideoUri = uri)
            updateNerdWindow("PHASE_1: SOURCE_STAGED >> ${uri.lastPathSegment}")

            binding.btnNewProject.text = "[ READY ]\nLAUNCH ANALYSIS"
            binding.btnNewProject.setOnClickListener { startAnalysisFlow() }

            moveToStep(2)
            setupPlayer(binding.analyzePlayerView, uri)
        }
    }

    private val analyticsPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { analyzeAnalyticsScreenshot(uri) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chewer = GeminiChewer(KeyVault(this))
        setupTabs()
        setupUI()
        setupNavigation()
        setupScheduleGrid()

        updateNerdWindow("SYSTEM_BOOT: SUCCESS. READY_FOR_INPUT.")
    }

    private fun setupUI() {
        binding.btnNewProject.setOnClickListener { videoPickerLauncher.launch("video/*") }
        binding.btnResetManual.setOnClickListener { resetProject() }
        binding.btnOpenSettings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        binding.btnSwoosh.setOnClickListener { deployAndSave() }
        binding.btnScanAnalytics.setOnClickListener { analyticsPickerLauncher.launch("image/*") }

        binding.btnMuteToggle.setOnClickListener {
            isMuted = !isMuted
            player?.volume = if (isMuted) 0f else 1f
            binding.btnMuteToggle.text = if (isMuted) "[ UNMUTE ]" else "[ MUTE ]"
        }
    }

    private fun startAnalysisFlow() {
        val uri = currentProject?.rawVideoUri ?: return
        updateNerdWindow("PHASE_2: AI_ANALYSIS_ACTIVE")
        binding.tvAnalyzePanel.text = "CONNECTING_TO_GEMINI_3... 🧠"

        lifecycleScope.launch {
            try {
                updateNerdWindow("FETCHING_BLACKBOX_CONTEXT...")
                val db = AppDatabase.getDatabase(this@MainActivity)
                val topPerformers = withContext(Dispatchers.IO) {
                    db.projectDao().getHighPerformers(80f)
                }

                updateNerdWindow("EXTRACTING_VIDEO_BYTES...")
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@launch
                inputStream.close()

                updateNerdWindow("UPLOADING_TO_NEURAL_ENGINE... (EXPECT_LAG)")
                val result = chewer.chewWithRetry(this@MainActivity, bytes, getActiveBlueprint(), topPerformers) { msg ->
                    updateNerdWindow("AI_FEEDBACK: $msg")
                }

                if (result != null) {
                    currentProject?.apply {
                        detectionLog = result["detection"] ?: ""
                        activeCaption = result["caption"] ?: ""
                        overlayText = result["overlay"] ?: ""
                        hashtags = result["hashtags"] ?: ""
                        seoTags = result["seo"] ?: ""
                        musicGenre = result["music"] ?: ""
                        veoPrompt = result["prompt"] ?: ""
                    }

                    withContext(Dispatchers.Main) {
                        updateNerdWindow("DATA_STREAM_RECEIVED: POPULATING_TABS")
                        binding.tvAnalyzePanel.text = "DETECTION: ${result["detection"]}"
                        etTabCaption?.setText(result["caption"])
                        etTabOverlay?.setText(result["overlay"])
                        etTabHashtags?.setText(result["hashtags"])
                        etTabTags?.setText(result["seo"])
                        etTabPrompt?.setText(result["prompt"])
                        tvMusicSuggestion?.text = "🎵 Suggested: ${result["music"]}"

                        updateNerdWindow("PHASE_3: GENERATION_COMPLETE. WAITING_FOR_USER_EDITS.")
                        moveToStep(3)
                    }
                } else {
                    updateNerdWindow("CRITICAL_FAULT: AI_RETURNED_NULL")
                    binding.tvAnalyzePanel.text = "ANALYSIS_FAULT: RESTARTING..."
                }
            } catch (e: Exception) {
                updateNerdWindow("EXCEPTION: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun deployAndSave() {
        val project = currentProject ?: return
        updateNerdWindow("PHASE_4: PACKAGING_PROJECT_FOR_BLACKBOX")
        lifecycleScope.launch(Dispatchers.IO) {
            val entity = ProjectEntity(
                videoUri = project.rawVideoUri.toString(),
                detectionLog = project.detectionLog,
                aiCaption = etTabCaption?.text.toString(),
                aiOverlay = etTabOverlay?.text.toString(),
                aiHashtags = etTabHashtags?.text.toString(),
                aiSeoTags = etTabTags?.text.toString(),
                aiPrompt = etTabPrompt?.text.toString(),
                aiMusic = tvMusicSuggestion?.text.toString(),
                bestPostingTime = project.bestPostingTime,
                viralScore = 0f
            )
            AppDatabase.getDatabase(this@MainActivity).projectDao().insertProject(entity)

            withContext(Dispatchers.Main) {
                updateNerdWindow("PHASE_5: DATA_LOCKED. SWOOSH_DEPLOYED.")
                Toast.makeText(this@MainActivity, "Transmission Success 🚀", Toast.LENGTH_SHORT).show()
                resetProject()
            }
        }
    }

    private fun resetProject() {
        player?.stop()
        currentProject = null
        binding.btnNewProject.text = "[ + ] NEW PROJECT"
        moveToStep(0)
        updateNerdWindow("SYSTEM_RESET: READY_FOR_NEXT_BYTE_BUDS_SHORT")
        binding.btnNewProject.setOnClickListener { videoPickerLauncher.launch("video/*") }
    }

    private fun setupScheduleGrid() {
        val scheduleListener = View.OnClickListener { v ->
            val time = (v as Button).text.toString()
            updateNerdWindow("SCHEDULE_LOCK_IN: $time")
            v.setBackgroundColor(android.graphics.Color.parseColor("#00FF41"))
            binding.btnSwoosh.text = "SWOOSH AT: $time"
        }
        binding.btnSlot1.setOnClickListener(scheduleListener)
        binding.btnSlot2.setOnClickListener(scheduleListener)
        binding.btnSlot3.setOnClickListener(scheduleListener)
        binding.btnSlot4.setOnClickListener(scheduleListener)
    }

    private fun analyzeAnalyticsScreenshot(uri: Uri) {
        updateNerdWindow("SCANNING_ANALYTICS_IMAGE...")
        lifecycleScope.launch {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: return@launch
            val bestTime = chewer.chewAnalytics(bytes)
            if (bestTime != null) {
                currentProject?.bestPostingTime = bestTime
                binding.tvBestTime.text = "Best time: $bestTime 🕒"
                updateNerdWindow("ANALYTICS_SYNCED: TARGET_TIME=$bestTime")
            }
        }
    }

    private fun setupNavigation() {
        binding.navigationRail.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.lane_engine -> { startActivity(Intent(this, EngineRoomActivity::class.java)); true }
                R.id.lane_analytics -> { startActivity(Intent(this, PerformanceActivity::class.java)); true }
                R.id.lane_overview -> { moveToStep(0); true }
                else -> false
            }
        }
    }

    private fun setupTabs() {
        val tabs = binding.generateTabs
        tabs.addTab(tabs.newTab().setText("Caption"))
        tabs.addTab(tabs.newTab().setText("Overlay"))
        tabs.addTab(tabs.newTab().setText("Hashtags"))
        tabs.addTab(tabs.newTab().setText("Tags"))
        tabs.addTab(tabs.newTab().setText("Music"))
        tabs.addTab(tabs.newTab().setText("Prompt"))

        val tabView = layoutInflater.inflate(R.layout.layout_generate_tabs, null)
        binding.generateContentFrame.addView(tabView)

        val tabFlipper = tabView.findViewById<android.widget.ViewFlipper>(R.id.tabFlipper)
        etTabCaption = tabView.findViewById(R.id.etTabCaption)
        etTabOverlay = tabView.findViewById(R.id.etTabOverlay)
        etTabHashtags = tabView.findViewById(R.id.etTabHashtags)
        etTabTags = tabView.findViewById(R.id.etTabTags)
        etTabPrompt = tabView.findViewById(R.id.etTabPrompt)
        tvMusicSuggestion = tabView.findViewById(R.id.tvMusicSuggestion)

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) { tabFlipper.displayedChild = tab.position }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        tabView.findViewById<View>(R.id.btnNextToReview).setOnClickListener {
            syncToReviewScreen()
            moveToStep(4)
        }

        binding.btnProceedToPublish.setOnClickListener { moveToStep(5) }
    }

    private fun syncToReviewScreen() {
        binding.etFinalCaption.setText(etTabCaption?.text.toString())
        binding.etFinalTags.setText(etTabHashtags?.text.toString())
        currentProject?.rawVideoUri?.let { setupPlayer(binding.reviewPlayerView, it) }
    }

    private fun setupPlayer(view: androidx.media3.ui.PlayerView, uri: Uri) {
        player?.release()
        player = ExoPlayer.Builder(this).build().apply {
            view.player = this
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = Player.REPEAT_MODE_ALL
            prepare()
            play()
        }
    }

    private fun getActiveBlueprint() = ChannelBlueprint("default", "Byte Buds", "Viral", "#shorts", "AI Pets")

    private fun moveToStep(index: Int) {
        runOnUiThread {
            binding.workflowFlipper.displayedChild = index
        }
    }

    private fun updateNerdWindow(msg: String) {
        runOnUiThread {
            binding.tvNerdLog.text = "[$msg]"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}