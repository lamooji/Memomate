package com.cs407.memoMate

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.yearMonth
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth

class DateSelector(
    private val context: Context,
    private val calendarView: CalendarView,
    private val todayDrawable: Int,
    private val importanceColors: Map<Int, Int>,
    private val taskImportanceMap: Map<LocalDate, Int>,
    private val onDateSelected: (LocalDate) -> Unit
) : MonthDayBinder<DateSelector.DayViewContainer> {

    private var currentMonth: YearMonth = YearMonth.now() // Tracks the current visible month

    override fun create(view: View): DayViewContainer {
        return DayViewContainer(view) { selectedDate ->
            onDateSelected(selectedDate)
        }
    }

    override fun bind(container: DayViewContainer, data: CalendarDay) {
        container.textView.text = data.date.dayOfMonth.toString()
        container.date = data.date

        val today = LocalDate.now()
        val importanceLevel = taskImportanceMap[data.date] ?: 0
        val importanceColor = importanceColors[importanceLevel] ?: R.color.default_day

        if (data.date.yearMonth != currentMonth) {
            container.textView.isEnabled = false
            container.textView.alpha = 0.3f
            container.textView.setBackgroundResource(0) // Clear background
            container.textView.setTextColor(ContextCompat.getColor(context, R.color.dark_gray))
        } else {
            container.textView.isEnabled = true
            container.textView.alpha = 1f
            container.textView.setTextColor(ContextCompat.getColor(context, R.color.black))

            // Highlight today's date or apply importance-based background
            if (data.date == today) {
                container.textView.setBackgroundResource(todayDrawable)
            } else {
                val importanceBackground = ContextCompat.getDrawable(context, R.drawable.bg_normal)
                importanceBackground?.setTint(ContextCompat.getColor(context, importanceColor))
                container.textView.background = importanceBackground
            }
        }
    }

    fun updateMonth(newMonth: YearMonth) {
        currentMonth = newMonth
        calendarView.notifyMonthChanged(newMonth)
    }

    class DayViewContainer(view: View, val onDateClick: (LocalDate) -> Unit) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
        var date: LocalDate? = null

        init {
            view.setOnClickListener {
                Log.d("DateSelector", "Clicked on date: $date")
                date?.let { onDateClick(it) }
            }
        }
    }
}
