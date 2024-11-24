// MainPageFragment.kt
package com.cs407.memoMate;

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.memoMate.R

class MainPageFragment : Fragment(), AddTaskMenu.TaskDialogListener {

    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<TaskItem>()

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

        addButton.setOnClickListener {
            val addTaskMenu = AddTaskMenu()
            addTaskMenu.setListener(this)
            addTaskMenu.show(parentFragmentManager, "AddTaskMenu")
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
    }


    // Implement the TaskDialogListener interface
    override fun onTaskAdded(name: String, ddl: String, isFinished: Boolean, note: String) {
        val newTask = TaskItem(name).apply {
            this.isChecked = isFinished
        }
        taskAdapter.addTask(newTask)
        Toast.makeText(requireContext(), "Task Added: $name", Toast.LENGTH_SHORT).show()
    }
}
