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
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
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
import com.shor.tbuddy.ui.ChannelSettingsActivity
import com.shor.tbuddy.ui.KeyVault
import com.shor.tbuddy.ui.GeminiChewer
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.InputStream

@OptIn(UnstableApi::class)
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var chewer: GeminiChewer
    private var currentProject: ShortsProject? = null
    private var player: ExoPlayer? = null
    private var isMuted = false

    // Tab-specific view references from layout_generate_tabs.xml
    private var etTabCaption: EditText? = null
    private var etTabOverlay: EditText? = null
    private var etTabHashtags: EditText? = null
    private var etTabTags: EditText? = null
    private var etTabPrompt: EditText? = null
    private var tvMusicSuggestion: TextView? = null

    private val videoPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            SlopLogger.info("STAGE_1: Video Staged.")
            currentProject = ShortsProject(rawVideoUri = uri)
            updateNerdWindow("FILE_READY: ${uri.lastPathSegment} | HIT_SCAN_TO_START")

            binding.btnNewProject.text = "[ CONFIRM & SCAN ]"
            binding.btnNewProject.setOnClickListener { startAnalysisFlow() }

            moveToStep(0)
            setupPlayer(binding.analyzePlayerView, uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chewer = GeminiChewer(KeyVault(this))
        setupUI()
        setupNavigation()
        setupTabs()
        setupScheduleGrid()

        SlopLogger.success("System Boot: Logic Aligned.")
        updateNerdWindow("SYSTEM_BOOT: SUCCESS.")
    }

    private fun setupUI() {
        binding.btnNewProject.setOnClickListener { videoPickerLauncher.launch("video/*") }
        binding.btnResetManual.setOnClickListener { resetProject() }

        // 🔧 WRENCH: Opens activity_settings_keys.xml (Vault)
        binding.btnOpenSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnSwoosh.setOnClickListener { deployAndSave() }

        binding.btnMuteToggle.setOnClickListener {
            isMuted = !isMuted
            player?.volume = if (isMuted) 0f else 1f
            binding.btnMuteToggle.text = if (isMuted) "[ UNMUTE ]" else "[ MUTE ]"
        }
    }

    private fun setupNavigation() {
        binding.navigationRail.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.lane_overview -> {
                    moveToStep(0)
                    true
                }
                R.id.lane_engine -> {
                    // ⚙️ GEAR: Opens EngineRoomActivity (Templates/Blueprints)
                    SlopLogger.info("NAV: Entering Engine Room Core")
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                R.id.lane_analytics -> {
                    // 🐾 PAW: Opens ChannelSettingsActivity (Identity/Brand Bible)
                    SlopLogger.info("NAV: Entering Channel Configuration")
                    startActivity(Intent(this, ChannelSettingsActivity::class.java))
                    true
                }
                R.id.lane_performance -> {
                    SlopLogger.info("NAV: Entering Performance Module")
                    startActivity(Intent(this, PerformanceActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun startAnalysisFlow() {
        val uri = currentProject?.rawVideoUri ?: return
        SlopLogger.info("COMMAND: Squeezing and Analyzing...")
        player?.stop()
        player?.release()
        player = null

        wipeUiBuffer()
        updateNerdWindow("PHASE_1.5: CRUSHING_VIDEO")
        moveToStep(2)

        val squeezer = VideoSqueezer(this)

        squeezer.squeeze(uri,
            onComplete = { compressedFile ->
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val db = AppDatabase.getDatabase(this@MainActivity)
                        val topPerformers = db.projectDao().getHighPerformers(80f)

                        val bytes = compressedFile.readBytes()
                        withContext(Dispatchers.Main) {
                            updateNerdWindow("PHASE_2: REQUEST_INIT")
                        }

                        val result = chewer.chewWithRetry(this@MainActivity, bytes, getActiveBlueprint(), topPerformers) { msg ->
                            updateNerdWindow("AI: $msg")
                        }

                        if (result != null) {
                            mapResultToProject(result)
                            withContext(Dispatchers.Main) {
                                applyResultsToViews(result)
                                moveToStep(3)
                            }
                        }
                        compressedFile.delete()
                    } catch (e: Exception) {
                        SlopLogger.error("NEURAL_FLOW_FAIL", e)
                        withContext(Dispatchers.Main) {
                            updateNerdWindow("ERROR: Neural flow failed.")
                        }
                    }
                }
            },
            onError = { e ->
                lifecycleScope.launch(Dispatchers.Main) {
                    updateNerdWindow("SQUEEZE_FAILED")
                }
            }
        )
    }

    private fun setupTabs() {
        val tabView = layoutInflater.inflate(R.layout.layout_generate_tabs, null)
        binding.generateContentFrame.removeAllViews()
        binding.generateContentFrame.addView(tabView)

        etTabCaption = tabView.findViewById(R.id.etTabCaption)
        etTabOverlay = tabView.findViewById(R.id.etTabOverlay)
        etTabHashtags = tabView.findViewById(R.id.etTabHashtags)
        etTabTags = tabView.findViewById(R.id.etTabTags)
        etTabPrompt = tabView.findViewById(R.id.etTabPrompt)
        tvMusicSuggestion = tabView.findViewById(R.id.tvMusicSuggestion)

        val tabFlipper = tabView.findViewById<android.widget.ViewFlipper>(R.id.tabFlipper)
        val tabLayout = binding.generateTabs
        tabLayout.addTab(tabLayout.newTab().setText("CAPTION"))
        tabLayout.addTab(tabLayout.newTab().setText("OVERLAY"))
        tabLayout.addTab(tabLayout.newTab().setText("TAGS"))
        tabLayout.addTab(tabLayout.newTab().setText("SEO"))
        tabLayout.addTab(tabLayout.newTab().setText("MUSIC"))
        tabLayout.addTab(tabLayout.newTab().setText("PROMPT"))

        binding.generateTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) { tabFlipper.displayedChild = tab.position }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        tabView.findViewById<View>(R.id.btnNextToReview).setOnClickListener {
            syncToReviewScreen()
            moveToStep(4)
        }
    }

    private fun wipeUiBuffer() {
        binding.tvAnalyzePanel.text = "WIPING_STALE_BUFFER..."
        etTabCaption?.text = null
        etTabOverlay?.text = null
        etTabHashtags?.text = null
    }

    private fun applyResultsToViews(result: Map<String, String>) {
        binding.tvAnalyzePanel.text = "DETECTION: ${result["detection"]}"
        etTabCaption?.setText(result["caption"])
        etTabOverlay?.setText(result["overlay"])
        etTabHashtags?.setText(result["hashtags"])
        etTabTags?.setText(result["seo"])
        etTabPrompt?.setText(result["prompt"])
        tvMusicSuggestion?.text = "🎵 Suggested: ${result["music"]}"
    }

    private fun mapResultToProject(result: Map<String, String>) {
        currentProject?.apply {
            detectionLog = result["detection"] ?: ""
            activeCaption = result["caption"] ?: ""
            overlayText = result["overlay"] ?: ""
            hashtags = result["hashtags"] ?: ""
            seoTags = result["seo"] ?: ""
            musicGenre = result["music"] ?: ""
            veoPrompt = result["prompt"] ?: ""
        }
    }

    private fun syncToReviewScreen() {
        binding.etFinalCaption.setText(etTabCaption?.text.toString())
        binding.etFinalTags.setText(etTabHashtags?.text.toString())
        currentProject?.rawVideoUri?.let { setupPlayer(binding.reviewPlayerView, it) }
    }

    private fun setupScheduleGrid() {
        val scheduleListener = View.OnClickListener { v ->
            val time = (v as Button).text.toString()
            updateNerdWindow("SCHEDULE_LOCKED: $time")
            v.setBackgroundColor(android.graphics.Color.parseColor("#00FF41"))
            binding.btnSwoosh.text = "POST AT: $time"
        }
        binding.btnSlot1.setOnClickListener(scheduleListener)
        binding.btnSlot2.setOnClickListener(scheduleListener)
        binding.btnSlot3.setOnClickListener(scheduleListener)
        binding.btnSlot4.setOnClickListener(scheduleListener)
    }

    private fun deployAndSave() {
        val project = currentProject ?: return
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
            withContext(Dispatchers.Main) { resetProject() }
        }
    }

    private fun resetProject() {
        player?.stop()
        currentProject = null
        binding.btnNewProject.text = "[ + ] NEW PROJECT"
        binding.btnNewProject.setOnClickListener { videoPickerLauncher.launch("video/*") }
        moveToStep(0)
    }

    private fun moveToStep(index: Int) {
        runOnUiThread { binding.workflowFlipper.displayedChild = index }
    }

    private fun updateNerdWindow(msg: String) {
        runOnUiThread { binding.tvNerdLog.text = "[$msg]" }
    }

    private fun setupPlayer(view: androidx.media3.ui.PlayerView, uri: Uri) {
        player?.release()
        player = ExoPlayer.Builder(this).build().apply {
            view.player = this
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            play()
        }
    }

    private fun getActiveBlueprint() = ChannelBlueprint("lane_animals", "Byte Buds", "Viral", "#shorts", "AI Pets", "")
    override fun onDestroy() { super.onDestroy(); player?.release() }
}
