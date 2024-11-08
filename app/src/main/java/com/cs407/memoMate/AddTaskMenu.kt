package com.cs407.memoMate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.cs407.memoMate.databinding.AddTaskMenuBinding  // Note the updated binding name

class AddTaskMenu : DialogFragment() {
    private var _binding: AddTaskMenuBinding? = null  // Updated binding type
    private val binding get() = _binding!!

    interface TaskDialogListener {
        fun onTaskAdded(name: String, ddl: String, isFinished: Boolean, note: String)
    }

    private var listener: TaskDialogListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddTaskMenuBinding.inflate(inflater, container, false)  // Updated binding name
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Make dialog width match parent with margins
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        binding.addButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val ddl = binding.ddlEditText.text.toString()
            val isFinished = binding.finishedCheckbox.isChecked
            val note = binding.noteEditText.text.toString()

            listener?.onTaskAdded(name, ddl, isFinished, note)
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