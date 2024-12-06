// AddTaskMenu.kt
package com.cs407.memoMate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.cs407.memoMate.databinding.AddTaskMenuBinding

class AddTaskMenu : DialogFragment() {
    private var _binding: AddTaskMenuBinding? = null
    private val binding get() = _binding!!
    private var task: TaskItem? = null

    interface TaskDialogListener {
        fun onTaskAdded(
            name: String,
            ddl: String,
            isFinished: Boolean,
            note: String,
            importance: Int,
            task: TaskItem? = null
        )
    }

    private var listener: TaskDialogListener? = null

    companion object {
        private const val ARG_TASK = "task"

        fun newInstance(task: TaskItem? = null): AddTaskMenu {
            val fragment = AddTaskMenu()
            val args = Bundle()
            args.putSerializable(ARG_TASK, task)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            task = it.getSerializable(ARG_TASK) as? TaskItem
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

    // Add this method to adjust the dialog size
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pre-populate fields if editing
        if (task != null) {
            binding.nameEditText.setText(task!!.name)
            binding.ddlEditText.setText(task!!.ddl)
            binding.finishedCheckbox.isChecked = task!!.isChecked
            binding.noteEditText.setText(task!!.note)
            binding.importanceEditText.setText(task!!.importance.toString())
            binding.addButton.text = "Save"
        }

        binding.addButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val ddl = binding.ddlEditText.text.toString()
            val isFinished = binding.finishedCheckbox.isChecked
            val note = binding.noteEditText.text.toString()
            val importanceText = binding.importanceEditText.text.toString()
            val importance = importanceText.toIntOrNull() ?: 3  // Default to 3 if invalid input

            listener?.onTaskAdded(name, ddl, isFinished, note, importance, task)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setListener(listener: TaskDialogListener) {
        this.listener = listener
    }
}
