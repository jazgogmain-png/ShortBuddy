package com.shor.tbuddy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.shor.tbuddy.databinding.ActivityMainBinding
import com.shor.tbuddy.models.ShortsProject
import com.shor.tbuddy.models.ChannelTemplate
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var chewer: GeminiChewer
    private lateinit var squeezer: VideoSqueezer

    private var currentProject: ShortsProject? = null
    private var player: ExoPlayer? = null

    private val videoPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            currentProject = ShortsProject(rawVideoUri = uri)
            moveToStep(1) // ProgressBar room
            startProcessing()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chewer = GeminiChewer(KeyVault(this))
        squeezer = VideoSqueezer(this)

        setupClickListeners()
        setupNavigation()
    }

    private fun setupClickListeners() {
        binding.btnNewProject.setOnClickListener {
            videoPickerLauncher.launch("video/*")
        }

        binding.btnSwoosh.setOnClickListener {
            updateNerdWindow("SYSTEM: Swooshing to YouTube Drafts...")
        }
    }

    private fun setupNavigation() {
        binding.navigationRail.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.lane_engine -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
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

    private fun moveToStep(stepIndex: Int) {
        runOnUiThread {
            binding.workflowFlipper.displayedChild = stepIndex
        }
    }

    private fun startProcessing() {
        val uri = currentProject?.rawVideoUri ?: return

        updateNerdWindow("CONSOLE: Phase 1 - Hardware Squeeze...")
        squeezer.squeeze(uri,
            onComplete = { file ->
                currentProject = currentProject?.copy(squeezedFile = file)
                runG3Analysis(file)
            },
            onError = { e ->
                updateNerdWindow("🛑 SQUEEZE_ERROR: ${e.message}")
                moveToStep(0)
            }
        )
    }

    private fun runG3Analysis(file: File) {
        lifecycleScope.launch {
            updateNerdWindow("CONSOLE: Phase 2 - G3 Brainstorming...")

            val template = ChannelTemplate("Lola", "Viral", "", listOf("cute"), "Global", "")
            val result = chewer.chewWithRetry(file.readBytes(), template)

            if (result != null) {
                // Fixed: Using .apply to modify fields (requires 'var' in ShortsProject)
                currentProject?.apply {
                    caption = result.title
                    overlayText = result.description
                    hashtags = result.tags.joinToString(" ")
                    viralityScore = 87
                }
                populateReviewScreen()
                moveToStep(2)
            } else {
                updateNerdWindow("🛑 G3_ERROR: Analysis failed.")
                moveToStep(0)
            }
        }
    }

    private fun populateReviewScreen() {
        val project = currentProject ?: return

        runOnUiThread {
            // Corrected IDs based on your XML
            binding.tvViralOverlay.text = project.overlayText
            binding.etTitle.setText(project.caption)
            binding.etHashtags.setText(project.hashtags)

            setupExoPlayer(project.rawVideoUri!!)
            updateNerdWindow("CONSOLE: Phase 3 - Awaiting Review.")
        }
    }

    private fun setupExoPlayer(uri: Uri) {
        player?.release()
        player = ExoPlayer.Builder(this).build().apply {
            binding.playerView.player = this
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = Player.REPEAT_MODE_ALL
            prepare()
            play()
        }
    }

    private fun updateNerdWindow(msg: String) {
        runOnUiThread {
            binding.tvNerdLog.append("\n> $msg")
            binding.svNerdWindow.post { binding.svNerdWindow.fullScroll(View.FOCUS_DOWN) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}