package com.example.dailytasks

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import android.content.Intent
import android.net.Uri
import com.example.dailytasks.data.StatusDefaults
import com.example.dailytasks.data.TaskEntity
import com.example.dailytasks.data.TaskRepository
import com.example.dailytasks.databinding.ActivityTaskDetailsBinding
import kotlinx.coroutines.launch

class TaskDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskDetailsBinding
    private val repository by lazy { TaskRepository.get(this) }
    private var currentTask: TaskEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val taskId = intent.getIntExtra("task_id", -1)
        if (taskId == -1) {
            Toast.makeText(this, "No task id", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadTask(taskId)
        setupActions()
    }

    private fun loadTask(id: Int) {
        lifecycleScope.launch {
            val task = repository.getById(id)
            if (task == null) {
                Toast.makeText(this@TaskDetailsActivity, "Task not found", Toast.LENGTH_SHORT)
                    .show()
                finish()
                return@launch
            }

            currentTask = task
            binding.shortNameValue.text = task.shortName
            binding.descriptionValue.text = task.description
            binding.difficultyValue.text = task.difficulty.toString()
            binding.dateValue.text = task.date
            binding.startTimeValue.text = task.startTime
            binding.durationValue.text = task.durationHours.toString()
            binding.statusValue.text = statusLabel(task.statusId)
            val loc = task.location
            binding.locationValue.text = loc ?: "-"
            binding.openMapsButton.isEnabled = !loc.isNullOrBlank()
            binding.completeButton.isEnabled = task.statusId != StatusDefaults.COMPLETED
        }
    }

    private fun statusLabel(id: Int): String {
        return when (id) {
            StatusDefaults.EXPIRED -> "expired"
            StatusDefaults.IN_PROGRESS -> "in-progress"
            StatusDefaults.RECORDED -> "recorded"
            StatusDefaults.COMPLETED -> "completed"
            else -> "unknown"
        }
    }

    private fun setupActions() {
        binding.completeButton.setOnClickListener {
            val task = currentTask ?: return@setOnClickListener
            if (task.statusId == StatusDefaults.COMPLETED) return@setOnClickListener

            val updated = task.copy(statusId = StatusDefaults.COMPLETED)
            lifecycleScope.launch {
                repository.updateTask(updated)
                Toast.makeText(this@TaskDetailsActivity, "Marked as completed", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }

        binding.openMapsButton.setOnClickListener {
            val task = currentTask ?: return@setOnClickListener
            val loc = task.location ?: return@setOnClickListener
            if (loc.isBlank()) return@setOnClickListener
            val uri = Uri.parse("geo:0,0?q=${Uri.encode(loc)}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }
}
