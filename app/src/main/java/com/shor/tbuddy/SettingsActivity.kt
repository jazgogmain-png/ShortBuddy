package com.shor.tbuddy.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.shor.tbuddy.databinding.ActivitySettingsKeysBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsKeysBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsKeysBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Using the central SharedPreferences for the Ninja stack
        val prefs = getSharedPreferences("ShortBuddyPrefs", Context.MODE_PRIVATE)

        // 1. Load existing key if it exists
        binding.etApiKey.setText(prefs.getString("gemini_api_key", ""))

        // 2. Navigation to the Engine Room (The Factory)
        binding.btnOpenEngine.setOnClickListener {
            val intent = Intent(this, EngineRoomActivity::class.java)
            startActivity(intent)
        }

        // 3. Save Logic for the Vault
        binding.btnSaveKeys.setOnClickListener {
            val key = binding.etApiKey.text.toString()

            if (key.isNotEmpty()) {
                prefs.edit().putString("gemini_api_key", key).apply()
                Toast.makeText(this, "Vault Locked 🔐", Toast.LENGTH_SHORT).show()
                finish() // Head back to the main cockpit
            } else {
                Toast.makeText(this, "Key Required for G3 Engine", Toast.LENGTH_SHORT).show()
            }
        }
    }
}