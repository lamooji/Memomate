package com.cs407.memoMate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.cs407.memoMate.Data.Task
import com.cs407.memoMate.databinding.AddTaskMenuBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A DialogFragment that provides a form interface for adding or editing tasks.
 * This dialog supports both creation of new tasks and modification of existing ones,
 * with input validation and importance level selection.
 */
class AddTaskMenu : DialogFragment() {

    // View binding for safe view access, nulled in onDestroyView to prevent memory leaks
    private var _binding: AddTaskMenuBinding? = null
    private val binding get() = _binding!!

    // Stores the task being edited, null when creating a new task
    private var task: Task? = null

    /**
     * Interface for communicating task additions/edits back to the parent fragment
     * This follows the observer pattern for loose coupling between components
     */
    interface TaskDialogListener {
        /**
         * Called when a task is saved (either added or edited)
         * @param name Task title/name
         * @param ddl Deadline in MM/DD/YYYY format
         * @param isFinished Task completion status
         * @param note Additional task details
         * @param importance Priority level (1=High/Red, 2=Medium/Yellow, 3=Low/Green)
         * @param task Original task if editing, null if creating new
         */
        fun onTaskAdded(
            name: String,
            ddl: String,
            isFinished: Boolean,
            note: String,
            importance: Int,
            task: Task? = null
        )
    }

    private var listener: TaskDialogListener? = null

    companion object {
        // Key for task argument in Bundle
        private const val ARG_TASK = "task"

        /**
         * Factory method to create new instances of AddTaskMenu
         * @param task Optional task for editing mode
         * @return New AddTaskMenu instance with arguments set
         */
        fun newInstance(task: Task? = null): AddTaskMenu {
            val fragment = AddTaskMenu()
            val args = Bundle()
            args.putParcelable(ARG_TASK, task)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve task from arguments if editing an existing task
        arguments?.let {
            task = it.getParcelable(ARG_TASK)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize view binding
        _binding = AddTaskMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // Configure dialog window to match parent width while wrapping content height
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize form fields based on whether we're editing or creating
        task?.let {
            // Populate fields with existing task data
            binding.nameEditText.setText(it.noteTitle)
            binding.ddlEditText.setText(it.ddl)
            binding.finishedCheckbox.isChecked = it.finished
            binding.noteEditText.setText(it.noteAbstract)
            binding.importanceSlider.value = it.significance.toFloat()
            updateImportanceLabel(it.significance)
        } ?: run {
            // Set default values for new task
            binding.importanceSlider.value = 1.0f  // Default to high importance
            updateImportanceLabel(1)
        }

        // Update importance label whenever slider value changes
        binding.importanceSlider.addOnChangeListener { _, value, _ ->
            updateImportanceLabel(value.toInt())
        }

        // Handle form submission
        binding.addButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val ddl = binding.ddlEditText.text.toString()
            val isFinished = binding.finishedCheckbox.isChecked
            val note = binding.noteEditText.text.toString()
            val importance = binding.importanceSlider.value.toInt()

            // Validate required fields
            if (name.isBlank()) {
                binding.nameEditText.error = "Task name cannot be empty"
                return@setOnClickListener
            }

            // Validate deadline format
            if (!isValidDeadlineFormat(ddl)) {
                binding.ddlEditText.error = "Please enter a valid date format (MM/DD/YYYY)"
                return@setOnClickListener
            }

            // Submit task if validation passes
            listener?.onTaskAdded(name, ddl, isFinished, note, importance, task)
            dismiss()
        }
    }

    /**
     * Validates that a deadline string matches the required MM/DD/YYYY format
     * and represents a valid calendar date
     *
     * @param ddl The deadline string to validate
     * @return true if the date is valid, false otherwise
     */
    private fun isValidDeadlineFormat(ddl: String): Boolean {
        if (ddl.isBlank()) return false

        // Validate format using regex pattern for MM/DD/YYYY
        val pattern = """^(0[1-9]|1[0-2])/(0[1-9]|[12]\d|3[01])/\d{4}$""".toRegex()
        if (!pattern.matches(ddl)) return false

        try {
            // Parse and validate individual date components
            val parts = ddl.split("/")
            val month = parts[0].toInt()
            val day = parts[1].toInt()
            val year = parts[2].toInt()

            // Calculate days in month, accounting for leap years
            val daysInMonth = when (month) {
                2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
                4, 6, 9, 11 -> 30
                else -> 31
            }

            return day <= daysInMonth
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Updates the importance label text based on the selected importance level
     *
     * @param importance The importance level (1=Red/High, 2=Yellow/Medium, 3=Green/Low)
     */
    private fun updateImportanceLabel(importance: Int) {
        val importanceText = when (importance) {
            1 -> "Red (1)"      // High priority
            2 -> "Yellow (2)"   // Medium priority
            3 -> "Green (3)"    // Low priority
            else -> "Green (3)" // Default to low priority
        }
        binding.importanceLabel.text = "Importance: $importanceText"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear binding reference to prevent memory leaks
        _binding = null
    }

    /**
     * Sets the listener for task addition/editing events
     *
     * @param listener The TaskDialogListener implementation to receive events
     */
    fun setListener(listener: TaskDialogListener) {
        this.listener = listener
    }
}