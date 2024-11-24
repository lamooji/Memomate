package com.cs407.memoMate

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.cs407.memoMate.databinding.CalendarDayLayoutBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import android.view.Gravity
import android.widget.Toast


class CalendarFragment : Fragment(R.layout.fragment_calendar) {

    private var selectedDate: LocalDate? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        val titlesContainer = view.findViewById<LinearLayout>(R.id.titlesContainer)

        // set title
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        val daysOfWeek = daysOfWeek(firstDayOfWeek = firstDayOfWeek)

        titlesContainer.removeAllViews() // remove views
        daysOfWeek.forEach { dayOfWeek ->
            val textView = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                gravity = Gravity.CENTER
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
            titlesContainer.addView(textView)
        }

        // set calendar range
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(10)
        val endMonth = currentMonth.plusMonths(10)
        calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        calendarView.post {
            calendarView.scrollToMonth(currentMonth)
        }

        // Use DateSelector for dayBinder
        calendarView.dayBinder = DateSelector(
            context = requireContext(),
            calendarView = calendarView,
            todayDrawable = R.drawable.bg_today,
            normalDrawable = R.drawable.bg_normal,
            highlightedDrawable = R.drawable.bg_highlighted
        )
        }
}
