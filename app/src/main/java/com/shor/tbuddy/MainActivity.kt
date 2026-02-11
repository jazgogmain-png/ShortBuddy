package com.shor.tbuddy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.shor.tbuddy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("lola_gemini", "🚀 [SYSTEM_START] Mission Control Awakening...")

        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            setupNavigation()
            Log.e("lola_gemini", "✅ [UI_INIT] Cockpit Layout Inflated.")
        } catch (e: Exception) {
            Log.e("lola_gemini", "🛑 [CRASH] Inflation Failed: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun setupNavigation() {
        binding.navigationRail.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.lane_engine -> {
                    startActivity(Intent(this, EngineRoomActivity::class.java))
                    true
                }
                R.id.lane_animals -> {
                    binding.currentLaneText.text = "Active Lane: Cuteness Overload 🐾"
                    true
                }
                // Handle the others so the app doesn't ignore the clicks
                else -> true
            }
        }
    }
}