package com.shor.tbuddy

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
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
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentProject: ShortsProject? = null
    private var player: ExoPlayer? = null

    // Reference to inflated tab views
    private var etTabCaption: EditText? = null
    private var etTabOverlay: EditText? = null
    private var etTabHashtags: EditText? = null
    private var etTabTags: EditText? = null
    private var etTabPrompt: EditText? = null

    private val videoPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            currentProject = ShortsProject(rawVideoUri = uri)
            updateNerdWindow("PHASE_1: SOURCE_STAGED")
            binding.btnNewProject.text = "[ READY ]\nLAUNCH ANALYSIS"
            binding.btnNewProject.setOnClickListener { startAnalysisFlow() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTabs()
        binding.btnNewProject.setOnClickListener { videoPickerLauncher.launch("video/*") }
    }

    private fun setupTabs() {
        val tabs = binding.generateTabs
        tabs.addTab(tabs.newTab().setText("Caption"))
        tabs.addTab(tabs.newTab().setText("Overlay"))
        tabs.addTab(tabs.newTab().setText("Hashtags"))
        tabs.addTab(tabs.newTab().setText("Tags"))
        tabs.addTab(tabs.newTab().setText("Music"))
        tabs.addTab(tabs.newTab().setText("Prompt"))

        // Inflate the separate tab layout into the frame
        val tabView = layoutInflater.inflate(R.layout.layout_generate_tabs, null)
        binding.generateContentFrame.addView(tabView)

        val tabFlipper = tabView.findViewById<android.widget.ViewFlipper>(R.id.tabFlipper)

        // Connect UI elements from inflated layout
        etTabCaption = tabView.findViewById(R.id.etTabCaption)
        etTabOverlay = tabView.findViewById(R.id.etTabOverlay)
        etTabHashtags = tabView.findViewById(R.id.etTabHashtags)
        etTabTags = tabView.findViewById(R.id.etTabTags)
        etTabPrompt = tabView.findViewById(R.id.etTabPrompt)

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tabFlipper.displayedChild = tab.position
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Copy Veo Prompt Logic
        tabView.findViewById<View>(R.id.btnCopyPrompt).setOnClickListener {
            val prompt = etTabPrompt?.text.toString()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("VeoPrompt", prompt))
            Toast.makeText(this, "PROMPT_COPIED", Toast.LENGTH_SHORT).show()
        }

        // Proceed to Phase 4
        tabView.findViewById<View>(R.id.btnNextToReview).setOnClickListener {
            syncToReviewScreen()
            moveToStep(3)
        }
    }

    private fun startAnalysisFlow() {
        moveToStep(1) // ANALYZE SCREEN
        updateNerdWindow("PHASE_2: AI_ANALYSIS_ACTIVE")
        setupPlayer(binding.analyzePlayerView, currentProject?.rawVideoUri!!)

        // Placeholder for G3 Analysis Trigger
        // For now, let's pretend it finishes and moves to Phase 3 after a delay
        binding.tvAnalyzePanel.postDelayed({
            updateNerdWindow("PHASE_3: GENERATION_COMPLETE")
            moveToStep(2) // GENERATE SCREEN
        }, 2000)
    }

    private fun syncToReviewScreen() {
        // Carry over everything from the tabs to the final review screen
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
            prepare()
            play()
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