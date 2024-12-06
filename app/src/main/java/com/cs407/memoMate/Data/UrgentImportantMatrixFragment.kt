package com.cs407.memoMate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cs407.memoMate.Data.NoteDatabase
import com.cs407.memoMate.Data.Task
import com.cs407.memoMate.Data.TaskDao
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.Handler
import android.os.Looper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class UrgentImportantMatrixFragment : Fragment() {
    private lateinit var matrixContainer: RelativeLayout
    private lateinit var taskDao: TaskDao
    private val mainHandler = Handler(Looper.getMainLooper())
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.urgent_important_matrix, container, false)

        // Initialize views
        matrixContainer = view.findViewById(R.id.matrix_container)

        // Update current date
        updateCurrentDate(view)

        // Initialize database
        val db = NoteDatabase.getDatabase(requireContext())
        taskDao = db.taskDao()

        // Setup back button
        view.findViewById<Button>(R.id.back_button).setOnClickListener {
            // Navigate back
            findNavController().navigateUp()
        }

        // Load tasks
        loadTasks()

        return view
    }

    private fun updateCurrentDate(view: View) {
        val dateText = view.findViewById<TextView>(R.id.date_text)
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        val currentDate = Date()
        dateText.text = "Today is ${sdf.format(currentDate)}"
    }

    private fun loadTasks() {
        executorService.execute {
            try {
                val tasks = taskDao.getAllTasks()
                mainHandler.post {
                    matrixContainer.removeAllViews()
                    tasks.forEach { task ->
                        if (!task.finished) {
                            addTaskCard(task)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun addTaskCard(task: Task) {
        val taskCard = layoutInflater.inflate(R.layout.task_card, null)

        // Set task details
        taskCard.findViewById<TextView>(R.id.task_name).text = task.noteTitle
        taskCard.findViewById<TextView>(R.id.task_date).text =
            SimpleDateFormat("MM/dd", Locale.getDefault()).format(task.ddl)

        // Calculate position parameters
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        // Set vertical position based on significance
        params.topMargin = when (task.significance) {
            3 -> 50    // High importance
            2 -> 200   // Medium importance
            else -> 350 // Low importance
        }

        // Calculate days until deadline
        val daysDiff = ((task.ddl.time - Date().time) / (1000 * 60 * 60 * 24)).toInt()

        // Set horizontal position based on deadline
        params.marginStart = when {
            daysDiff <= 3 -> 250  // Within 3 days
            daysDiff <= 7 -> 130  // 3-7 days
            else -> 10            // More than 7 days
        }

        taskCard.layoutParams = params
        matrixContainer.addView(taskCard)

        // Add click listener to task card
        taskCard.setOnClickListener {
            // TODO: Navigate to task details
            // findNavController().navigate(UrgentImportantMatrixFragmentDirections.actionToTaskDetail(task.noteId))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executorService.shutdown()
    }
}