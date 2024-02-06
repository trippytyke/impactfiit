package com.uol.impactfiit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val homeFragment=HomeFragment()
        val settingsFragment=SettingsFragment()
        val logFragment=LogFragment()
        val progressFragment=ProgressFragment()
        val workoutFragment=WorkoutFragment()

        val fragShow = intent.getStringExtra("fragShow")
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        setCurrentFragment(homeFragment)
        bottomNavigationView.setSelectedItemId(R.id.home)

        when(fragShow){
            "settings" -> {
                setCurrentFragment(settingsFragment)
                bottomNavigationView.setSelectedItemId(R.id.settings)
            }
            "log" -> {
                setCurrentFragment(logFragment)
                bottomNavigationView.setSelectedItemId(R.id.log)
            }
            "progress" -> {
                setCurrentFragment(progressFragment)
                bottomNavigationView.setSelectedItemId(R.id.progress)
            }
            "home" -> {
                setCurrentFragment(homeFragment)
                bottomNavigationView.setSelectedItemId(R.id.home)
            }
            null -> {
                setCurrentFragment(homeFragment)
                bottomNavigationView.setSelectedItemId(R.id.home)
            }
            else -> {
                setCurrentFragment(homeFragment)
                bottomNavigationView.setSelectedItemId(R.id.home)
            }

        }

        bottomNavigationView.setOnNavigationItemSelectedListener{
            when(it.itemId){
                R.id.home -> setCurrentFragment(homeFragment)
                R.id.settings -> setCurrentFragment(settingsFragment)
                R.id.log -> setCurrentFragment(logFragment)
                R.id.progress -> setCurrentFragment(progressFragment)
                R.id.workout -> setCurrentFragment(workoutFragment)
            }
            true
        }
    }

    private fun setCurrentFragment(fragment:Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment,fragment)
            commit()
        }
}