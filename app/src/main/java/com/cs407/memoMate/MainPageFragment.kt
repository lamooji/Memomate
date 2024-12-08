// MainPageFragment.kt
package com.cs407.memoMate;

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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

        // Initialize Buttons
        val addButton = view.findViewById<Button>(R.id.addButton)
        val calendarButton = view.findViewById<Button>(R.id.calendarButton)
        val topButton = view.findViewById<Button>(R.id.topButton) // New button
        val editButton = view.findViewById<Button>(R.id.editButton) // New Edit button
        addButton.setOnClickListener {
            val addTaskMenu = AddTaskMenu()
            addTaskMenu.setListener(this)
            addTaskMenu.show(parentFragmentManager, "AddTaskMenu")
        }
        editButton.setOnClickListener {
            showEditTaskDialog()
        }

        calendarButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CalendarFragment())
                .addToBackStack(null)
                .commit()
        }

        topButton.setOnClickListener {
            // Navigate to urgent_important_matrix.xml (assuming it's tied to a fragment)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UrgentImportantMatrixFragment())
                .addToBackStack(null)
                .commit()
        }

        val chatGPTButton = view.findViewById<Button>(R.id.chatgptButton)
        chatGPTButton.setOnClickListener {
            // Navigate to ChatGPTFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChatGPTFragment())
                .addToBackStack(null)
                .commit()
        }


    }

    private fun showEditTaskDialog() {
        val taskNames = tasks.map { it.name }.toTypedArray()
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Task to Edit")
            .setItems(taskNames) { dialog, which ->
                val selectedTask = tasks[which]
                val addTaskMenu = AddTaskMenu.newInstance(selectedTask)
                addTaskMenu.setListener(this)
                addTaskMenu.show(parentFragmentManager, "EditTaskMenu")
            }
        builder.show()
    }

    override fun onTaskAdded(
        name: String,
        ddl: String,
        isFinished: Boolean,
        note: String,
        importance: Int,
        task: Task?
    ) {
        if (task == null) {
            // Adding a new task
            val newTask = Task(name).apply {
                this.isChecked = isFinished
                this.ddl = ddl
                this.note = note
                this.importance = importance
            }
            taskAdapter.addTask(newTask)
            Toast.makeText(requireContext(), "Task Added: $name", Toast.LENGTH_SHORT).show()
        } else {
            // Editing an existing task
            val index = tasks.indexOf(task)
            if (index != -1) {
                task.name = name
                task.ddl = ddl
                task.isChecked = isFinished
                task.note = note
                task.importance = importance
                taskAdapter.notifyItemChanged(index)
                Toast.makeText(requireContext(), "Task Updated: $name", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

