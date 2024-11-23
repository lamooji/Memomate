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


class CalendarFragment : Fragment(R.layout.fragment_calendar) {

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

        // bind dates
//        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
//            override fun create(view: View) = DayViewContainer(view)
//
//            override fun bind(container: DayViewContainer, data: CalendarDay) {
//                container.textView.text = data.date.dayOfMonth.toString()
//
//                // highlight today
//                val today = LocalDate.now()
//                container.textView.setBackgroundResource(
//                    if (data.date == today) R.drawable.bg_today else R.drawable.bg_normal
//                )
//
//                // disable future datas
//                container.textView.isEnabled = !data.date.isAfter(today)
//            }
//        }

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.textView.text = data.date.dayOfMonth.toString()

                // get current month
                val currentMonth = YearMonth.now()

                // set backgrand color for today
                val today = LocalDate.now()
                container.textView.setBackgroundResource(
                    if (data.date == today) R.drawable.bg_today else R.drawable.bg_normal
                )

                // set other month to gray
                if (data.date.month != currentMonth.month) {
                    container.textView.isEnabled = false
                    container.textView.alpha = 0.3f
                } else {
                    container.textView.isEnabled = true
                    container.textView.alpha = 1f
                }

            }
        }

    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView = view.findViewById<TextView>(R.id.calendarDayText)

        // With ViewBinding
        //val textView = CalendarDayLayoutBinding.bind(view).calendarDayText

    }
}
