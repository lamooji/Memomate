package com.cs407.memoMate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class ScreenFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.main_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val iconImageView: ImageView = view.findViewById(R.id.icon_image)

        // Get the current day of the month
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        // Dynamically get the resource ID for the image based on the day
        val resourceName = "icon_$currentDay"
        val resourceId = resources.getIdentifier(resourceName, "drawable", requireContext().packageName)

        if (resourceId != 0) {
            // If the resource exists, set it as the image
            iconImageView.setImageResource(resourceId)
        }

        // Calendar Button Navigation
        view.findViewById<LinearLayout>(R.id.calendar_button).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CalendarFragment())
                .addToBackStack(null)
                .commit()
        }

        // Matrix Button Navigation
        view.findViewById<LinearLayout>(R.id.matrix_button).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UrgentImportantMatrixFragment())
                .addToBackStack(null)
                .commit()
        }

        // Today's Task Button Navigation
        view.findViewById<LinearLayout>(R.id.todays_task_button).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MainPageFragment())
                .addToBackStack(null)
                .commit()
        }
    }

}
