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
import java.text.ParseException
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
        val selectedDate: String = selectedDateStr ?: getCurrentDate() // Default to today's date if not provided

        val sectionTitle = view.findViewById<TextView>(R.id.section_title)
        val formattedDate = formatDateWithSuffix(selectedDate)
        sectionTitle.text = "$formattedDate's Tasks"

        // Initialize RecyclerView
        taskRecyclerView = view.findViewById(R.id.task_recycler_view)
        taskRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        taskListAdapter = SelectedTaskListAdapter(mutableListOf())
        taskRecyclerView.adapter = taskListAdapter

        // Initialize database and load tasks
        database = NoteDatabase.getDatabase(requireContext())
        loadTasksForSelectedDate(selectedDate)

        val backButton: ImageView = view.findViewById(R.id.back_button)
        val addTaskButton: ImageView = view.findViewById(R.id.add_task_button)

        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        addTaskButton.visibility = View.GONE

        return view
    }

    private fun formatDateWithSuffix(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Ensure this matches the input format
        val date: Date? = try {
            inputFormat.parse(dateString) // Attempt to parse the date
        } catch (e: ParseException) {
            Log.e("DateError", "Unparseable date: $dateString. Error: ${e.message}")
            null
        }

        if (date == null) {
            return "Invalid Date"
        }

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

    private fun loadTasksForSelectedDate(selectedDate: String) {
        val dateFormatInput = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()) // Input format for "ddl" in the database
        val dateFormatOutput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Desired output format for filtering

        GlobalScope.launch(Dispatchers.IO) {
            val allTasks = database.taskDao().getAllTasks() // Fetch all tasks

            // Filter tasks where ddl matches the selected date
            val filteredTasks = allTasks.filter { task ->
                try {
                    val taskDate = dateFormatInput.parse(task.ddl) // Parse ddl as Date using the input format
                    val formattedTaskDate = dateFormatOutput.format(taskDate!!) // Reformat the parsed Date
                    formattedTaskDate == selectedDate // Compare with the selectedDate in "yyyy-MM-dd" format
                } catch (e: ParseException) {
                    Log.e("DateError", "Error parsing date: ${task.ddl}, Error: ${e.message}")
                    false
                }
            }

            withContext(Dispatchers.Main) {
                taskListAdapter.updateTaskItems(filteredTasks)
                filteredTasks.forEach { task ->
                    Log.d("SelectedTaskListFragment", "Filtered Task: ${task.noteTitle}, Date: ${task.ddl}")
                }
            }
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
