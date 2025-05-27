package com.qltc.finace.view.main.home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.qltc.finace.view.main.home.expense.FragmentExpense
import com.qltc.finace.view.main.home.income.IncomeFragment

class ViewPagerAdapter(fragment : Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return FragmentExpense()
            1 -> return  IncomeFragment()
            else -> return FragmentExpense()
        }
    }
}