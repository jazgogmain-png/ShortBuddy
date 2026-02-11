package com.shor.tbuddy

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shor.tbuddy.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Use the same Prefs file as the rest of the app
        val prefs = getSharedPreferences("ShortBuddyPrefs", Context.MODE_PRIVATE)

        // 1. LOAD EXISTING DATA
        val savedPrompt = prefs.getString("master_system_prompt", "")
        val savedTags = prefs.getString("static_tags", "#shorts #viralshorts #aibaby")
        val savedAutoLevel = prefs.getFloat("automation_level", 0f)

        binding.etMasterPrompt.setText(savedPrompt)
        binding.etStaticTags.setText(savedTags)
        binding.sliderAutomation.value = savedAutoLevel

        // 2. THE SAVE LOGIC
        binding.btnSaveSettings.setOnClickListener {
            val newPrompt = binding.etMasterPrompt.text.toString()
            val newTags = binding.etStaticTags.setText(savedTags).toString() // Just ensuring it's a string
            val newLevel = binding.sliderAutomation.value

            prefs.edit().apply {
                putString("master_system_prompt", newPrompt)
                putString("static_tags", binding.etStaticTags.text.toString())
                putFloat("automation_level", newLevel)
                apply()
            }

            SlopLogger.success("ENGINE: Viral DNA Locked. Auto-Level: $newLevel%")

            // Go back to the main cockpit
            finish()
        }
    }
}