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
        if (selectedDate != null) {
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
        val importanceEditText = dialogView.findViewById<EditText>(R.id.importance_edit_text)
        val finishedCheckbox = dialogView.findViewById<CheckBox>(R.id.finished_checkbox)
        val noteEditText = dialogView.findViewById<EditText>(R.id.note_edit_text)
        val saveButton = dialogView.findViewById<Button>(R.id.add_button)

        // Pre-fill fields for existing tasks
        if (!isNewTask) {
            nameEditText.setText(task.noteTitle)
            ddlEditText.setText(task.ddl)
            importanceEditText.setText(task.significance.toString())
            finishedCheckbox.isChecked = task.finished
            noteEditText.setText(task.noteAbstract)
            saveButton.text = "Save"
        } else {
            saveButton.text = "Add Task"
        }

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle(if (isNewTask) "Add Task" else "Edit Task")
            .setView(dialogView)
            .setNegativeButton("Cancel", null) // Dismiss dialog on cancel
            .create()

        saveButton.setOnClickListener {
            val updatedName = nameEditText.text.toString().trim()
            val updatedDdl = ddlEditText.text.toString().trim()
            val updatedImportance = importanceEditText.text.toString().toIntOrNull() ?: 1
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
                        // Insert new task
                        database.taskDao().insertTask(
                            Task(
                                noteId = 0,
                                noteTitle = updatedName,
                                ddl = updatedDdl,
                                significance = updatedImportance,
                                finished = updatedFinished,
                                noteAbstract = updatedNote,
                                importance = task.importance
                            )
                        )
                        Log.d("showEditTaskDialog", "Task added: $updatedName")
                    } else {
                        // Update existing task
                        database.taskDao().updateTask(
                            task.copy(
                                noteTitle = updatedName,
                                ddl = updatedDdl,
                                significance = updatedImportance,
                                finished = updatedFinished,
                                noteAbstract = updatedNote
                            )
                        )
                        Log.d("showEditTaskDialog", "Task updated: $updatedName")
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

                val groupedItems = mutableListOf<Any>()

                val tasksWithinThreeDays = allTasks.filter { task ->
                    val taskDate = dateFormat.parse(task.ddl)
                    taskDate != null && (taskDate.time - todayDate.time) / (1000 * 60 * 60 * 24) in 0..2
                }

                val tasksBetweenThreeAndFiveDays = allTasks.filter { task ->
                    val taskDate = dateFormat.parse(task.ddl)
                    taskDate != null && (taskDate.time - todayDate.time) / (1000 * 60 * 60 * 24) in 3..5
                }

                val tasksAfterSevenDays = allTasks.filter { task ->
                    val taskDate = dateFormat.parse(task.ddl)
                    taskDate != null && (taskDate.time - todayDate.time) / (1000 * 60 * 60 * 24) > 7
                }

                val tasksNotGrouped = allTasks.filterNot { task ->
                    val taskDate = dateFormat.parse(task.ddl)
                    taskDate != null && (taskDate.time - todayDate.time) / (1000 * 60 * 60 * 24) >= 0
                }

                if (tasksWithinThreeDays.isNotEmpty()) {
                    groupedItems.add("Upcoming in 3 Days")
                    groupedItems.addAll(tasksWithinThreeDays)
                }
                if (tasksBetweenThreeAndFiveDays.isNotEmpty()) {
                    groupedItems.add("Upcoming in 7 Days")
                    groupedItems.addAll(tasksBetweenThreeAndFiveDays)
                }
                if (tasksAfterSevenDays.isNotEmpty()) {
                    groupedItems.add("Upcoming in Future")
                    groupedItems.addAll(tasksAfterSevenDays)
                }
                if (tasksNotGrouped.isNotEmpty()) {
                    groupedItems.add("Other Tasks")
                    groupedItems.addAll(tasksNotGrouped)
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
            significance = 1, // Default significance level
            importance = 1,
            ddl = "", // Default deadline as the current date
            finished = false, // Default unfinished state
            noteTitle = "", // Empty title for a new task
            noteAbstract = "" // Empty abstract for a new task
        )
        showEditTaskDialog(newTask, isNewTask = true)
    }
}
