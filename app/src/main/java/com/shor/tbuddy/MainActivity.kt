package com.shor.tbuddy

import android.content.ClipData
import android.content.ClipboardManager
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
import com.shor.tbuddy.ui.SettingsActivity
import com.shor.tbuddy.ui.EngineRoomActivity
import com.shor.tbuddy.ui.PerformanceActivity
import com.shor.tbuddy.ui.KeyVault
import kotlinx.coroutines.launch
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var chewer: GeminiChewer
    private var currentProject: ShortsProject? = null
    private var player: ExoPlayer? = null
    private var isMuted = false

    private var etTabCaption: EditText? = null
    private var etTabOverlay: EditText? = null
    private var etTabHashtags: EditText? = null
    private var etTabTags: EditText? = null
    private var etTabPrompt: EditText? = null
    private var tvMusicSuggestion: TextView? = null

    private val videoPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            currentProject = ShortsProject(rawVideoUri = uri)
            updateNerdWindow("PHASE_1: SOURCE_STAGED")
            binding.btnNewProject.text = "[ READY ]\nLAUNCH ANALYSIS"
            binding.btnNewProject.setOnClickListener { startAnalysisFlow() }
            setupPlayer(binding.analyzePlayerView, uri)
        }
    }

    private val analyticsPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            analyzeAnalyticsScreenshot(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chewer = GeminiChewer(KeyVault(this))
        setupTabs()
        setupClickListeners()
        setupNavigation()
        setupScheduleGrid()
    }

    private fun setupClickListeners() {
        binding.btnNewProject.setOnClickListener {
            videoPickerLauncher.launch("video/*")
        }

        // 🧹 MANUAL RESET BUTTON Logic
        binding.btnResetManual.setOnClickListener {
            resetProject()
            Toast.makeText(this, "SYSTEM_CLEARED", Toast.LENGTH_SHORT).show()
        }

        binding.btnOpenSettings.setOnClickListener {
            updateNerdWindow("NAV_EVENT: OPENING_VAULT")
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnSwoosh.setOnClickListener {
            updateNerdWindow("UPLOADING: TRANSMITTING_TO_YOUTUBE")
            Toast.makeText(this, "SWOOSH! TRANSMISSION_SUCCESSFUL 🚀", Toast.LENGTH_SHORT).show()
            // Removed auto-reset from here to keep it manual per your request
        }

        binding.btnMuteToggle?.setOnClickListener {
            isMuted = !isMuted
            player?.volume = if (isMuted) 0f else 1f
            binding.btnMuteToggle?.text = if (isMuted) "[ UNMUTE ]" else "[ MUTE ]"
        }

        binding.btnScanAnalytics.setOnClickListener {
            updateNerdWindow("SYSTEM: STAGING_ANALYTICS_SCAN")
            analyticsPickerLauncher.launch("image/*")
        }
    }

    private fun resetProject() {
        // 1. Kill the video ghost
        player?.stop()
        player?.clearMediaItems()

        // 2. Clear the UI data
        currentProject = null
        binding.tvAnalyzePanel.text = "SCANNING..."
        binding.btnNewProject.text = "[ + ] NEW PROJECT"

        // 3. Clear the tabs
        etTabCaption?.setText("")
        etTabOverlay?.setText("")
        etTabHashtags?.setText("")
        etTabTags?.setText("")
        etTabPrompt?.setText("")
        tvMusicSuggestion?.text = ""

        // 4. Back to Dashboard
        moveToStep(0)
        updateNerdWindow("SYSTEM_RESET: READY_FOR_NEW_INPUT")

        // Re-enable original picker logic
        binding.btnNewProject.setOnClickListener {
            videoPickerLauncher.launch("video/*")
        }
    }

    private fun setupScheduleGrid() {
        val scheduleListener = View.OnClickListener { v ->
            val time = (v as Button).text.toString()
            updateNerdWindow("SCHEDULE_LOCKED: $time")

            binding.btnSlot1.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#121212")))
            binding.btnSlot2.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#121212")))
            binding.btnSlot3.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#121212")))
            binding.btnSlot4.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#121212")))

            v.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#00FF41")))
            v.setTextColor(android.graphics.Color.BLACK)

            binding.btnSwoosh.text = "CONFIRM_SCHEDULE: $time"
        }

        binding.btnSlot1.setOnClickListener(scheduleListener)
        binding.btnSlot2.setOnClickListener(scheduleListener)
        binding.btnSlot3.setOnClickListener(scheduleListener)
        binding.btnSlot4.setOnClickListener(scheduleListener)
    }

    private fun analyzeAnalyticsScreenshot(uri: Uri) {
        updateNerdWindow("PHASE_6: ANALYZING_AUDIENCE_GRAPH")
        lifecycleScope.launch {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: return@launch
            inputStream.close()

            val bestTime = chewer.chewAnalytics(bytes)
            if (bestTime != null) {
                binding.tvBestTime.text = "Best time for your audience: $bestTime 🟢"
                updateNerdWindow("NEURAL_STRATEGY: OPTIMAL_WINDOW_FOUND")
            }
        }
    }

    private fun setupNavigation() {
        binding.navigationRail.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.lane_engine -> {
                    updateNerdWindow("NAV_EVENT: ENTERING_ENGINE_ROOM")
                    startActivity(Intent(this, EngineRoomActivity::class.java))
                    true
                }
                R.id.lane_analytics -> {
                    updateNerdWindow("NAV_EVENT: VIEWING_STATS")
                    startActivity(Intent(this, PerformanceActivity::class.java))
                    true
                }
                R.id.lane_overview -> {
                    moveToStep(0)
                    true
                }
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
            override fun onTabSelected(tab: TabLayout.Tab) {
                tabFlipper.displayedChild = tab.position
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        tabView.findViewById<View>(R.id.btnCopyPrompt).setOnClickListener {
            val prompt = etTabPrompt?.text.toString()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("VeoPrompt", prompt))
            Toast.makeText(this, "PROMPT_COPIED", Toast.LENGTH_SHORT).show()
        }

        tabView.findViewById<View>(R.id.btnNextToReview).setOnClickListener {
            syncToReviewScreen()
            moveToStep(4)
        }

        binding.btnProceedToPublish.setOnClickListener {
            moveToStep(5)
            updateNerdWindow("PHASE_5: READY_FOR_CLOUD_PUSH")
        }
    }

    private fun startAnalysisFlow() {
        val uri = currentProject?.rawVideoUri ?: return
        moveToStep(2)
        updateNerdWindow("PHASE_2: AI_ANALYSIS_ACTIVE")
        setupPlayer(binding.analyzePlayerView, uri)

        lifecycleScope.launch {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: return@launch
            inputStream.close()

            val result = chewer.chewWithRetry(this@MainActivity, bytes) { msg ->
                updateNerdWindow(msg)
            }

            if (result != null) {
                binding.tvAnalyzePanel.text = "DETECTION_LOG:\n${result["detection"]}"

                etTabCaption?.setText(result["caption"])
                etTabOverlay?.setText(result["overlay"])
                etTabHashtags?.setText(result["hashtags"])
                etTabTags?.setText(result["seo"])
                etTabPrompt?.setText(result["prompt"])
                tvMusicSuggestion?.text = "🎵 Suggested: ${result["music"]}"

                updateNerdWindow("PHASE_3: GENERATION_COMPLETE")
                moveToStep(3)
            } else {
                updateNerdWindow("ERROR: NEURAL_STRIKE_FAILED")
                moveToStep(0)
            }
        }
    }

    private fun syncToReviewScreen() {
        binding.etFinalCaption.setText(etTabCaption?.text.toString())
        binding.etFinalTags.setText(etTabHashtags?.text.toString())
        setupPlayer(binding.reviewPlayerView, currentProject?.rawVideoUri!!)
        updateNerdWindow("PHASE_4: REVIEW_SYNC_COMPLETE")
    }

    private fun setupPlayer(view: androidx.media3.ui.PlayerView, uri: Uri) {
        player?.release()
        player = ExoPlayer.Builder(this).build().apply {
            view.player = this
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = Player.REPEAT_MODE_ALL
            volume = if (isMuted) 0f else 1f
            prepare()
            play()
        }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onResume() {
        super.onResume()
        if (binding.workflowFlipper.displayedChild in 2..4) {
            player?.play()
        }
    }

    private fun moveToStep(index: Int) {
        runOnUiThread { binding.workflowFlipper.displayedChild = index }
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