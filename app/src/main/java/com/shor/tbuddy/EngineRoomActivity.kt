package com.shor.tbuddy

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
// This now matches your exact lowercase filename 'engineroomactivity.xml'
import com.shor.tbuddy.databinding.EngineroomactivityBinding

class EngineRoomActivity : AppCompatActivity() {

    private lateinit var binding: EngineroomactivityBinding
    private lateinit var keyVault: KeyVault

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate using the specific name generated from engineroomactivity.xml
        binding = EngineroomactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        keyVault = KeyVault(this)

        // Load existing keys from storage
        val prefs = getSharedPreferences("ShortBuddyKeys", Context.MODE_PRIVATE)
        val savedKeys = prefs.getString("raw_key_pool", "")
        binding.etKeyPool.setText(savedKeys)

        // The "Engage" button logic
        binding.btnSaveKeys.setOnClickListener {
            val rawKeys = binding.etKeyPool.text.toString()
            if (rawKeys.isNotEmpty()) {
                // Save the text to the vault
                prefs.edit().putString("raw_key_pool", rawKeys).apply()

                // Update the active rolling pool
                keyVault.updatePool(rawKeys)

                Toast.makeText(this, "Pool Engaged: ${keyVault.getPoolSize()} keys ready.", Toast.LENGTH_LONG).show()
                finish() // Close window and return to Mission Control
            } else {
                Toast.makeText(this, "Paste keys first, Ninja!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}