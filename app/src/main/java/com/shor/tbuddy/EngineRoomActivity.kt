package com.shor.tbuddy.ui

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.shor.tbuddy.database.AppDatabase
import com.shor.tbuddy.databinding.ActivityEngineRoomBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EngineRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEngineRoomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEngineRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("ShortBuddyPrefs", Context.MODE_PRIVATE)

        // 🧬 LOAD SAVED CORE DATA
        // Automation level dictates how assertive the AI is in creative choices
        binding.sliderAutomation.value = prefs.getFloat("automation_level", 50f)

        binding.etMeltTemplate.setText(prefs.getString("template_melt",
            "This little [animal] just melted my heart 🥹🐾"))

        binding.etAdoptionTemplate.setText(prefs.getString("template_adoption",
            "Would you adopt this tiny [animal]? 🐾✨"))

        binding.etPromptDefault.setText(prefs.getString("prompt_default",
            "Ultra cute baby [animal] with big glossy eyes, cinematic lighting, 8k, highly detailed."))

        binding.etPromptCozy.setText(prefs.getString("prompt_cozy",
            "Baby [animal] sleepy, soft pastel lighting, fluffy textures, cozy atmosphere."))

        // 💾 SYNCHRONIZE CORE (SAVE)
        binding.btnSaveEngine.setOnClickListener {
            prefs.edit().apply {
                putFloat("automation_level", binding.sliderAutomation.value)
                putString("template_melt", binding.etMeltTemplate.text.toString())
                putString("template_adoption", binding.etAdoptionTemplate.text.toString())
                putString("prompt_default", binding.etPromptDefault.text.toString())
                putString("prompt_cozy", binding.etPromptCozy.text.toString())
                apply()
            }

            Toast.makeText(this, "CORE_SYNCHRONIZED 🦾", Toast.LENGTH_SHORT).show()
            finish()
        }

        // 💣 DATA PURGE (Safe call in case button isn't in XML yet)
        binding.root.findViewWithTag<android.view.View>("nuke_btn")?.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                AppDatabase.getDatabase(this@EngineRoomActivity).projectDao().nukeBlackbox()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EngineRoomActivity, "BLACKBOX_PURGED 💀", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}