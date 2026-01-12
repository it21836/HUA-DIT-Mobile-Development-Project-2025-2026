package com.example.dailytasks

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import android.content.Intent
import com.example.dailytasks.data.StatusDefaults
import com.example.dailytasks.data.TaskEntity
import com.example.dailytasks.data.TaskRepository
import com.example.dailytasks.data.TaskValidator
import com.example.dailytasks.databinding.ActivityMainBinding
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private var selectedStartTime: LocalTime = LocalTime.now()
    private val prefs by lazy { getSharedPreferences("settings", MODE_PRIVATE) }
    private val repository by lazy { TaskRepository.get(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDefaults()
        setupTimePicker()
        setupSave()
        setupSettings()
    }

    private fun setupDefaults() {
        val defaultDuration = prefs.getInt("default_duration", 1)
        val defaultDifficulty = prefs.getInt("default_difficulty", 5)
        binding.durationInput.setText(defaultDuration.toString())
        binding.difficultyInput.setText(defaultDifficulty.toString())
        updateTimeLabel(selectedStartTime)
    }

    private fun setupTimePicker() {
        binding.pickTimeButton.setOnClickListener {
            val now = selectedStartTime
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    selectedStartTime = LocalTime.of(hour, minute)
                    updateTimeLabel(selectedStartTime)
                },
                now.hour,
                now.minute,
                true
            ).show()
        }
    }

    private fun setupSave() {
        binding.saveButton.setOnClickListener {
            val shortName = binding.shortNameInput.text.toString().trim()
            val description = binding.descriptionInput.text.toString().trim()
            val difficultyVal = binding.difficultyInput.text.toString().toIntOrNull()
            val durationVal = binding.durationInput.text.toString().toIntOrNull()
            val location = binding.locationInput.text.toString().trim().ifBlank { null }

            val parseErrors = mutableListOf<String>()
            if (difficultyVal == null) parseErrors += "Difficulty must be a number"
            if (durationVal == null) parseErrors += "Duration must be a number"

            val errors = TaskValidator.validate(
                shortName = shortName,
                description = description,
                difficulty = difficultyVal ?: -1,
                durationHours = durationVal ?: -1
            ) + parseErrors

            if (errors.isNotEmpty()) {
                Toast.makeText(this, errors.joinToString("\n"), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val today = LocalDate.now().toString()
            val task = TaskEntity(
                shortName = shortName,
                description = description,
                difficulty = difficultyVal ?: 0,
                date = today,
                startTime = selectedStartTime.format(timeFormatter),
                durationHours = durationVal ?: 0,
                statusId = StatusDefaults.RECORDED,
                location = location
            )

            lifecycleScope.launch {
                repository.addTask(task)
                Toast.makeText(this@MainActivity, "Task saved", Toast.LENGTH_SHORT).show()
                clearForm()
            }
        }
    }

    private fun clearForm() {
        binding.shortNameInput.text?.clear()
        binding.descriptionInput.text?.clear()
        binding.locationInput.text?.clear()

        val defaultDuration = prefs.getInt("default_duration", 1)
        val defaultDifficulty = prefs.getInt("default_difficulty", 5)
        binding.durationInput.setText(defaultDuration.toString())
        binding.difficultyInput.setText(defaultDifficulty.toString())

        selectedStartTime = LocalTime.now()
        updateTimeLabel(selectedStartTime)
    }

    private fun setupSettings() {
        binding.openSettingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun updateTimeLabel(time: LocalTime) {
        binding.startTimeValue.text = time.format(timeFormatter)
    }
}

