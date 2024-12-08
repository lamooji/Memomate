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
            loadGroupedTasks(selectedDate)
        }

        val addTaskButton = view.findViewById<ImageView>(R.id.add_task_button)
        addTaskButton.setOnClickListener { showAddTaskDialog() } // Separate dialog for adding tasks

        val backButton = view.findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        return view
    }

    private fun formatDateWithSuffix(date: Date?): String {
        if (date == null) return "Invalid Date" // Handle null case

        val dayFormat = SimpleDateFormat("d", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val day = dayFormat.format(date).toInt()
        val month = monthFormat.format(date)

        val suffix = when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }

        return "$month $day$suffix"
    }

    private fun showEditTaskDialog(task: Task) {
        // Inflate the layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_task_menu, null)

        val nameEditText = dialogView.findViewById<EditText>(R.id.name_edit_text)
        val ddlEditText = dialogView.findViewById<EditText>(R.id.ddl_edit_text)
        val importanceEditText = dialogView.findViewById<EditText>(R.id.importance_edit_text)
        val finishedCheckbox = dialogView.findViewById<CheckBox>(R.id.finished_checkbox)
        val noteEditText = dialogView.findViewById<EditText>(R.id.note_edit_text)
        val saveButton = dialogView.findViewById<Button>(R.id.add_button)

        // Pre-fill fields with task details
        nameEditText.setText(task.noteTitle)
        ddlEditText.setText(task.ddl) // Directly set the `ddl` as a string
        importanceEditText.setText(task.significance.toString())
        finishedCheckbox.isChecked = task.finished
        noteEditText.setText(task.noteAbstract)

        // Change button text to "Save"
        saveButton.text = "Save"

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Edit Task")
            .setView(dialogView)
            .setPositiveButton(null, null) // Use custom listener
            .setNegativeButton("Cancel", null) // Dismiss dialog on cancel
            .create()

        saveButton.setOnClickListener {
            val updatedName = nameEditText.text.toString()
            val updatedDdl = ddlEditText.text.toString()
            val updatedImportance = importanceEditText.text.toString().toIntOrNull() ?: 1
            val updatedFinished = finishedCheckbox.isChecked
            val updatedNote = noteEditText.text.toString()

            // Validate the `updatedDdl` format
            if (!isValidDateFormat(updatedDdl)) {
                ddlEditText.error = "Invalid date format. Use MM/dd/yyyy."
                return@setOnClickListener
            }

            // Update task in database
            GlobalScope.launch(Dispatchers.IO) {
                database.taskDao().updateTask(
                    task.copy(
                        noteTitle = updatedName,
                        ddl = updatedDdl, // No parsing needed, use `ddl` as string
                        significance = updatedImportance,
                        finished = updatedFinished,
                        noteAbstract = updatedNote
                    )
                )
                withContext(Dispatchers.Main) {
                    loadGroupedTasks(updatedDdl) // Refresh the UI
                    dialog.dismiss()
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

    private fun loadGroupedTasks(selectedDate: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val allTasks = database.taskDao().getAllTasks()
                val groupedItems = mutableListOf<Any>()

                val selectedDateParsed = dateFormat.parse(selectedDate)

                if (selectedDateParsed != null) {
                    val tasksWithinThreeDays = allTasks.filter { task ->
                        val taskDate = dateFormat.parse(task.ddl)
                        taskDate != null && (taskDate.time - selectedDateParsed.time) / (1000 * 60 * 60 * 24) in 0..2
                    }

                    val tasksBetweenThreeAndFiveDays = allTasks.filter { task ->
                        val taskDate = dateFormat.parse(task.ddl)
                        taskDate != null && (taskDate.time - selectedDateParsed.time) / (1000 * 60 * 60 * 24) in 3..5
                    }

                    val tasksAfterSevenDays = allTasks.filter { task ->
                        val taskDate = dateFormat.parse(task.ddl)
                        taskDate != null && (taskDate.time - selectedDateParsed.time) / (1000 * 60 * 60 * 24) > 7
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
                } else {
                    groupedItems.add("Invalid selected date.")
                }

                withContext(Dispatchers.Main) {
                    taskListAdapter.updateTaskItems(groupedItems)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("loadGroupedTasks", "Error: ${e.message}")
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
        showEditTaskDialog(newTask)
    }
}
