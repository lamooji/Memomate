
package com.cs407.memoMate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cs407.memoMate.databinding.ActivityMainBinding
import com.cs407.memoMate.MainPageFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

       if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
               .replace(R.id.fragment_container, ScreenFragment())
               .commit()
        }

    }
}
