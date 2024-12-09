package com.cs407.memoMate

import TaskViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cs407.memoMate.Data.NoteDatabase
import com.cs407.memoMate.Data.Task
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

class UrgentImportantMatrixFragment : Fragment() {

    private lateinit var taskViewModel: TaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_urgent_important_matrix, container, false)

        // Initialize database and ViewModel
        val db = NoteDatabase.getDatabase(requireContext())
        val factory = TaskViewModelFactory(db.taskDao())
        taskViewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]

        // Parent layout
        val matrixContainer = view.findViewById<FrameLayout>(R.id.matrix_container)

        // Observe tasks and organize them
        observeTasksAndOrganizeMatrix(matrixContainer)

        return view
    }

    private fun observeTasksAndOrganizeMatrix(matrixContainer: FrameLayout) {
        matrixContainer.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (view == null || !isAdded) {
                    matrixContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    return
                }

                // Check if the container dimensions are ready
                if (matrixContainer.width > 0 && matrixContainer.height > 0) {
                    taskViewModel.getAllTasks().observe(viewLifecycleOwner) { tasks ->
                        if (view != null) { // Ensure the fragment's view still exists
                            organizeTasks(matrixContainer, tasks)
                        }
                    }
                    matrixContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        })
    }

    private fun organizeTasks(matrixContainer: FrameLayout, tasks: List<Task>) {
        matrixContainer.removeAllViews()

        val now = Date()
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val containerWidth = matrixContainer.width
        val containerHeight = matrixContainer.height

        if (containerWidth == 0 || containerHeight == 0) return // Ensure layout dimensions are ready

        // Group tasks by importance
        val tasksByImportance = tasks.groupBy { it.importance }
        val importanceColors = mapOf(
            1 to R.color.high_importance, // High importance
            2 to R.color.medium_importance, // Medium importance
            3 to R.color.low_importance // Low importance
        )

        val sectionHeight = containerHeight / 3
        val sectionWidth = containerWidth - 40 // Padding for task placement

        tasksByImportance.forEach { (importance, taskList) ->
            val color = ContextCompat.getColor(
                requireContext(),
                importanceColors[importance] ?: R.color.light_gray
            )

            // Adjust yPosition to lower the placement of the urgent section
            val yOffset = 250 // Define a base offset for adjustments
            val yPosition = when (importance) {
                1 -> (importance - 1) * sectionHeight + yOffset // Lower the high-importance (red) box
                3 -> (importance - 1) * sectionHeight - yOffset // Raise the low-importance (green) box
                else -> (importance - 1) * sectionHeight // Default positioning for medium importance
            }

            taskList.forEachIndexed { index, task ->
                val taskDate = try {
                    dateFormat.parse(task.ddl)
                } catch (e: Exception) {
                    null
                } ?: now

                val daysUntilDue = ((taskDate.time - now.time) / (1000 * 60 * 60 * 24)).toInt()

                // Calculate X position based on urgency
                val xPosition = calculateXPosition(daysUntilDue, sectionWidth)

                // Create task view
                val taskView = createTaskView(task, color)

                // Position task
                val taskHeight = max(sectionHeight / (taskList.size + 1), 100) // Minimum height
                val layoutParams = FrameLayout.LayoutParams(200, taskHeight).apply {
                    marginStart = min(xPosition, sectionWidth - 200) // Prevent overflow
                    topMargin = yPosition + (index * taskHeight) // Spread within section
                }

                matrixContainer.addView(taskView, layoutParams)
            }
        }
    }


    private fun calculateXPosition(daysUntilDue: Int, sectionWidth: Int): Int {
        return when {
            daysUntilDue > 3 -> 0 // Far left for tasks > 3 days
            daysUntilDue <= 0 -> sectionWidth - 200 // Rightmost for tasks due today
            else -> ((sectionWidth - 200) / 3) * (3 - daysUntilDue) // Spread between far left and right
        }
    }

    private fun createTaskView(task: Task, color: Int): TextView {
        return TextView(requireContext()).apply {
            text = "${task.noteTitle}\nDue: ${task.ddl}"
            setBackgroundColor(color)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            textSize = 14f
            setPadding(16, 16, 16, 16)
            elevation = 8f // Add elevation for better visibility
        }
    }
}
