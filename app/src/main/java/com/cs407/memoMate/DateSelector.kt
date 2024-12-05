package com.cs407.memoMate

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth

class DateSelector(
    private val context: Context,
    private val calendarView: CalendarView,
    private val todayDrawable: Int,
    private val normalDrawable: Int,
    private val highlightedDrawable: Int
) : MonthDayBinder<DateSelector.DayViewContainer> {

    private var selectedDate: LocalDate? = null // Tracks the selected date
    private var currentMonth: YearMonth = YearMonth.now() // Tracks the current visible month

    override fun create(view: View) = DayViewContainer(view)

    override fun bind(container: DayViewContainer, data: CalendarDay) {
        container.textView.text = data.date.dayOfMonth.toString()

        val today = LocalDate.now()

        // Highlight selected date or today
        when {
            data.date == selectedDate -> {
                container.textView.setBackgroundResource(highlightedDrawable)
                container.textView.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            data.date == today -> {
                container.textView.setBackgroundResource(todayDrawable)
                container.textView.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            else -> {
                container.textView.setBackgroundResource(normalDrawable)
                container.textView.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
        }

        // Disable non-current month dates
        if (data.date.month != currentMonth.month) {
            container.textView.isEnabled = false
            container.textView.alpha = 0.3f
        } else {
            container.textView.isEnabled = true
            container.textView.alpha = 1f
        }

        // Handle click events
        container.textView.setOnClickListener {
            if (data.date.month == currentMonth.month) {
                val previousSelectedDate = selectedDate
                selectedDate = data.date

                // Refresh previously selected and newly selected dates
                previousSelectedDate?.let { calendarView.notifyDateChanged(it) }
                selectedDate?.let { calendarView.notifyDateChanged(it) }
            }
        }
    }

    /**
     * Update the current month and refresh the calendar view.
     */
    fun updateMonth(newMonth: YearMonth) {
        currentMonth = newMonth
        calendarView.notifyMonthChanged(newMonth)
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
    }
}
