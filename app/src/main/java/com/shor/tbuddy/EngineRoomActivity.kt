package com.shor.tbuddy.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shor.tbuddy.databinding.ActivityChannelSettingsBinding

class ChannelSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChannelSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChannelSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // We'll add the G3-assisted template logic here next!
    }
}