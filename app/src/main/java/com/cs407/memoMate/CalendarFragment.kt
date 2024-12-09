package com.cs407.memoMate

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.CalendarView
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.cs407.memoMate.Data.NoteDatabase
import com.cs407.memoMate.Data.TaskDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CalendarFragment : Fragment(R.layout.fragment_calendar) {

    private var currentMonth: YearMonth = YearMonth.now()
    private lateinit var taskDao: TaskDao
    private lateinit var calendarView: CalendarView
    private lateinit var dateSelector: DateSelector
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

        // Initialize the dateSelector
        dateSelector = DateSelector(
            context = requireContext(),
            calendarView = calendarView,
            todayDrawable = R.drawable.bg_today,
            importanceColors = importanceColors,
            taskImportanceMap = taskImportanceMap
        ) { selectedDate ->
            Log.d("DateSelector", "Date clicked: $selectedDate")
            navigateToTaskList(selectedDate)
        }

        setupWeekTitle(titlesContainer)
        setupCalendar(calendarView, monthTitle)

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

        val backButton = view.findViewById<ImageView>(R.id.going_back_calendar)
        backButton.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        // Load tasks for the current month
        loadTasksForCurrentMonth()
    }

    private fun setupWeekTitle(titlesContainer: LinearLayout) {
        val daysOfWeek = daysOfWeek()
        titlesContainer.removeAllViews()
        daysOfWeek.forEach { dayOfWeek ->
            val textView = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())
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

        // Assign the dateSelector
        calendarView.dayBinder = dateSelector

        updateMonthTitle(monthTitle)
    }

    private fun updateMonthTitle(monthTitle: TextView) {
        monthTitle.text = currentMonth.format(dateFormatter)
    }

    private fun loadTasksForCurrentMonth() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val currentMonthStr = currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                val tasks = taskDao.getTasksForMonth(currentMonthStr)

                val groupedByDate = tasks.groupBy { task ->
                    try {
                        LocalDate.parse(task.ddl, DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US))
                    } catch (e: Exception) {
                        Log.e("CalendarFragment", "Invalid date format: ${task.ddl}")
                        null // Ignore invalid dates
                    }
                }.filterKeys { it != null } // Remove null (invalid dates)

                taskImportanceMap.clear()
                groupedByDate.forEach { (date, tasksOnDate) ->
                    val maxImportance = tasksOnDate.maxOfOrNull { it.importance } ?: 0
                    taskImportanceMap[date!!] = maxImportance
                }

                withContext(Dispatchers.Main) {
                    calendarView.notifyCalendarChanged() // Refresh calendar on the main thread
                }
            } catch (e: Exception) {
                Log.e("CalendarFragment", "Error loading tasks: ${e.message}")
            }
        }
    }


    private fun navigateToTaskList(date: LocalDate) {
        val bundle = Bundle().apply {
            putString("selected_date", date.toString())
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SelectedTaskListFragment().apply {
                arguments = bundle
            })
            .addToBackStack(null)
            .commit()
    }
}
