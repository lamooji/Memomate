package com.cs407.memoMate

import android.os.Bundle
import android.view.*
import android.widget.ImageView
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

/**
 * Main page fragment that displays the task list and handles task management operations
 * Implements TaskDialogListener to handle task creation and editing
 */
class MainPageFragment : Fragment(), AddTaskMenu.TaskDialogListener {

    // Adapter for managing task list display
    private lateinit var taskAdapter: TaskAdapter
    // Mutable list to hold current tasks
    private val tasks = mutableListOf<Task>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_page, container, false)
    }

    /**
     * Initializes UI components and sets up event listeners
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up RecyclerView for task list
        val recyclerView = view.findViewById<RecyclerView>(R.id.checklistRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        taskAdapter = TaskAdapter(tasks)
        recyclerView.adapter = taskAdapter

        // Initialize data and UI components
        loadTasksFromDatabase()

        // Set up menu functionality
        val menuIcon = view.findViewById<ImageView>(R.id.menuIcon)
        menuIcon.setOnClickListener { showPopupMenu(menuIcon) }

        // Set up ChatGPT navigation
        val chatGPTButton = view.findViewById<View>(R.id.chatgptButton)
        chatGPTButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChatGPTFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    /**
     * Displays the popup menu with task management options
     * @param anchor View to anchor the popup menu to
     */
    private fun showPopupMenu(anchor: View) {
        val popup = androidx.appcompat.widget.PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.main_page_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_add -> {
                    // Show dialog to add new task
                    val addTaskMenu = AddTaskMenu()
                    addTaskMenu.setListener(this)
                    addTaskMenu.show(parentFragmentManager, "AddTaskMenu")
                    true
                }
                R.id.action_edit -> {
                    showEditTaskDialog()
                    true
                }
                R.id.action_delete -> {
                    showDeleteTaskDialog()
                    true
                }
                R.id.action_calendar -> {
                    // Navigate to calendar view
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CalendarFragment())
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.action_matrix -> {
                    // Navigate to urgency-importance matrix
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, UrgentImportantMatrixFragment())
                        .addToBackStack(null)
                        .commit()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    /**
     * Loads and sorts tasks from the database
     * Tasks are sorted by significance level
     */
    private fun loadTasksFromDatabase() {
        val db = NoteDatabase.getDatabase(requireContext())
        val taskDao = db.taskDao()

        lifecycleScope.launch(Dispatchers.IO) {
            val taskList = taskDao.getAllTasks()
            val sortedTaskList = taskList.sortedBy { it.significance }

            withContext(Dispatchers.Main) {
                tasks.clear()
                tasks.addAll(sortedTaskList)
                taskAdapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * Shows dialog for selecting and editing an existing task
     * Displays a toast message if no tasks exist
     */
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

    /**
     * Shows dialog for selecting and deleting a task
     * Displays a toast message if no tasks exist
     */
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

    /**
     * Deletes a task from the database
     * @param task The task to delete
     */
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
                loadTasksFromDatabase()
            }
        }
    }

    /**
     * Handles task creation and updating
     * Maps importance levels to significance values and prevents duplicate tasks
     */
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
                // Create new task
                val significanceValue = when (importance) {
                    1 -> 1 // High priority (Red)
                    2 -> 2 // Medium priority (Yellow)
                    3 -> 3 // Low priority (Green)
                    else -> 3
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

                // Check for duplicates
                val existingTasks = taskDao.getAllTasks()
                if (existingTasks.any { it.noteTitle == newTask.noteTitle && it.ddl == newTask.ddl }) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Task already exists.", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                taskDao.insertTask(newTask)
            } else {
                // Update existing task
                val significanceValue = when (importance) {
                    1 -> 1
                    2 -> 2
                    3 -> 3
                    else -> task.significance
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

            withContext(Dispatchers.Main) {
                loadTasksFromDatabase()
            }
        }
    }
}