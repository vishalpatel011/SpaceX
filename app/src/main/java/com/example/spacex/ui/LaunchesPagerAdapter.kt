package com.example.spacex.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class LaunchesPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UpcomingLaunchesFragment()
            1 -> PastLaunchesFragment()
            else -> throw IllegalStateException("Invalid position")
        }
    }
}