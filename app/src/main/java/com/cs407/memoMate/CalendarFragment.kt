package com.cs407.memoMate

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import com.cs407.memoMate.Data.NoteDatabase
import com.cs407.memoMate.Data.Task
import com.cs407.memoMate.Data.TaskDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class CalendarFragment : Fragment(R.layout.fragment_calendar) {

    private var selectedDate: LocalDate? = null
    private var currentMonth: YearMonth = YearMonth.now()
    private lateinit var taskDao: TaskDao
    private lateinit var calendarView: CalendarView
    private val dateFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

    private val importanceColors = mapOf(
        3 to R.color.high_importance,
        2 to R.color.medium_importance,
        1 to R.color.low_importance
    )

    private val taskImportanceMap = mutableMapOf<LocalDate, Int>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        calendarView = view.findViewById(R.id.calendarView)
        val titlesContainer: LinearLayout = view.findViewById(R.id.titlesContainer)
        val monthTitle: TextView = view.findViewById(R.id.monthTitle)
        val btnNextMonth: ImageView = view.findViewById(R.id.btnNextMonth)
        val btnPrevMonth: ImageView = view.findViewById(R.id.btnPrevMonth)

        val db = NoteDatabase.getDatabase(requireContext())
        taskDao = db.taskDao()

        Log.d("calendar", "Database initialized")

        // Populate and load tasks
        CoroutineScope(Dispatchers.IO).launch {
            populateDatabaseWithDummyData(taskDao).join()
            loadTasksForCurrentMonth()
            withContext(Dispatchers.Main) {
                setupWeekTitle(titlesContainer)
                setupCalendar(calendarView, monthTitle)
                calendarView.notifyCalendarChanged()
            }
        }

        btnNextMonth.setOnClickListener {
            currentMonth = currentMonth.plusMonths(1)
            updateMonthTitle(monthTitle)
            calendarView.scrollToMonth(currentMonth)
            loadTasksForCurrentMonth()
        }

        btnPrevMonth.setOnClickListener {
            currentMonth = currentMonth.minusMonths(1)
            updateMonthTitle(monthTitle)
            calendarView.scrollToMonth(currentMonth)
            loadTasksForCurrentMonth()
        }
    }

    private fun setupWeekTitle(titlesContainer: LinearLayout) {
        val daysOfWeek = daysOfWeek()
        titlesContainer.removeAllViews()
        daysOfWeek.forEach { dayOfWeek ->
            val textView = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                gravity = android.view.Gravity.CENTER
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
            titlesContainer.addView(textView)
        }
    }

    private fun setupCalendar(calendarView: CalendarView, monthTitle: TextView) {
        val startMonth = currentMonth.minusMonths(10)
        val endMonth = currentMonth.plusMonths(10)

        calendarView.setup(startMonth, endMonth, daysOfWeek().first())
        calendarView.scrollToMonth(currentMonth)

        val dateSelector = DateSelector(
            context = requireContext(),
            calendarView = calendarView,
            todayDrawable = R.drawable.bg_today,
            importanceColors = importanceColors,
            taskImportanceMap = taskImportanceMap
        )

        calendarView.dayBinder = dateSelector

        updateMonthTitle(monthTitle)
    }

    private fun updateMonthTitle(monthTitle: TextView) {
        monthTitle.text = currentMonth.format(dateFormatter)
    }

    private fun loadTasksForCurrentMonth() {
        CoroutineScope(Dispatchers.IO).launch {
            // Fetch tasks for the current month
            val currentMonthStr = currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            val tasks = taskDao.getTasksForMonth(currentMonthStr)

            // Group tasks by date and calculate maximum importance for each date
            val groupedByDate = tasks.groupBy { task ->
                task.ddl.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
            }

            taskImportanceMap.clear()
            groupedByDate.forEach { (date, tasksOnDate) ->
                val maxImportance = tasksOnDate.maxOfOrNull { it.significance } ?: 0
                taskImportanceMap[date] = maxImportance
            }

            // Notify calendar on the main thread
            withContext(Dispatchers.Main) {
                calendarView.notifyCalendarChanged()
            }
        }
    }


    private fun populateDatabaseWithDummyData(taskDao: TaskDao): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            val dummyTasks = listOf(
                Task(
                    noteId = 0, // Auto-generate ID
                    noteTitle = "High Priority Task",
                    noteAbstract = "This is a high priority task description.",
                    ddl = Date(System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000)), // 2 days from now
                    significance = 3,
                    finished = false
                ),
                Task(
                    noteId = 0,
                    noteTitle = "Medium Priority Task",
                    noteAbstract = "This is a medium priority task description.",
                    ddl = Date(System.currentTimeMillis() + (5 * 24 * 60 * 60 * 1000)), // 5 days from now
                    significance = 2,
                    finished = false
                ),
                Task(
                    noteId = 0,
                    noteTitle = "Low Priority Task",
                    noteAbstract = "This is a low priority task description.",
                    ddl = Date(System.currentTimeMillis() + (10 * 24 * 60 * 60 * 1000)), // 10 days from now
                    significance = 1,
                    finished = false
                ),
                Task(
                    noteId = 0,
                    noteTitle = "Completed Task",
                    noteAbstract = "This task is already finished.",
                    ddl = Date(System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000)), // 1 day ago
                    significance = 3,
                    finished = false
                ),
                Task(
                    noteId = 0,
                    noteTitle = "high Task",
                    noteAbstract = "This task is already finished.",
                    ddl = Date(System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000)), // 30 day ago
                    significance = 3,
                    finished = false
                ),
                Task(
                    noteId = 0,
                    noteTitle = "high Task",
                    noteAbstract = "This task is already finished.",
                    ddl = Date(System.currentTimeMillis() + (27 * 24 * 60 * 60 * 1000)), // 30 day ago
                    significance = 3,
                    finished = false
                )

            )

            dummyTasks.forEach { task ->
                taskDao.insertTask(task)
            }

            Log.d("calendar", "Inserted ${dummyTasks.size} dummy tasks into the database.")
        }
    }
}
