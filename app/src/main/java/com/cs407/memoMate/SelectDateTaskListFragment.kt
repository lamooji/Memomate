package com.cs407.memoMate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.memoMate.Data.NoteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class SelectedTaskListFragment : Fragment() {

    private lateinit var taskRecyclerView: RecyclerView
    private lateinit var taskListAdapter: SelectedTaskListAdapter
    private lateinit var database: NoteDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_task_list, container, false)

        // Retrieve the selected date
        val selectedDateStr = arguments?.getString("selected_date")
        val selectedDate: Date? = selectedDateStr?.let {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.parse(it) // Convert string to Date
        }

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
        taskListAdapter = SelectedTaskListAdapter(mutableListOf())
        taskRecyclerView.adapter = taskListAdapter

        // Initialize database and load tasks
        database = NoteDatabase.getDatabase(requireContext())
        if (selectedDate != null) {
            loadTasksForSelectedDate(selectedDate)
        }

        val backButton: ImageView = view.findViewById(R.id.back_button)
        val addTaskButton: ImageView = view.findViewById(R.id.add_task_button)

        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }


        addTaskButton.visibility = View.GONE

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

    private fun loadTasksForSelectedDate(selectedDate: Date) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(selectedDate)
        Log.d("SelectedTaskListFragment", "Formatted date for filtering: $formattedDate")

        GlobalScope.launch(Dispatchers.IO) {
            val allTasks = database.taskDao().getAllTasks() // Fetch all tasks

            // Filter tasks where ddl matches the selected date
            val filteredTasks = allTasks.filter { task ->
                dateFormat.format(task.ddl) == formattedDate
            }

            withContext(Dispatchers.Main) {
                taskListAdapter.updateTaskItems(filteredTasks)
                filteredTasks.forEach { task ->
                    Log.d("SelectedTaskListFragment", "Filtered Task: ${task.noteTitle}, Date: ${task.ddl}")
                }
            }
        }
    }


}
