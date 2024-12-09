package com.cs407.memoMate

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.memoMate.Data.NoteDatabase
import com.cs407.memoMate.Data.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainPageFragment : Fragment(), AddTaskMenu.TaskDialogListener {

    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.checklistRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        taskAdapter = TaskAdapter(tasks)
        recyclerView.adapter = taskAdapter

        // Load tasks from database (sorted ascending by significance: Red(1), Yellow(2), Green(3))
        loadTasksFromDatabase()

        // Initialize Buttons
        val addButton = view.findViewById<Button>(R.id.addButton)
        val deleteButton = view.findViewById<Button>(R.id.deleteButton)
        val editButton = view.findViewById<Button>(R.id.editButton)
        val calendarButton = view.findViewById<Button>(R.id.calendarButton)
        val topButton = view.findViewById<Button>(R.id.topButton)
        val chatGPTButton = view.findViewById<Button>(R.id.chatgptButton)

        addButton.setOnClickListener {
            val addTaskMenu = AddTaskMenu()
            addTaskMenu.setListener(this)
            addTaskMenu.show(parentFragmentManager, "AddTaskMenu")
        }

        editButton.setOnClickListener {
            showEditTaskDialog()
        }

        deleteButton.setOnClickListener {
            showDeleteTaskDialog()
        }

        calendarButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CalendarFragment())
                .addToBackStack(null)
                .commit()
        }

        topButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UrgentImportantMatrixFragment())
                .addToBackStack(null)
                .commit()
        }

        chatGPTButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChatGPTFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadTasksFromDatabase() {
        val db = NoteDatabase.getDatabase(requireContext())
        val taskDao = db.taskDao()

        lifecycleScope.launch(Dispatchers.IO) {
            val taskList = taskDao.getAllTasks()

            // Sort tasks by significance ascending: Red(1) > Yellow(2) > Green(3)
            // This puts Red at the top, Yellow in the middle, Green at the bottom.
            val sortedTaskList = taskList.sortedBy { it.significance }

            withContext(Dispatchers.Main) {
                tasks.clear()
                tasks.addAll(sortedTaskList)
                taskAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun showEditTaskDialog() {
        if (tasks.isEmpty()) {
            Toast.makeText(requireContext(), "No tasks to edit.", Toast.LENGTH_SHORT).show()
            return
        }

        val taskNames = tasks.map { it.noteTitle }.toTypedArray()
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Task to Edit")
            .setItems(taskNames) { _, which ->
                val selectedTask = tasks[which]
                val addTaskMenu = AddTaskMenu.newInstance(selectedTask)
                addTaskMenu.setListener(this)
                addTaskMenu.show(parentFragmentManager, "EditTaskMenu")
            }
        builder.show()
    }

    private fun showDeleteTaskDialog() {
        if (tasks.isEmpty()) {
            Toast.makeText(requireContext(), "No tasks to delete.", Toast.LENGTH_SHORT).show()
            return
        }

        val taskNames = tasks.map { it.noteTitle }.toTypedArray()
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Task to Delete")
            .setItems(taskNames) { _, which ->
                val selectedTask = tasks[which]
                deleteTask(selectedTask)
            }
        builder.show()
    }

    private fun deleteTask(task: Task) {
        val db = NoteDatabase.getDatabase(requireContext())
        val taskDao = db.taskDao()

        lifecycleScope.launch(Dispatchers.IO) {
            val rowsDeleted = taskDao.deleteTask(task)
            withContext(Dispatchers.Main) {
                if (rowsDeleted > 0) {
                    Toast.makeText(requireContext(), "Task deleted successfully.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to delete task.", Toast.LENGTH_SHORT).show()
                }
                // Refresh the list after deletion
                loadTasksFromDatabase()
            }
        }
    }

    override fun onTaskAdded(
        name: String,
        ddl: String,
        isFinished: Boolean,
        note: String,
        importance: Int,
        task: Task?
    ) {
        val db = NoteDatabase.getDatabase(requireContext())
        val taskDao = db.taskDao()

        lifecycleScope.launch(Dispatchers.IO) {
            if (task == null) {
                // Mapping importance to significance:
                // Red = 1, Yellow = 2, Green = 3
                val significanceValue = when (importance) {
                    1 -> 1 // Red
                    2 -> 2 // Yellow
                    3 -> 3 // Green
                    else -> 3 // Default to Green if invalid input
                }

                val newTask = Task(
                    noteId = 0,
                    significance = significanceValue,
                    ddl = ddl,
                    finished = isFinished,
                    noteTitle = name,
                    noteAbstract = note,
                    importance = importance
                )

                // Prevent duplicate tasks (same title and ddl)
                val existingTasks = taskDao.getAllTasks()
                if (existingTasks.any { it.noteTitle == newTask.noteTitle && it.ddl == newTask.ddl }) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Task already exists.", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                taskDao.insertTask(newTask)
            } else {
                // Editing an existing task
                val significanceValue = when (importance) {
                    1 -> 1 // Red
                    2 -> 2 // Yellow
                    3 -> 3 // Green
                    else -> task.significance // Keep old significance if invalid
                }

                val updatedTask = task.copy(
                    noteTitle = name,
                    ddl = ddl,
                    finished = isFinished,
                    noteAbstract = note,
                    importance = importance,
                    significance = significanceValue
                )
                taskDao.updateTask(updatedTask)
            }

            // Reload tasks to refresh UI
            withContext(Dispatchers.Main) {
                loadTasksFromDatabase()
            }
        }
    }
}
