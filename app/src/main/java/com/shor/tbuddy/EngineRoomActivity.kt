package com.shor.tbuddy

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.shor.tbuddy.databinding.EngineroomactivityBinding

class EngineRoomActivity : AppCompatActivity() {

    private lateinit var binding: EngineroomactivityBinding
    private lateinit var keyVault: KeyVault

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EngineroomactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        keyVault = KeyVault(this)

        // Unified Preference File: "ShortBuddyPrefs"
        val prefs = getSharedPreferences("ShortBuddyPrefs", Context.MODE_PRIVATE)
        val savedKeys = prefs.getString("raw_key_pool", "")
        binding.etKeyPool.setText(savedKeys)

        binding.btnSaveKeys.setOnClickListener {
            val rawKeys = binding.etKeyPool.text.toString().trim()
            if (rawKeys.isNotEmpty()) {
                // Save to the unified vault
                prefs.edit().putString("raw_key_pool", rawKeys).apply()

                // Force the KeyVault to re-read the storage
                keyVault.updatePool()

                val poolSize = keyVault.getPoolSize()
                SlopLogger.success("ENGINE_ROOM: Keys locked in. Pool Size: $poolSize")

                Toast.makeText(this, "Pool Engaged: $poolSize keys ready.", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Paste keys first, Ninja!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}