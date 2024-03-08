package com.uol.impactfiit

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ListViewModel : ViewModel() { //ViewModel to pass data from the AddRoutineFragment to the ViewRoutineFragment
    val viewRoutineList = ArrayList<ViewRoutineFragment.ViewRoutine>()
    fun clearData() {
        viewRoutineList.clear()
    }
}
class ViewRoutineActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewroutine)

        //Get the viewpager and tablayout views
        val viewPager = findViewById<ViewPager2>(R.id.pager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val topAppBar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar)

        //Set the adapter for the viewpager
        viewPager.adapter = PagerAdapter(this)

        //Attach the tablayout to the viewpager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            if (position == 0) {
                tab.text = "Routine"
            } else {
                tab.text = "Add Exercise"
            }
        }.attach()

        //Set the top app bar navigation icon to go back to the previous activity
        topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }
}


class PagerAdapter(fragmentActivity: FragmentActivity) : //Adapter for the viewpager
    FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        when (position){
            0 -> {
                return ViewRoutineFragment.newInstance(position)
            }
            1->{
                return AddRoutineFragment.newInstance(position)
            }

        }
        return ViewRoutineFragment.newInstance(position)
    }

    override fun getItemCount(): Int {
        return ITEM_COUNT
    }

    companion object {
        private const val ITEM_COUNT = 2
    }
}


