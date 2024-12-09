package com.cs407.memoMate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import android.widget.Toast
import com.cs407.memoMate.Data.NoteDatabase
import com.cs407.memoMate.Data.Task
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ScreenFragment : Fragment(), AddTaskMenu.TaskDialogListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.main_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val iconImageView: ImageView = view.findViewById(R.id.icon_image)

        // Get the current day of the month
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        // Dynamically get the resource ID for the image based on the day
        val resourceName = "icon_$currentDay"
        val resourceId = resources.getIdentifier(resourceName, "drawable", requireContext().packageName)

        if (resourceId != 0) {
            // If the resource exists, set it as the image
            iconImageView.setImageResource(resourceId)
        }

        // Calendar Button Navigation
        view.findViewById<LinearLayout>(R.id.calendar_button).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CalendarFragment())
                .addToBackStack(null)
                .commit()
        }

        // Matrix Button Navigation
        view.findViewById<LinearLayout>(R.id.matrix_button).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UrgentImportantMatrixFragment())
                .addToBackStack(null)
                .commit()
        }

        // Today's Task Button Navigation
        view.findViewById<LinearLayout>(R.id.todays_task_button).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MainPageFragment())
                .addToBackStack(null)
                .commit()
        }

        // GPT
        val askAIButton = view.findViewById<LinearLayout>(R.id.ask_ai_button)
        askAIButton.setOnClickListener {
            val chatOverlayDialog = ChatOverlayDialogFragment()
            chatOverlayDialog.show(parentFragmentManager, "ChatOverlayDialog")
        }

        // Edit button
        view.findViewById<MaterialTextView>(R.id.edit_button).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ViewTaskListFragment())
                .addToBackStack(null)
                .commit()
        }

        // Add New Task button (similar logic to MainPageFragment)
        val addNewTaskButton = view.findViewById<LinearLayout>(R.id.add_new_task_button)
        addNewTaskButton.setOnClickListener {
            val addTaskMenu = AddTaskMenu()
            addTaskMenu.setListener(this) // Set this fragment as the listener
            addTaskMenu.show(parentFragmentManager, "AddTaskMenu")
        }
    }

    // Implement the AddTaskMenu.TaskDialogListener interface methods:
    override fun onTaskAdded(
        name: String,
        ddl: String,
        isFinished: Boolean,
        note: String,
        importance: Int,
        task: Task?
    ) {
        // This logic is similar to what's done in MainPageFragment:
        val db = NoteDatabase.getDatabase(requireContext())
        val taskDao = db.taskDao()

        // Since this fragment doesn't maintain a task list like MainPageFragment,
        // we only insert/update the database. If needed, you can also refresh UI or navigate.
        CoroutineScope(Dispatchers.IO).launch {
            if (task == null) {
                // Adding a new task
                val newTask = Task(
                    noteId = 0,
                    significance = 0,
                    ddl = ddl,
                    finished = isFinished,
                    noteTitle = name,
                    noteAbstract = note,
                    importance = importance
                )

                // Prevent duplicate tasks with the same title and deadline
                val existingTasks = taskDao.getAllTasks()
                if (existingTasks.any { it.noteTitle == newTask.noteTitle && it.ddl == newTask.ddl }) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Task already exists.", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                taskDao.insertTask(newTask)
            } else {
                // Editing an existing task
                val updatedTask = task.copy(
                    noteTitle = name,
                    ddl = ddl,
                    finished = isFinished,
                    noteAbstract = note,
                    importance = importance
                )
                taskDao.updateTask(updatedTask)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Task saved successfully.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
