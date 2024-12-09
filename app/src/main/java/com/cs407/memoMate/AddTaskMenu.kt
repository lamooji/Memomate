package com.cs407.memoMate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.cs407.memoMate.Data.NoteDatabase
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
            args.putParcelable(ARG_TASK, task) // Use Parcelable for Task
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            task = it.getParcelable(ARG_TASK) // Retrieve the task if editing
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

        binding.addButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val ddl = binding.ddlEditText.text.toString()
            val isFinished = binding.finishedCheckbox.isChecked
            val note = binding.noteEditText.text.toString()
            val importanceText = binding.importanceEditText.text.toString()
            val importance = importanceText.toIntOrNull() ?: 3 // Default to 3 if invalid input

            val newTask = Task(
                noteId = task?.noteId ?: 0, // Retain ID if editing; 0 for new tasks
                noteTitle = name,
                noteAbstract = note,
                ddl = ddl,
                finished = isFinished,
                importance = importance,
                significance = task?.significance ?: 1, // Default significance
            )

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
