package com.shor.tbuddy.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shor.tbuddy.databinding.ActivityPerformanceBinding
import com.shor.tbuddy.models.PerformanceMetric

class PerformanceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPerformanceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerformanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInsightPanel()

        binding.btnClosePerformance.setOnClickListener {
            finish()
        }
    }

    private fun setupInsightPanel() {
        // This is the "AI Insight Panel" from your blueprint
        val insights = """
            • Audience prefers: Sleepy/Cozy animals 🐾
            • Top Lighting: Warm / Golden Hour ☀️
            • Top Motion: Slow Motion / Head Tilts 🎥
        """.trimIndent()

        binding.tvAiInsights.text = insights
    }
}