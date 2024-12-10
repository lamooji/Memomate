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


/**
 * Fragment that displays a calendar view with task indicators
 * Tasks are color-coded based on their importance level, and users can
 * navigate between months and select dates to view associated tasks
 */
class CalendarFragment : Fragment(R.layout.fragment_calendar) {

    // Current month being displayed in the calendar
    private var currentMonth: YearMonth = YearMonth.now()

    // Database access objects
    private lateinit var taskDao: TaskDao
    private lateinit var calendarView: CalendarView
    private lateinit var dateSelector: DateSelector

    // Formatter for month/year display
    private val dateFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

    // Map of importance levels to their corresponding colors
    private val importanceColors = mapOf(
        1 to R.color.high_importance,    // High priority (Red)
        2 to R.color.medium_importance,  // Medium priority (Yellow)
        3 to R.color.low_importance      // Low priority (Green)
    )

    // Maps dates to their highest task importance level
    private val taskImportanceMap = mutableMapOf<LocalDate, Int>()

    /**
     * Initializes the calendar view and sets up UI components
     * Called after the fragment's layout has been created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Initialize UI components
        calendarView = view.findViewById(R.id.calendarView)
        val titlesContainer: LinearLayout = view.findViewById(R.id.titlesContainer)
        val monthTitle: TextView = view.findViewById(R.id.monthTitle)
        val btnNextMonth: ImageView = view.findViewById(R.id.btnNextMonth)
        val btnPrevMonth: ImageView = view.findViewById(R.id.btnPrevMonth)

        // Initialize database access
        val db = NoteDatabase.getDatabase(requireContext())
        taskDao = db.taskDao()

        Log.d("calendar", "Database initialized")

        // Set up date selection handling
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

        // Initialize calendar components
        setupWeekTitle(titlesContainer)
        setupCalendar(calendarView, monthTitle)

        // Set up month navigation
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

        // Set up back navigation
        val backButton = view.findViewById<ImageView>(R.id.going_back_calendar)
        backButton.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        // Initial task loading
        loadTasksForCurrentMonth()
    }

    /**
     * Sets up the weekday titles at the top of the calendar
     * @param titlesContainer Container view for weekday titles
     */
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

    /**
     * Configures the calendar view with date range and initial display
     * @param calendarView The calendar view to configure
     * @param monthTitle TextView displaying current month/year
     */
    private fun setupCalendar(calendarView: CalendarView, monthTitle: TextView) {
        // Set up calendar range (10 months before and after current month)
        val startMonth = currentMonth.minusMonths(10)
        val endMonth = currentMonth.plusMonths(10)

        calendarView.setup(startMonth, endMonth, daysOfWeek().first())
        calendarView.scrollToMonth(currentMonth)

        // Assign date selection handler
        calendarView.dayBinder = dateSelector

        updateMonthTitle(monthTitle)
    }

    /**
     * Updates the month/year display in the calendar header
     * @param monthTitle TextView to update
     */
    private fun updateMonthTitle(monthTitle: TextView) {
        monthTitle.text = currentMonth.format(dateFormatter)
    }

    /**
     * Loads tasks for the current month from the database
     * Updates the task importance map and refreshes the calendar display
     */
    private fun loadTasksForCurrentMonth() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Get tasks for current month
                val currentMonthStr = currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                val tasks = taskDao.getTasksForMonth(currentMonthStr)
                Log.d("CalendarFragment", "number of task being loaded ${tasks.size}")

                // Group tasks by date and filter out invalid dates
                val groupedByDate = tasks.groupBy { task ->
                    try {
                        LocalDate.parse(task.ddl, DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US))
                    } catch (e: Exception) {
                        Log.e("CalendarFragment", "Invalid date format: ${task.ddl}")
                        null
                    }
                }.filterKeys { it != null }

                // Update importance map with highest importance per date
                taskImportanceMap.clear()
                groupedByDate.forEach { (date, tasksOnDate) ->
                    val maxImportance = tasksOnDate.maxOfOrNull { it.importance } ?: 0
                    taskImportanceMap[date!!] = maxImportance
                }

                // Refresh calendar on main thread
                withContext(Dispatchers.Main) {
                    calendarView.notifyCalendarChanged()
                }
            } catch (e: Exception) {
                Log.e("CalendarFragment", "Error loading tasks: ${e.message}")
            }
        }
    }

    /**
     * Navigates to the task list fragment for a selected date
     * @param date The selected date to show tasks for
     */
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