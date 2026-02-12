package com.shor.tbuddy

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.shor.tbuddy.databinding.ActivityMainBinding
import com.shor.tbuddy.models.ShortsProject
import com.shor.tbuddy.models.ChannelTemplate
import com.shor.tbuddy.ui.SettingsActivity
import com.shor.tbuddy.ui.EngineRoomActivity
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var chewer: GeminiChewer
    private lateinit var squeezer: VideoSqueezer

    private var currentProject: ShortsProject? = null
    private var player: ExoPlayer? = null
    private var currentStyleIndex = 0 // 0: Melt, 1: Adopt, 2: Cozy

    private val videoPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            currentProject = ShortsProject(rawVideoUri = uri)
            moveToStep(1) // Step 2: Analyze
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
        binding.btnNewProject.setOnClickListener { videoPickerLauncher.launch("video/*") }

        // GEAR ICON: The Vault
        binding.btnOpenSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // VEO CLIPBOARD: The Factory Loop
        binding.btnCopyVeo.setOnClickListener {
            val prompt = currentProject?.veoPrompt ?: return@setOnClickListener
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Veo", prompt))
            Toast.makeText(this, "Master Prompt Clipped! 🎥", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupNavigation() {
        binding.navigationRail.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.lane_engine -> {
                    startActivity(Intent(this, EngineRoomActivity::class.java))
                    true
                }
                R.id.lane_overview -> {
                    moveToStep(0)
                    true
                }
                R.id.lane_cuteness -> {
                    cycleViralStyles() // THE PAW ACTION
                    true
                }
                else -> false
            }
        }
    }

    private fun cycleViralStyles() {
        val project = currentProject ?: return
        if (binding.workflowFlipper.displayedChild != 2) return

        currentStyleIndex = (currentStyleIndex + 1) % 3

        val newCaption = when(currentStyleIndex) {
            0 -> project.captionMelt
            1 -> project.captionAdopt
            else -> project.captionCozy
        }

        project.activeCaption = newCaption
        binding.etTitle.setText(newCaption)
        updateNerdWindow("STYLE: Switched to Lane ${currentStyleIndex + 1}")
    }

    private fun runG3Analysis(file: File) {
        lifecycleScope.launch {
            updateNerdWindow("CHEWER: Un-bundling Ninja Data...")
            val template = ChannelTemplate("Lola", "Viral", "", listOf("cute"), "Global", "")
            val result = chewer.chewWithRetry(file.readBytes(), template)

            if (result != null) {
                currentProject?.apply {
                    // Extracting the clean data we formatted in GeminiChewer
                    val data = result.description
                    captionMelt = result.title
                    captionAdopt = data.substringAfter("ADOPT: ").substringBefore(" | COZY:")
                    captionCozy = data.substringAfter("COZY: ").substringBefore(" | VEO:")
                    veoPrompt = data.substringAfter("VEO: ")

                    activeCaption = captionMelt
                    overlayText = data.substringAfter("OVERLAY: ").substringBefore(" | ADOPT:")
                    hashtags = result.tags.joinToString(" ")
                }
                populateReviewScreen()
                moveToStep(2) // Step 3: Review
            }
        }
    }

    private fun populateReviewScreen() {
        val project = currentProject ?: return
        runOnUiThread {
            // No more clobbered text!
            binding.tvViralOverlay.text = project.overlayText
            binding.etTitle.setText(project.activeCaption)
            binding.etHashtags.setText(project.hashtags)
            setupExoPlayer(project.rawVideoUri!!)
        }
    }

    private fun moveToStep(stepIndex: Int) {
        runOnUiThread { binding.workflowFlipper.displayedChild = stepIndex }
    }

    private fun startProcessing() {
        val uri = currentProject?.rawVideoUri ?: return
        updateNerdWindow("SQUEEZER: Preparing slop for G3...")
        squeezer.squeeze(uri,
            onComplete = { file ->
                currentProject = currentProject?.copy(squeezedFile = file)
                runG3Analysis(file)
            },
            onError = { moveToStep(0) }
        )
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