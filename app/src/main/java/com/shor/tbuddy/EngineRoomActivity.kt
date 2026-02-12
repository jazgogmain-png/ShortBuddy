package com.shor.tbuddy

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.shor.tbuddy.databinding.ActivitySettingsBinding

class EngineRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val loopRule = "Ensure seamless looping by ending the animation mid-blink or mid-head-tilt so the transition back to the start feels natural and continuous."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("ShortBuddyPrefs", Context.MODE_PRIVATE)

        // Load Persisted Data
        binding.etMasterPrompt.setText(prefs.getString("master_system_prompt", ""))
        binding.etStaticTags.setText(prefs.getString("static_tags", "#shorts #viralshorts #aibaby"))
        binding.sliderAutomation.value = prefs.getFloat("automation_level", 0f)

        setupFrontEngineFactory()

        binding.btnSaveSettings.setOnClickListener {
            prefs.edit().apply {
                putString("master_system_prompt", binding.etMasterPrompt.text.toString())
                putString("static_tags", binding.etStaticTags.text.toString())
                putFloat("automation_level", binding.sliderAutomation.value)
                apply()
            }
            Toast.makeText(this, "Engine Locked 🦾", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupFrontEngineFactory() {
        binding.btnGenKitten.setOnClickListener {
            copyToClipboard("Ultra cute baby kitten with big glossy eyes, tiny pink nose, soft fluffy fur, slow blinking and gentle head tilt, sitting on a cozy pastel blanket, warm golden light, shallow depth of field, cinematic softness, dreamy atmosphere, ultra detailed fur texture, smooth natural motion, emotional warmth, vertical 9:16, $loopRule")
        }

        binding.btnGenPuppy.setOnClickListener {
            copyToClipboard("Tiny baby puppy with oversized eyes, soft fluffy fur, tiny paws, curious head tilt, gentle breathing motion, cozy indoor pastel lighting, soft cinematic glow, shallow depth of field, emotional warmth, dreamy realism, smooth slow movement, ultra detailed fur, vertical 9:16, $loopRule")
        }

        binding.btnGenFox.setOnClickListener {
            copyToClipboard("Adorable baby fox with oversized sparkling eyes, fluffy tail, tiny body proportions, slow blink, gentle head tilt, soft glowing forest background, pastel fantasy lighting, cinematic softness, dreamy atmosphere, ultra detailed fur, smooth gentle movement, emotional warmth, vertical 9:16, $loopRule")
        }

        binding.btnGenBunny.setOnClickListener {
            copyToClipboard("Ultra cute baby bunny with big round eyes, soft fluffy fur, tiny twitching nose, gentle blinking, pastel meadow background, warm soft lighting, cinematic depth of field, dreamy cozy atmosphere, ultra detailed fur, smooth subtle movement, vertical 9:16, $loopRule")
        }

        binding.btnGenCustom.setOnClickListener {
            showCustomPromptDialog()
        }
    }

    private fun showCustomPromptDialog() {
        val input = EditText(this)
        input.hint = "e.g. Red Panda, Hippo, Seal"
        input.setPadding(60, 40, 60, 40)

        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle("CUSTOM ANIMAL")
            .setMessage("Enter animal for the Viral Prompt:")
            .setView(input)
            .setPositiveButton("GENERATE") { _, _ ->
                val animal = input.text.toString().ifEmpty { "baby animal" }
                val customPrompt = "Ultra cute baby $animal with oversized sparkling eyes, " +
                        "tiny body proportions, soft fluffy fur, slow blink, gentle head tilt, " +
                        "soft glowing background, pastel fantasy lighting, cinematic softness, " +
                        "dreamy atmosphere, ultra detailed fur, smooth gentle movement, " +
                        "emotional warmth, vertical 9:16, $loopRule"

                copyToClipboard(customPrompt)
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("VeoPrompt", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "VEO Prompt Clipped 🎥", Toast.LENGTH_SHORT).show()
    }
}