// MainPageFragment.kt
package com.cs407.memomate;

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainPageFragment : Fragment() {

    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<TaskItem>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_main_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.checklistRecyclerView)
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
        taskAdapter = TaskAdapter(tasks)
        recyclerView.adapter = taskAdapter

        // Initialize Buttons
        val addButton = view.findViewById<Button>(R.id.addButton)
        val editButton = view.findViewById<Button>(R.id.editButton)
        val calendarButton = view.findViewById<Button>(R.id.calendarButton)

        addButton.setOnClickListener {
            // Implement edit functionality here
            Toast.makeText(requireContext(), "Add button clicked", Toast.LENGTH_SHORT).show()
        }

        editButton.setOnClickListener {
            // Implement edit functionality here
            Toast.makeText(requireContext(), "Edit button clicked", Toast.LENGTH_SHORT).show()
        }

        calendarButton.setOnClickListener {
            // Implement calendar functionality here
            Toast.makeText(requireContext(), "Calendar button clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
