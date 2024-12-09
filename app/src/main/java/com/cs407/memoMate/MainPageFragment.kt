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

        // Load tasks from database
        loadTasksFromDatabase()

        // Initialize the menu icon
        val menuIcon = view.findViewById<ImageView>(R.id.menuIcon)
        menuIcon.setOnClickListener { showPopupMenu(menuIcon) }

        // ChatGPT button remains directly accessible
        val chatGPTButton = view.findViewById<View>(R.id.chatgptButton)
        chatGPTButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChatGPTFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun showPopupMenu(anchor: View) {
        val popup = androidx.appcompat.widget.PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.main_page_menu, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_add -> {
                    // Same action as addButton previously
                    val addTaskMenu = AddTaskMenu()
                    addTaskMenu.setListener(this)
                    addTaskMenu.show(parentFragmentManager, "AddTaskMenu")
                    true
                }
                R.id.action_edit -> {
                    // Same action as editButton previously
                    showEditTaskDialog()
                    true
                }
                R.id.action_delete -> {
                    // Same action as deleteButton previously
                    showDeleteTaskDialog()
                    true
                }
                R.id.action_calendar -> {
                    // Same as calendarButton previously
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CalendarFragment())
                        .addToBackStack(null)
                        .commit()
                    true
                }
                R.id.action_matrix -> {
                    // Same as topButton previously
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
                val significanceValue = when (importance) {
                    1 -> 1 // Red
                    2 -> 2 // Yellow
                    3 -> 3 // Green
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

                // Prevent duplicate tasks
                val existingTasks = taskDao.getAllTasks()
                if (existingTasks.any { it.noteTitle == newTask.noteTitle && it.ddl == newTask.ddl }) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Task already exists.", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                taskDao.insertTask(newTask)
            } else {
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
