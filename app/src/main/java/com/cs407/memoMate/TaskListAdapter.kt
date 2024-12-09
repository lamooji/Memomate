package com.cs407.memoMate

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs407.memoMate.Data.Task

class TaskListAdapter(
    private val taskItems: MutableList<Any>,
    private val onDeleteTask: (Task, Int) -> Unit, // Pass position for accurate updates
    private val onEditTask: (Task) -> Unit         // Callback for edit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_TASK = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (taskItems[position] is String) {
            TYPE_HEADER
        } else {
            TYPE_TASK
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.task_list_section_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.task_card, parent, false)
            TaskViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = taskItems[position]) {
            is String -> {
                (holder as HeaderViewHolder).headerTitle.text = item
            }
            is Task -> {
                val task = item
                val taskHolder = holder as TaskViewHolder
                taskHolder.taskName.text = task.noteTitle

                // Set priority background based on significance
                when (task.significance) {
                    1 -> {
                        taskHolder.taskPriority.setBackgroundResource(R.drawable.priority_badge_high)
                        taskHolder.taskPriority.text = "High" // Set text for high priority
                    }
                    2 -> {
                        taskHolder.taskPriority.setBackgroundResource(R.drawable.priority_badge_medium)
                        taskHolder.taskPriority.text = "Medium" // Set text for medium priority
                    }
                    3 -> {
                        taskHolder.taskPriority.setBackgroundResource(R.drawable.priority_badge_low)
                        taskHolder.taskPriority.text = "Low" // Set text for low priority
                    }
                }


                // Set click listener for delete icon
                taskHolder.deleteIcon.setOnClickListener {
                    onDeleteTask(task, position) // Pass task and position to fragment
                }

                // Set click listener for edit icon
                taskHolder.editIcon.setOnClickListener {
                    onEditTask(task) // Notify fragment to edit the task
                }
            }
        }
    }

    override fun getItemCount(): Int = taskItems.size

    fun updateTaskItems(newItems: List<Any>) {
        Log.d("update","updateTaks")
        taskItems.clear()
        taskItems.addAll(newItems)
        notifyDataSetChanged()
    }

    fun removeTask(position: Int) {
        taskItems.removeAt(position)
        notifyItemRemoved(position)

        // Remove section header if no tasks remain under it
        if (position > 0 && taskItems[position - 1] is String &&
            (position >= taskItems.size || taskItems[position] is String)) {
            taskItems.removeAt(position - 1)
            notifyItemRemoved(position - 1)
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerTitle: TextView = itemView.findViewById(R.id.section_title)
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskName: TextView = itemView.findViewById(R.id.task_name)
        val taskPriority: TextView = itemView.findViewById(R.id.task_priority)
        val deleteIcon: ImageView = itemView.findViewById(R.id.delete_icon) // Delete icon
        val editIcon: ImageView = itemView.findViewById(R.id.edit_icon)     // Edit icon
    }
}
