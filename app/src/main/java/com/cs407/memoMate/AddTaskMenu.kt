package com.cs407.memoMate

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.cs407.memoMate.Data.Task
import com.cs407.memoMate.databinding.AddTaskMenuBinding

class AddTaskMenu : DialogFragment() {

    private var _binding: AddTaskMenuBinding? = null
    private val binding get() = _binding!!
    private var task: Task? = null

    interface TaskDialogListener {
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
        private const val ARG_TASK = "task"

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
        arguments?.let {
            task = it.getParcelable(ARG_TASK)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddTaskMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If editing an existing task, populate fields
        task?.let {
            binding.nameEditText.setText(it.noteTitle)
            binding.ddlEditText.setText(it.ddl)
            binding.finishedCheckbox.isChecked = it.finished
            binding.noteEditText.setText(it.noteAbstract)
            binding.importanceSlider.value = it.significance.toFloat()
            updateImportanceLabel(it.significance)
        } ?: run {
            // Default importance to Red=1 if new task
            binding.importanceSlider.value = 1.0f
            updateImportanceLabel(1)
        }

        // Add a TextWatcher to automatically insert '/' characters for DDL
        binding.ddlEditText.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var prevLength = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                if (s == null) return

                isFormatting = true

                val length = s.length
                // After typing 2 digits, add a slash if it's not already there
                if (length == 2 && prevLength < length) {
                    s.append("/")
                }
                // After typing 5 characters (which includes "MM/"), add another slash if not already present
                else if (length == 5 && prevLength < length) {
                    s.append("/")
                }

                prevLength = s.length
                isFormatting = false
            }
        })

        // Update importance label when slider changes
        binding.importanceSlider.addOnChangeListener { _, value, _ ->
            updateImportanceLabel(value.toInt())
        }

        binding.addButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val ddl = binding.ddlEditText.text.toString().trim()
            val isFinished = binding.finishedCheckbox.isChecked
            val note = binding.noteEditText.text.toString().trim()

            // Get importance from the slider
            val importance = binding.importanceSlider.value.toInt()

            // Validate the DDL format: MM/dd/yyyy
            val dateRegex = Regex("^\\d{2}/\\d{2}/\\d{4}\$")
            if (!dateRegex.matches(ddl)) {
                Toast.makeText(requireContext(), "Date must be in MM/dd/yyyy format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            listener?.onTaskAdded(name, ddl, isFinished, note, importance, task)
            dismiss()
        }
    }

    private fun updateImportanceLabel(importance: Int) {
        val importanceText = when (importance) {
            1 -> "Red (1)"
            2 -> "Yellow (2)"
            3 -> "Green (3)"
            else -> "Green (3)"
        }
        binding.importanceLabel.text = "Importance: $importanceText"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setListener(listener: TaskDialogListener) {
        this.listener = listener
    }
}
