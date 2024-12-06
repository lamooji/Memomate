package com.cs407.memoMate

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
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
import androidx.navigation.fragment.findNavController


class CalendarFragment : Fragment(R.layout.fragment_calendar) {

    private var selectedDate: LocalDate? = null
    private var currentMonth: YearMonth = YearMonth.now()
    private val dateFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val calendarView: CalendarView = view.findViewById(R.id.calendarView)
        val titlesContainer: LinearLayout = view.findViewById(R.id.titlesContainer)
        val monthTitle: TextView = view.findViewById(R.id.monthTitle)
        val btnNextMonth: ImageView = view.findViewById(R.id.btnNextMonth)
        val btnPrevMonth: ImageView = view.findViewById(R.id.btnPrevMonth)

        setupWeekTitle(titlesContainer)

        setupCalendar(calendarView, monthTitle)

        btnNextMonth.setOnClickListener {
            currentMonth = currentMonth.plusMonths(1)
            updateMonthTitle(monthTitle)
            calendarView.scrollToMonth(currentMonth)
        }

        btnPrevMonth.setOnClickListener {
            currentMonth = currentMonth.minusMonths(1)
            updateMonthTitle(monthTitle)
            calendarView.scrollToMonth(currentMonth)
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

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View): DayViewContainer {
                return DayViewContainer(view)
            }

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.textView.text = data.date.dayOfMonth.toString()

                val today = LocalDate.now()

                when {
                    data.date == today -> {
                        container.textView.setBackgroundResource(R.drawable.bg_today)
                        container.textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    }
                    data.date.month != currentMonth.month -> {
                        container.textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_gray))
                        container.textView.alpha = 0.3f
                    }
                    else -> {
                        container.textView.setBackgroundResource(R.drawable.bg_normal)
                        container.textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                        container.textView.alpha = 1f
                    }
                }

//                container.textView.setOnClickListener {
//                    if (data.date.month == currentMonth.month) {
//                        val previousDate = selectedDate
//                        selectedDate = data.date
//                        previousDate?.let { calendarView.notifyDateChanged(it) }
//                        selectedDate?.let { calendarView.notifyDateChanged(it) }
//                    }
//                }

                container.textView.setOnClickListener {
                    selectedDate = data.date

                    // Navigate to MainPageFragment
                    val mainPageFragment = MainPageFragment()

                    // Use FragmentManager to replace the current fragment
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, mainPageFragment)
                        .addToBackStack(null) // Add to back stack for navigation history
                        .commit()
                }

            }
        }

        updateMonthTitle(monthTitle)
    }

    private fun updateMonthTitle(monthTitle: TextView) {
        monthTitle.text = currentMonth.format(dateFormatter)
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
    }
}
