package com.cs407.memoMate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import kotlin.random.Random

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

        // Get ImageView from the inflated view
        val iconImageView: ImageView = view.findViewById(R.id.icon_image)

        // hardcode rn for testing ((((((
        val imageResources = listOf(
            R.drawable.icon_1,
            R.drawable.icon_2,
            R.drawable.icon_3,
        )

        // TODO: display current date
        val randomImageResId = imageResources.random()

        iconImageView.setImageResource(randomImageResId)

    }
}
