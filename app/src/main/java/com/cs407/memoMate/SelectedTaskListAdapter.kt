package com.cs407.memoMate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs407.memoMate.Data.Task

class SelectedTaskListAdapter(
    private val taskItems: MutableList<Task>
) : RecyclerView.Adapter<SelectedTaskListAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_card, parent, false) // Reuse your task card layout
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskItems[position]

        // Bind task data to UI elements
        holder.taskName.text = task.noteTitle

        // Set priority background based on significance
        when (task.significance) {
            1 -> holder.taskPriority.setBackgroundResource(R.drawable.priority_badge_low)
            2 -> holder.taskPriority.setBackgroundResource(R.drawable.priority_badge_medium)
            3 -> holder.taskPriority.setBackgroundResource(R.drawable.priority_badge_high)
        }

        // Remove Edit and Delete buttons (set visibility to GONE)
        holder.editIcon.visibility = View.GONE
        holder.deleteIcon.visibility = View.GONE
    }

    override fun getItemCount(): Int = taskItems.size

    fun updateTaskItems(newItems: List<Task>) {
        taskItems.clear()
        taskItems.addAll(newItems)
        notifyDataSetChanged()
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskName: TextView = itemView.findViewById(R.id.task_name)      // Task name
        val taskPriority: TextView = itemView.findViewById(R.id.task_priority) // Priority badge
        val editIcon: ImageView = itemView.findViewById(R.id.edit_icon)     // Edit icon
        val deleteIcon: ImageView = itemView.findViewById(R.id.delete_icon) // Delete icon
    }
}
