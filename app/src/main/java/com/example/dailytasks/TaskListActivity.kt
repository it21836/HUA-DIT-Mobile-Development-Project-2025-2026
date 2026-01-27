package com.example.dailytasks

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dailytasks.data.StatusDefaults
import com.example.dailytasks.data.TaskEntity
import com.example.dailytasks.data.TaskRepository
import com.example.dailytasks.databinding.ActivityTaskListBinding
import com.example.dailytasks.databinding.ItemTaskBinding
import kotlinx.coroutines.launch
import java.io.File

class TaskListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskListBinding
    private val repository by lazy { TaskRepository.get(this) }
    private val adapter = TaskAdapter { task ->
        val intent = Intent(this, TaskDetailsActivity::class.java)
        intent.putExtra("task_id", task.uid)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        setupExport()
        loadTasks()
    }

    override fun onResume() {
        super.onResume()
        loadTasks()
    }

    private fun loadTasks() {
        lifecycleScope.launch {
            val tasks = repository.getPendingOrdered()
            adapter.submit(tasks)
        }
    }

    private fun setupExport() {
        binding.exportButton.setOnClickListener {
            lifecycleScope.launch {
                val tasks = repository.getPendingOrdered()
                if (tasks.isEmpty()) {
                    Toast.makeText(this@TaskListActivity, getString(R.string.export_empty), Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: filesDir
                val file = File(dir, "pending_${System.currentTimeMillis()}.txt")
                runCatching {
                    file.printWriter().use { out ->
                        tasks.forEach { task ->
                            out.println(formatTaskLine(task))
                        }
                    }
                }.onSuccess {
                    Toast.makeText(this@TaskListActivity, getString(R.string.export_success, file.absolutePath), Toast.LENGTH_LONG).show()
                }.onFailure {
                    Toast.makeText(this@TaskListActivity, getString(R.string.export_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun formatTaskLine(task: TaskEntity): String {
        val status = statusLabel(task.statusId)
        val loc = task.location ?: "-"
        return "${task.shortName} | $status | ${task.date} ${task.startTime} | duration ${task.durationHours}h | diff ${task.difficulty} | loc $loc"
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

private class TaskAdapter(
    private val onItemClick: (TaskEntity) -> Unit
) : RecyclerView.Adapter<TaskViewHolder>() {

    private val items = mutableListOf<TaskEntity>()

    fun submit(newItems: List<TaskEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding, onItemClick)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(items[position])
    }
}

private class TaskViewHolder(
    private val binding: ItemTaskBinding,
    private val onItemClick: (TaskEntity) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(task: TaskEntity) {
        binding.shortNameText.text = task.shortName
        binding.statusText.text = statusLabel(task.statusId)
        binding.timeText.text = task.startTime
        binding.root.setOnClickListener { onItemClick(task) }
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
