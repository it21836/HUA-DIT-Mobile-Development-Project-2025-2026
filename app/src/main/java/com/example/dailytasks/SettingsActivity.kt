package com.example.dailytasks

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dailytasks.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val prefs by lazy { getSharedPreferences("settings", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadDefaults()
        setupSave()
    }

    private fun loadDefaults() {
        val defaultDuration = prefs.getInt("default_duration", 1)
        val defaultDifficulty = prefs.getInt("default_difficulty", 5)
        binding.defaultDurationInput.setText(defaultDuration.toString())
        binding.defaultDifficultyInput.setText(defaultDifficulty.toString())
    }

    private fun setupSave() {
        binding.saveDefaultsButton.setOnClickListener {
            val durationVal = binding.defaultDurationInput.text.toString().toIntOrNull()
            val difficultyVal = binding.defaultDifficultyInput.text.toString().toIntOrNull()

            if (durationVal == null || durationVal <= 0) {
                Toast.makeText(this, "Duration must be > 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (difficultyVal == null || difficultyVal !in 0..10) {
                Toast.makeText(this, "Difficulty must be 0..10", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prefs.edit()
                .putInt("default_duration", durationVal)
                .putInt("default_difficulty", difficultyVal)
                .apply()

            Toast.makeText(this, "Defaults saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
