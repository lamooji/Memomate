package com.cs407.memoMate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_task_list, container, false)

        // Retrieve the selected date
        val selectedDateStr = arguments?.getString("selected_date")
        val selectedDate: Date = selectedDateStr?.let {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.parse(it)
        } ?: Date() // Default to today's date if no date is passed


        val sectionTitle = view.findViewById<TextView>(R.id.section_title)
        if (selectedDate != null) {
            val formattedDate = formatDateWithSuffix(selectedDate)
            sectionTitle.text = "$formattedDate's Tasks"
        } else {
            sectionTitle.text = "Tasks List"
        }

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

    private fun formatDateWithSuffix(date: Date): String {
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
        ddlEditText.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(task.ddl))
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

            val updatedDdlDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(updatedDdl) ?: Date()

            // Update task in database
            GlobalScope.launch(Dispatchers.IO) {
                database.taskDao().updateTask(
                    task.copy(
                        noteTitle = updatedName,
                        ddl = updatedDdlDate,
                        significance = updatedImportance,
                        finished = updatedFinished,
                        noteAbstract = updatedNote
                    )
                )
                withContext(Dispatchers.Main) {
                    loadGroupedTasks(updatedDdlDate) // Refresh the UI
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun loadGroupedTasks(selectedDate: Date) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedSelectedDate = dateFormat.format(selectedDate)

        GlobalScope.launch(Dispatchers.IO) {
            val tasksWithinThreeDays = database.taskDao().getTasksWithinThreeDays(formattedSelectedDate)
            val tasksBetweenThreeAndFiveDays = database.taskDao().getTasksBetweenThreeAndFiveDays(formattedSelectedDate)
            val tasksAfterSevenDays = database.taskDao().getTasksAfterSevenDays(formattedSelectedDate)

            withContext(Dispatchers.Main) {
                val groupedItems = mutableListOf<Any>()
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
                taskListAdapter.updateTaskItems(groupedItems)
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
            ddl = Date(), // Default deadline as the current date
            finished = false, // Default unfinished state
            noteTitle = "", // Empty title for a new task
            noteAbstract = "" // Empty abstract for a new task
        )
        showEditTaskDialog(newTask)
    }
}
