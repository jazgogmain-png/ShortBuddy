package com.shor.tbuddy.ui

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.shor.tbuddy.databinding.ActivitySettingsBinding
import com.shor.tbuddy.SlopLogger

class EngineRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val loopRule = "Vertical 9:16. Ensure seamless looping by ending mid-blink."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("ShortBuddyPrefs", Context.MODE_PRIVATE)

        // 1. LOAD VIRAL DNA
        binding.etMasterPrompt.setText(prefs.getString("master_system_prompt", ""))
        binding.etStaticTags.setText(prefs.getString("static_tags", "#shorts #babyanimals"))
        binding.sliderAutomation.value = prefs.getFloat("automation_level", 0f)

        // 2. ACTIVATE VEO FACTORY
        setupVeoFactory()

        // 3. SAVE LOGIC
        binding.btnSaveSettings.setOnClickListener {
            val newPrompt = binding.etMasterPrompt.text.toString()
            val newTags = binding.etStaticTags.text.toString()
            val newLevel = binding.sliderAutomation.value

            prefs.edit().apply {
                putString("master_system_prompt", newPrompt)
                putString("static_tags", newTags)
                putFloat("automation_level", newLevel)
                apply()
            }

            SlopLogger.success("ENGINE: Viral DNA Locked. Auto-Level: $newLevel%")
            Toast.makeText(this, "Engine Engaged 🦾", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupVeoFactory() {
        binding.btnGenKitten.setOnClickListener { copyToClipboard("Baby kitten, big glossy eyes, fluffy, $loopRule") }
        binding.btnGenPuppy.setOnClickListener { copyToClipboard("Baby puppy, oversized eyes, soft fur, $loopRule") }
        binding.btnGenFox.setOnClickListener { copyToClipboard("Baby fox, sparkling eyes, fluffy tail, $loopRule") }

        binding.btnGenCustom.setOnClickListener {
            val input = EditText(this)
            input.hint = "e.g. Red Panda"
            AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("CUSTOM ANIMAL")
                .setView(input)
                .setPositiveButton("GENERATE") { _, _ ->
                    val animal = input.text.toString()
                    copyToClipboard("Baby $animal, oversized eyes, cinematic, $loopRule")
                }
                .setNegativeButton("CANCEL", null)
                .show()
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Veo", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Prompt Clipped 🎥", Toast.LENGTH_SHORT).show()
    }
}