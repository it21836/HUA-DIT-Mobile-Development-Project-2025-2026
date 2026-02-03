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
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.dailytasks.data.StatusDefaults
import com.example.dailytasks.data.TaskEntity
import com.example.dailytasks.data.TaskRepository
import com.example.dailytasks.databinding.ActivityTaskListBinding
import com.example.dailytasks.databinding.ItemTaskBinding
import com.example.dailytasks.provider.TaskContract
import com.example.dailytasks.work.StatusUpdateWorker
import kotlinx.coroutines.launch
import java.io.File
import android.content.ContentValues
import java.time.format.DateTimeFormatter

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
        setupRefresh()
        setupProviderTests()
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
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                dir?.mkdirs()
                val targetDir = dir ?: filesDir
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

    private fun setupRefresh() {
        binding.refreshButton.setOnClickListener {
            val request = OneTimeWorkRequestBuilder<StatusUpdateWorker>().build()
            WorkManager.getInstance(this).enqueue(request)
            Toast.makeText(this, "Refresh scheduled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupProviderTests() {
        binding.providerInsertButton.setOnClickListener {
            val cv = ContentValues().apply {
                put("shortName", "Provider task")
                put("description", "Inserted via ContentProvider")
                put("difficulty", 3)
                put("date", java.time.LocalDate.now().toString())
                put("startTime", java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")))
                put("durationHours", 1)
                put("statusId", StatusDefaults.RECORDED)
                put("location", "provider test loc")
            }
            contentResolver.insert(TaskContract.TASKS_URI, cv)
            loadTasks()
            Toast.makeText(this, "Inserted via provider", Toast.LENGTH_SHORT).show()
        }

        binding.providerQueryButton.setOnClickListener {
            contentResolver.query(TaskContract.TASKS_URI, null, null, null, null)?.use { cursor ->
                Toast.makeText(this, "Provider rows: ${cursor.count}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatTaskLine(task: TaskEntity): String {
        val status = statusLabel(task.statusId)
        val loc = task.location ?: "-"
        val desc = task.description
        return "id ${task.uid} | ${task.shortName} | $status | ${task.date} ${task.startTime} | duration ${task.durationHours}h | diff ${task.difficulty} | loc $loc | desc $desc"
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
