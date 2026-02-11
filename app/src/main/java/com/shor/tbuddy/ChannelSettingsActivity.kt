package com.shor.tbuddy

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.shor.tbuddy.databinding.ActivityChannelSettingsBinding

class ChannelSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChannelSettingsBinding

    // The Account Picker Launcher
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        if (task.isSuccessful) {
            val account = task.result
            binding.btnLinkYouTube.text = "Linked: ${account.email}"
            SlopLogger.info("YouTube Account Linked: ${account.email}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChannelSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLinkYouTube.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                // Requesting the ability to manage YouTube videos
                .requestScopes(Scope("https://www.googleapis.com/auth/youtube.upload"))
                .build()

            val signInClient = GoogleSignIn.getClient(this, gso)
            signInLauncher.launch(signInClient.signInIntent)
        }

        binding.btnSaveBlueprint.setOnClickListener {
            // Logic to save the Blueprint (Voice, Tags, Description) to SharedPreferences
            finish()
        }
    }
}