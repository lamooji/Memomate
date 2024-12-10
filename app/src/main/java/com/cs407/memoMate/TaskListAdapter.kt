package com.cs407.memoMate

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs407.memoMate.Data.Task

/**
 * RecyclerView adapter that displays tasks with section headers
 * Supports two types of items: headers (String) and tasks (Task)
 * Provides functionality for task deletion and editing
 *
 * @param taskItems Mutable list of items (can be either String for headers or Task for task items)
 * @param onDeleteTask Callback function for task deletion, includes position for accurate updates
 * @param onEditTask Callback function for task editing
 */
class TaskListAdapter(
    private val taskItems: MutableList<Any>,
    private val onDeleteTask: (Task, Int) -> Unit,
    private val onEditTask: (Task) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0  // ViewType for section headers
        private const val TYPE_TASK = 1    // ViewType for task items
    }

    /**
     * Determines the view type based on item type
     * @param position Position of the item in the list
     * @return TYPE_HEADER for String items (headers) or TYPE_TASK for Task items
     */
    override fun getItemViewType(position: Int): Int {
        return if (taskItems[position] is String) {
            TYPE_HEADER
        } else {
            TYPE_TASK
        }
    }

    /**
     * Creates appropriate ViewHolder based on view type
     * @param parent Parent ViewGroup
     * @param viewType Type of view (TYPE_HEADER or TYPE_TASK)
     * @return Appropriate ViewHolder instance
     */
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

    /**
     * Binds data to ViewHolder based on item type
     * For headers: Sets header text
     * For tasks: Sets task details, priority badge, and click listeners
     *
     * @param holder ViewHolder to bind data to
     * @param position Position of item in list
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = taskItems[position]) {
            is String -> {
                (holder as HeaderViewHolder).headerTitle.text = item
            }
            is Task -> {
                val task = item
                val taskHolder = holder as TaskViewHolder
                taskHolder.taskName.text = task.noteTitle

                // Set priority badge styling and text based on significance level
                when (task.significance) {
                    1 -> {
                        taskHolder.taskPriority.setBackgroundResource(R.drawable.priority_badge_high)
                        taskHolder.taskPriority.text = "High"
                    }
                    2 -> {
                        taskHolder.taskPriority.setBackgroundResource(R.drawable.priority_badge_medium)
                        taskHolder.taskPriority.text = "Medium"
                    }
                    3 -> {
                        taskHolder.taskPriority.setBackgroundResource(R.drawable.priority_badge_low)
                        taskHolder.taskPriority.text = "Low"
                    }
                }

                // Set up click listeners for task actions
                taskHolder.deleteIcon.setOnClickListener {
                    onDeleteTask(task, position)
                }

                taskHolder.editIcon.setOnClickListener {
                    onEditTask(task)
                }
            }
        }
    }

    override fun getItemCount(): Int = taskItems.size

    /**
     * Updates the entire list with new items
     * @param newItems New list of items (mix of headers and tasks)
     */
    fun updateTaskItems(newItems: List<Any>) {
        Log.d("update", "updateTasks")
        taskItems.clear()
        taskItems.addAll(newItems)
        notifyDataSetChanged()
    }

    /**
     * Removes a task and its header if it's the last task in its section
     * @param position Position of task to remove
     */
    fun removeTask(position: Int) {
        taskItems.removeAt(position)
        notifyItemRemoved(position)

        // Remove header if it's now empty (no tasks between this header and next header)
        if (position > 0 && taskItems[position - 1] is String &&
            (position >= taskItems.size || taskItems[position] is String)) {
            taskItems.removeAt(position - 1)
            notifyItemRemoved(position - 1)
        }
    }

    /**
     * ViewHolder for section headers
     */
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerTitle: TextView = itemView.findViewById(R.id.section_title)
    }

    /**
     * ViewHolder for task items
     * Contains views for task name, priority badge, and action icons
     */
    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskName: TextView = itemView.findViewById(R.id.task_name)
        val taskPriority: TextView = itemView.findViewById(R.id.task_priority)
        val deleteIcon: ImageView = itemView.findViewById(R.id.delete_icon)
        val editIcon: ImageView = itemView.findViewById(R.id.edit_icon)
    }
}