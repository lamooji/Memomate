package com.cs407.memoMate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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
import java.util.Date
import java.util.Locale


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

        // Parse the string into a Date object
        val selectedDate: Date? = selectedDateStr?.let {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Adjust the format to match your input
            dateFormat.parse(it) // Returns a Date object or null if parsing fails
        }

        val sectionTitle = view.findViewById<TextView>(R.id.section_title)

        // Update the TextView based on the selected date
        if (selectedDate != null) {
            val formattedDate = formatDateWithSuffix(selectedDate)
            sectionTitle.text = "$formattedDate's Tasks"
        } else {
            sectionTitle.text = "Tasks List" // Default title
        }

        // Initialize RecyclerView
        taskRecyclerView = view.findViewById(R.id.task_recycler_view)
        taskRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize adapter with an empty list
        taskListAdapter = TaskListAdapter(mutableListOf())
        taskRecyclerView.adapter = taskListAdapter

        // Initialize database
        database = NoteDatabase.getDatabase(requireContext())
        Log.d("ViewTaskListFragment", "Database initialized: $database")

        // Load grouped tasks from database
        if (selectedDate != null) {
            loadGroupedTasks(selectedDate)
        }

        val addTaskButton = view.findViewById<ImageView>(R.id.add_task_button)
        addTaskButton.setOnClickListener {
            openAddTaskDialog()
        }

        val backButton = view.findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return view
    }


    private fun formatDateWithSuffix(date: Date): String {
        val dayFormat = SimpleDateFormat("d", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        val day = dayFormat.format(date).toInt()
        val month = monthFormat.format(date)

        // Determine the suffix for the day
        val suffix = when {
            day in 11..13 -> "th" // Special case for 11th, 12th, 13th
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }

        return "$month $day$suffix"
    }


    private fun openAddTaskDialog() {
        // Create an instance of the dialog
        val addTaskDialog = AddTaskMenu.newInstance()

        // Set the listener to handle the callback
        addTaskDialog.setListener(object : AddTaskMenu.TaskDialogListener {
            override fun onTaskAdded(
                name: String,
                ddl: String,
                isFinished: Boolean,
                note: String,
                importance: Int,
                task: TaskItem?
            ) {
                // Convert the provided data into a Task object
                val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val ddlDate = dateFormatter.parse(ddl) ?: Date()

                val newTask = Task(
                    noteId = 0, // Auto-generate ID
                    noteTitle = name,
                    significance = importance,
                    ddl = ddlDate,
                    finished = isFinished,
                    noteAbstract = note
                )

                // Add the task to the database and the RecyclerView
                addTask(newTask)
            }
        })

        // Show the dialog
        addTaskDialog.show(parentFragmentManager, "AddTaskMenu")
    }

    private fun loadGroupedTasks(selectedDate: Date) {

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedSelectedDate = dateFormat.format(selectedDate)

        GlobalScope.launch(Dispatchers.IO) {

            val allTasks = database.taskDao().getAllTasks()
            Log.d("ViewTaskListFragment", "All tasks: ${allTasks.size}")
            allTasks.forEach { Log.d("ViewTaskListFragment", "Task: ${it.noteTitle}, Deadline: ${it.ddl}") }

            // Query tasks from database
            val tasksWithinThreeDays = database.taskDao().getTasksWithinThreeDays(formattedSelectedDate)
            val tasksBetweenThreeAndFiveDays = database.taskDao().getTasksBetweenThreeAndFiveDays(formattedSelectedDate)
            val tasksAfterSevenDays = database.taskDao().getTasksAfterSevenDays(formattedSelectedDate)

            tasksWithinThreeDays.forEach { Log.d("ViewTaskListFragment", "Task within 3 days: ${it.noteTitle}") }
            tasksBetweenThreeAndFiveDays.forEach { Log.d("ViewTaskListFragment", "Task within 3-5 days: ${it.noteTitle}") }
            tasksAfterSevenDays.forEach { Log.d("ViewTaskListFragment", "Task after 7 days: ${it.noteTitle}") }

            withContext(Dispatchers.Main) {
                val groupedItems = mutableListOf<Any>()

                // Add "Upcoming in 3 Days" section
                if (tasksWithinThreeDays.isNotEmpty()) {
                    groupedItems.add("Upcoming in 3 Days") // Header as a String
                    groupedItems.addAll(tasksWithinThreeDays) // Tasks
                }

                // Add "Upcoming in 7 Days" section
                if (tasksBetweenThreeAndFiveDays.isNotEmpty()) {
                    groupedItems.add("Upcoming in 7 Days")
                    groupedItems.addAll(tasksBetweenThreeAndFiveDays)
                }

                // Add "Upcoming in Future" section
                if (tasksAfterSevenDays.isNotEmpty()) {
                    groupedItems.add("Upcoming in Future")
                    groupedItems.addAll(tasksAfterSevenDays)
                }

                // Update RecyclerView with grouped items
                taskListAdapter.updateTaskItems(groupedItems)
            }
        }
    }

    private fun addTask(task: Task) {
        GlobalScope.launch(Dispatchers.IO) {
            database.taskDao().insertTask(task)
            withContext(Dispatchers.Main) {
                // Add task to RecyclerView
                taskListAdapter.addTask(task)
                loadGroupedTasks(task.ddl)
            }
        }
    }
}
