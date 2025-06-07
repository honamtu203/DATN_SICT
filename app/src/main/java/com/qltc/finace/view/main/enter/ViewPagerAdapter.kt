package com.qltc.finace.view.main.enter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.qltc.finace.view.main.enter.income.IncomeFragment
import com.qltc.finace.view.main.enter.expense.FragmentExpense

class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentExpense()
            else -> IncomeFragment()
        }
    }
} 