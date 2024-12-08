package com.cs407.memoMate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs407.memoMate.Data.Task


class TaskListAdapter(private val taskItems: MutableList<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
                (holder as TaskViewHolder).taskName.text = task.noteTitle

                // 根据优先级设置背景
                when (task.significance) {
                    1 -> holder.taskPriority.setBackgroundResource(R.drawable.priority_badge_high)
                    2 -> holder.taskPriority.setBackgroundResource(R.drawable.priority_badge_medium)
                    3 -> holder.taskPriority.setBackgroundResource(R.drawable.priority_badge_low)
                }
            }
        }
    }

    override fun getItemCount(): Int = taskItems.size

    fun updateTaskItems(newItems: List<Any>) {
        taskItems.clear()
        taskItems.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addTask(task: Task) {
        taskItems.add(task)
        notifyItemInserted(taskItems.size - 1)
    }


    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerTitle: TextView = itemView.findViewById(R.id.section_title)
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskName: TextView = itemView.findViewById(R.id.task_name)
        val taskPriority: TextView = itemView.findViewById(R.id.task_priority)
    }
}


