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
            binding.importanceSlider.value = it.significance.toFloat() // significance = importance here
            updateImportanceLabel(it.significance)
        } ?: run {
            // Default importance to Red=1 if new task
            binding.importanceSlider.value = 1.0f
            updateImportanceLabel(1)
        }

        // Listen for slider changes to update the label
        binding.importanceSlider.addOnChangeListener { _, value, _ ->
            updateImportanceLabel(value.toInt())
        }

        binding.addButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val ddl = binding.ddlEditText.text.toString()
            val isFinished = binding.finishedCheckbox.isChecked
            val note = binding.noteEditText.text.toString()

            // Get importance from the slider
            val importance = binding.importanceSlider.value.toInt()

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
