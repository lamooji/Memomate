package com.cs407.memoMate

import TaskViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageButton
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

/**
 * Fragment that implements the Urgent-Important Matrix (Eisenhower Matrix) visualization
 * This matrix helps users prioritize tasks based on their urgency and importance
 */
class UrgentImportantMatrixFragment : Fragment() {

    // ViewModel to handle task-related data operations
    private lateinit var taskViewModel: TaskViewModel

    /**
     * Creates and returns the view hierarchy associated with the fragment
     * Initializes the database, ViewModel, and sets up the UI components
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_urgent_important_matrix, container, false)

        // Initialize database and ViewModel for task management
        val db = NoteDatabase.getDatabase(requireContext())
        val factory = TaskViewModelFactory(db.taskDao())
        taskViewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]

        // Set up back button navigation
        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Get reference to the main container for the matrix
        val matrixContainer = view.findViewById<FrameLayout>(R.id.matrix_container)

        // Start observing tasks and organize them in the matrix
        observeTasksAndOrganizeMatrix(matrixContainer)

        return view
    }

    /**
     * Sets up observation of tasks and organizes them in the matrix layout
     * Waits for the container layout to be ready before organizing tasks
     * @param matrixContainer The FrameLayout that will contain the matrix visualization
     */
    private fun observeTasksAndOrganizeMatrix(matrixContainer: FrameLayout) {
        matrixContainer.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Check if fragment is still valid and attached
                if (view == null || !isAdded) {
                    matrixContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    return
                }

                // Proceed only when container dimensions are available
                if (matrixContainer.width > 0 && matrixContainer.height > 0) {
                    // Observe tasks and update matrix when task list changes
                    taskViewModel.getAllTasks().observe(viewLifecycleOwner) { tasks ->
                        if (view != null) {
                            organizeTasks(matrixContainer, tasks)
                        }
                    }
                    // Remove listener once initial layout is complete
                    matrixContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        })
    }

    /**
     * Organizes tasks in the matrix based on their importance and urgency
     * Tasks are positioned vertically by importance and horizontally by urgency
     * @param matrixContainer The container where tasks will be displayed
     * @param tasks List of tasks to be organized in the matrix
     */
    private fun organizeTasks(matrixContainer: FrameLayout, tasks: List<Task>) {
        matrixContainer.removeAllViews()

        val now = Date()
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val containerWidth = matrixContainer.width
        val containerHeight = matrixContainer.height

        if (containerWidth == 0 || containerHeight == 0) return

        // Define color scheme for different importance levels
        val importanceColors = mapOf(
            1 to R.color.high_importance,    // Red for high importance
            2 to R.color.medium_importance,  // Yellow for medium importance
            3 to R.color.low_importance      // Green for low importance
        )

        // Calculate section dimensions
        val sectionHeight = containerHeight / 3
        val sectionWidth = containerWidth - 40 // Account for padding

        // Group and position tasks by importance level
        tasks.groupBy { it.importance }.forEach { (importance, taskList) ->
            val color = ContextCompat.getColor(
                requireContext(),
                importanceColors[importance] ?: R.color.light_gray
            )

            // Calculate vertical position with offset adjustment
            val yOffset = 250
            val yPosition = when (importance) {
                1 -> (importance - 1) * sectionHeight + yOffset    // High importance section
                3 -> (importance - 1) * sectionHeight - yOffset    // Low importance section
                else -> (importance - 1) * sectionHeight           // Medium importance section
            }

            // Position each task within its importance section
            taskList.forEachIndexed { index, task ->
                // Calculate task due date and urgency
                val taskDate = try {
                    dateFormat.parse(task.ddl)
                } catch (e: Exception) {
                    null
                } ?: now

                val daysUntilDue = ((taskDate.time - now.time) / (1000 * 60 * 60 * 24)).toInt()
                val xPosition = calculateXPosition(daysUntilDue, sectionWidth)

                // Create and position task view
                val taskView = createTaskView(task, color)
                val taskHeight = max(sectionHeight / (taskList.size + 1), 100)

                val layoutParams = FrameLayout.LayoutParams(200, taskHeight).apply {
                    marginStart = min(xPosition, sectionWidth - 200)
                    topMargin = yPosition + (index * taskHeight)
                }

                matrixContainer.addView(taskView, layoutParams)
            }
        }
    }

    /**
     * Calculates the horizontal position of a task based on its urgency (days until due)
     * @param daysUntilDue Number of days until the task is due
     * @param sectionWidth Width of the matrix section
     * @return Horizontal position (x-coordinate) for the task
     */
    private fun calculateXPosition(daysUntilDue: Int, sectionWidth: Int): Int {
        return when {
            daysUntilDue > 3 -> 0                                        // Not urgent (left side)
            daysUntilDue <= 0 -> sectionWidth - 200                      // Most urgent (right side)
            else -> ((sectionWidth - 200) / 3) * (3 - daysUntilDue)     // In between
        }
    }

    /**
     * Creates a TextView to represent a task in the matrix
     * @param task The task to be displayed
     * @param color Background color based on task importance
     * @return Configured TextView representing the task
     */
    private fun createTaskView(task: Task, color: Int): TextView {
        return TextView(requireContext()).apply {
            text = "${task.noteTitle}\nDue: ${task.ddl}"
            setBackgroundColor(color)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            textSize = 14f
            setPadding(16, 16, 16, 16)
            elevation = 8f  // Add shadow for depth
        }
    }
}