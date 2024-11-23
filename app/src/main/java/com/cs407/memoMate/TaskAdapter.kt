// TaskAdapter.kt
package com.cs407.memoMate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs407.memoMate.R

class TaskAdapter(private val tasks: MutableList<TaskItem>) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    // ViewHolder class for task items
    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskCheckBox: CheckBox = itemView.findViewById(R.id.taskCheckBox)
        val taskTextView: TextView = itemView.findViewById(R.id.taskTextView)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        // Inflate the item_task layout
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val taskItem = tasks[position]
        holder.taskTextView.text = taskItem.task
        holder.taskCheckBox.isChecked = taskItem.isChecked

        // Handle checkbox state changes
        holder.taskCheckBox.setOnCheckedChangeListener { _, isChecked ->
            taskItem.isChecked = isChecked
        }
    }

    // Return the size of your dataset
    override fun getItemCount(): Int = tasks.size

    // Function to add a new task
    fun addTask(taskItem: TaskItem) {
        tasks.add(taskItem)
        notifyItemInserted(tasks.size - 1)
    }
}