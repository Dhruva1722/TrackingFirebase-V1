package com.example.afinal.UserActivity


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.afinal.UserActivity.Fragment.FirstStepFragment
import com.example.afinal.UserActivity.Fragment.SecondStepFragment
import com.example.afinal.UserActivity.Fragment.ThirdStepFragment

class RegistrationPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> FirstStepFragment()
            1 -> SecondStepFragment()
            2 -> ThirdStepFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }

    override fun getCount(): Int = 3 // Total number of registration steps
}
