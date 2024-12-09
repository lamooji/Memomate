package com.cs407.memoMate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.memoMate.Data.NoteDatabase
import com.cs407.memoMate.Data.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ViewTaskListFragment : Fragment() {

    private lateinit var taskRecyclerView: RecyclerView
    private lateinit var taskListAdapter: TaskListAdapter
    private lateinit var database: NoteDatabase
    private val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()) // Use consistent format

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_task_list, container, false)

        // Retrieve the selected date or default to today's date
        val selectedDate: String = arguments?.getString("selected_date") ?: dateFormat.format(Date())

        val sectionTitle = view.findViewById<TextView>(R.id.section_title)
        sectionTitle.text = "Tasks List"

        // Initialize RecyclerView
        taskRecyclerView = view.findViewById(R.id.task_recycler_view)
        taskRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize adapter with callbacks for delete and edit
        taskListAdapter = TaskListAdapter(
            taskItems = mutableListOf(),
            onDeleteTask = { task, position -> deleteTask(task, position) },
            onEditTask = { task -> showEditTaskDialog(task) } // Updated to show edit dialog
        )
        taskRecyclerView.adapter = taskListAdapter

        // Initialize database
        database = NoteDatabase.getDatabase(requireContext())
        if (selectedDate.isNotEmpty()) {
            loadGroupedTasks()
        }

        val addTaskButton = view.findViewById<ImageView>(R.id.add_task_button)
        addTaskButton.setOnClickListener { showAddTaskDialog() } // Separate dialog for adding tasks

        val backButton = view.findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        return view
    }

    private fun showEditTaskDialog(task: Task, isNewTask: Boolean = false) {
        // Inflate the layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_task_menu, null)

        val nameEditText = dialogView.findViewById<EditText>(R.id.name_edit_text)
        val ddlEditText = dialogView.findViewById<EditText>(R.id.ddl_edit_text)
        val importanceSlider = dialogView.findViewById<com.google.android.material.slider.Slider>(R.id.importance_slider)
        val importanceLabel = dialogView.findViewById<TextView>(R.id.importance_label)
        val finishedCheckbox = dialogView.findViewById<CheckBox>(R.id.finished_checkbox)
        val noteEditText = dialogView.findViewById<EditText>(R.id.note_edit_text)
        val saveButton = dialogView.findViewById<Button>(R.id.add_button)

        // Function to update the importance label based on slider value
        fun updateImportanceLabel(value: Int) {
            val text = when (value) {
                1 -> "Red (1)"
                2 -> "Yellow (2)"
                3 -> "Green (3)"
                else -> "Green (3)"
            }
            importanceLabel.text = "Importance: $text"
        }

        // Pre-fill fields for existing tasks
        if (!isNewTask) {
            nameEditText.setText(task.noteTitle)
            ddlEditText.setText(task.ddl)
            importanceSlider.value = task.significance.toFloat()  // significance: Red=1, Yellow=2, Green=3
            updateImportanceLabel(task.significance)
            finishedCheckbox.isChecked = task.finished
            noteEditText.setText(task.noteAbstract)
            saveButton.text = "Save"
        } else {
            // Default values for a new task
            importanceSlider.value = 1f
            updateImportanceLabel(1)
            saveButton.text = "Add Task"
        }

        // Listen for slider changes
        importanceSlider.addOnChangeListener { _, value, _ ->
            updateImportanceLabel(value.toInt())
        }

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle(if (isNewTask) "Add Task" else "Edit Task")
            .setView(dialogView)
            .setNegativeButton("Cancel", null) // Dismiss dialog on cancel
            .create()

        saveButton.setOnClickListener {
            val updatedName = nameEditText.text.toString().trim()
            val updatedDdl = ddlEditText.text.toString().trim()
            val updatedImportance = importanceSlider.value.toInt()
            val updatedFinished = finishedCheckbox.isChecked
            val updatedNote = noteEditText.text.toString().trim()

            // Validate inputs
            if (updatedName.isEmpty()) {
                nameEditText.error = "Task name cannot be empty."
                return@setOnClickListener
            }

            if (!isValidDateFormat(updatedDdl)) {
                ddlEditText.error = "Invalid date format. Use MM/dd/yyyy."
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    if (isNewTask) {
                        // Check for duplicate task
                        val existingTasks = database.taskDao().getAllTasks()
                        if (existingTasks.any { it.noteTitle == updatedName && it.ddl == updatedDdl }) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "Task with the same name and date already exists.", Toast.LENGTH_SHORT).show()
                            }
                            return@launch
                        }

                        // Insert new task
                        database.taskDao().insertTask(
                            Task(
                                noteId = 0,
                                noteTitle = updatedName,
                                ddl = updatedDdl,
                                significance = updatedImportance,
                                finished = updatedFinished,
                                noteAbstract = updatedNote,
                                importance = updatedImportance
                            )
                        )
                    } else {
                        // Update existing task
                        database.taskDao().updateTask(
                            task.copy(
                                noteTitle = updatedName,
                                ddl = updatedDdl,
                                significance = updatedImportance,
                                finished = updatedFinished,
                                noteAbstract = updatedNote,
                                importance = updatedImportance
                            )
                        )
                    }

                    withContext(Dispatchers.Main) {
                        loadGroupedTasks()
                        dialog.dismiss()
                    }
                } catch (e: Exception) {
                    Log.e("showEditTaskDialog", "Error saving task: ${e.message}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Failed to save task.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }

        dialog.show()
    }

    // Helper function to validate date format
    private fun isValidDateFormat(date: String): Boolean {
        return try {
            dateFormat.isLenient = false
            dateFormat.parse(date) != null
        } catch (e: Exception) {
            false
        }
    }

    private fun loadGroupedTasks() {
        val todayString = dateFormat.format(Date()) // Today's date as string
        val todayDate = dateFormat.parse(todayString) ?: Date() // Parse today's date

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val allTasks = database.taskDao().getAllTasks()
                Log.d("loadGroupedTasks", "All tasks from DB: $allTasks")
                Log.d("loadGroupedTasks", "Number of All tasks: ${allTasks.size}")

                val groupedItems = mutableListOf<Any>()

                val tasksWithinThreeDays = mutableListOf<Task>()
                val tasksBetweenThreeAndFiveDays = mutableListOf<Task>()
                val tasksAfterSevenDays = mutableListOf<Task>()

                allTasks.forEach { task ->
                    val taskDate = dateFormat.parse(task.ddl)
                    if (taskDate != null) {
                        val daysDifference = (taskDate.time - todayDate.time) / (1000 * 60 * 60 * 24)
                        when {
                            daysDifference in 0..2 -> tasksWithinThreeDays.add(task)
                            daysDifference in 3..7 -> tasksBetweenThreeAndFiveDays.add(task)
                            daysDifference > 7 -> tasksAfterSevenDays.add(task)
                        }
                    }
                }

                if (tasksWithinThreeDays.isNotEmpty()) {
                    Log.d("loadGroupedTasks", "tasksWithinThreeDays:${tasksWithinThreeDays}")
                    Log.d("loadGroupedTasks", "number of taks in 3 days:${tasksWithinThreeDays.size}")

                    groupedItems.add("Upcoming in 3 Days")
                    groupedItems.addAll(tasksWithinThreeDays)
                }
                if (tasksBetweenThreeAndFiveDays.isNotEmpty()) {
                    Log.d("loadGroupedTasks", "number of taks in 7 days:${tasksBetweenThreeAndFiveDays.size}")
                    Log.d("loadGroupedTasks", "tasksBetweenThreeAndFiveDays:${tasksBetweenThreeAndFiveDays}")

                    groupedItems.add("Upcoming in 7 Days")
                    groupedItems.addAll(tasksBetweenThreeAndFiveDays)
                }
                if (tasksAfterSevenDays.isNotEmpty()) {
                    Log.d("loadGroupedTasks", "number of taks in > 7 days:${tasksAfterSevenDays.size}")
                    Log.d("loadGroupedTasks", "tasksAfterSevenDays:${tasksAfterSevenDays}")
                    groupedItems.add("Upcoming in Future")
                    groupedItems.addAll(tasksAfterSevenDays)
                }

                withContext(Dispatchers.Main) {
                    taskListAdapter.updateTaskItems(groupedItems)
                }
            } catch (e: Exception) {
                Log.e("loadGroupedTasks", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    taskListAdapter.updateTaskItems(listOf("Failed to load tasks."))
                }
            }
        }
    }

    private fun deleteTask(task: Task, position: Int) {
        Log.d("DeleteTask", "Deleting task: ${task.noteId} - ${task.noteTitle}")

        GlobalScope.launch(Dispatchers.IO) {
            database.taskDao().deleteTask(task) // Delete task from database
            withContext(Dispatchers.Main) {
                taskListAdapter.removeTask(position) // Remove task from adapter and update UI
            }
        }
    }

    private fun showAddTaskDialog() {
        val newTask = Task(
            noteId = 0, // Default ID for a new task
            significance = 1, // Default significance (Red)
            importance = 1,
            ddl = "", // Empty by default
            finished = false,
            noteTitle = "",
            noteAbstract = ""
        )
        showEditTaskDialog(newTask, isNewTask = true)
    }
}
