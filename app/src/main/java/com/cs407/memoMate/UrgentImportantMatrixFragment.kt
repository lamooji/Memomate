package com.cs407.memoMate

import TaskViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cs407.memoMate.Data.NoteDatabase
import com.cs407.memoMate.Data.Task

class UrgentImportantMatrixFragment : Fragment() {

    private lateinit var taskViewModel: TaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_urgent_important_matrix, container, false)

        // Initialize database and ViewModel with custom factory
        val db = NoteDatabase.getDatabase(requireContext())
        val factory = TaskViewModelFactory(db.taskDao())
        taskViewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]

        // Get references to the quadrant layouts
        val urgentImportant = view.findViewById<LinearLayout>(R.id.urgent_important_quadrant)
        val urgentNotImportant = view.findViewById<LinearLayout>(R.id.urgent_not_important_quadrant)
        val notUrgentImportant = view.findViewById<LinearLayout>(R.id.not_urgent_important_quadrant)
        val notUrgentNotImportant = view.findViewById<LinearLayout>(R.id.not_urgent_not_important_quadrant)

        // Observe and populate each quadrant
        observeTasksAndPopulateQuadrants(urgentImportant, urgentNotImportant, notUrgentImportant, notUrgentNotImportant)

        return view
    }

    private fun observeTasksAndPopulateQuadrants(
        urgentImportant: LinearLayout,
        urgentNotImportant: LinearLayout,
        notUrgentImportant: LinearLayout,
        notUrgentNotImportant: LinearLayout
    ) {
        taskViewModel.getUrgentAndImportantTasks().observe(viewLifecycleOwner) { tasks ->
            populateTasks(urgentImportant, tasks, "No urgent and important tasks.")
        }

        taskViewModel.getUrgentButNotImportantTasks().observe(viewLifecycleOwner) { tasks ->
            populateTasks(urgentNotImportant, tasks, "No urgent but less important tasks.")
        }

        taskViewModel.getImportantButNotUrgentTasks().observe(viewLifecycleOwner) { tasks ->
            populateTasks(notUrgentImportant, tasks, "No important but not urgent tasks.")
        }

        taskViewModel.getNeitherUrgentNorImportantTasks().observe(viewLifecycleOwner) { tasks ->
            populateTasks(notUrgentNotImportant, tasks, "No tasks in this quadrant.")
        }
    }

    private fun populateTasks(quadrant: LinearLayout, tasks: List<Task>, emptyMessage: String) {
        quadrant.removeAllViews()
        if (tasks.isEmpty()) {
            val emptyView = TextView(requireContext()).apply {
                text = emptyMessage
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                textSize = 14f
                setPadding(16, 16, 16, 16)
            }
            quadrant.addView(emptyView)
            return
        }

        tasks.forEach { task ->
            val taskView = TextView(requireContext()).apply {
                text = "${task.noteTitle}\nDue: ${task.ddl}"
                setPadding(16, 16, 16, 16)
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_gray))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                textSize = 14f
            }
            quadrant.addView(taskView)
        }
    }
}
