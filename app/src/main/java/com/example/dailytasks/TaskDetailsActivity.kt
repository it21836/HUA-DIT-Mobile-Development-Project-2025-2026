package com.example.dailytasks

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dailytasks.data.StatusDefaults
import com.example.dailytasks.data.TaskRepository
import com.example.dailytasks.databinding.ActivityTaskDetailsBinding
import kotlinx.coroutines.launch

class TaskDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskDetailsBinding
    private val repository by lazy { TaskRepository.get(this) }

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

            binding.shortNameValue.text = task.shortName
            binding.descriptionValue.text = task.description
            binding.difficultyValue.text = task.difficulty.toString()
            binding.dateValue.text = task.date
            binding.startTimeValue.text = task.startTime
            binding.durationValue.text = task.durationHours.toString()
            binding.statusValue.text = statusLabel(task.statusId)
            binding.locationValue.text = task.location ?: "-"
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
}
