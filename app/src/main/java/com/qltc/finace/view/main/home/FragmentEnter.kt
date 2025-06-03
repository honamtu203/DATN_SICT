package com.qltc.finace.view.main.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.databinding.FragmentEnterBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentEnter : BaseFragment<FragmentEnterBinding, BaseEnterViewModel>(), EnterListener {
    override val layoutID: Int = R.layout.fragment_enter
    override val viewModel: ShareEnterViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.apply {
            listener = this@FragmentEnter
            viewModel = this@FragmentEnter.viewModel
        }
        viewBinding.vpgHome.adapter = ViewPagerAdapter(this)

        TabLayoutMediator(viewBinding.tabLayout,viewBinding.vpgHome) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.expense)
                1 -> tab.text = getString(R.string.income)
            }
        }.attach()
        onViewPagerChange()
    }
    private fun onViewPagerChange() {
        viewBinding.vpgHome.registerOnPageChangeCallback(object : OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == FRAGMENT_EXPENSE) {
                    viewModel.typeCurrentFragment = FRAGMENT_EXPENSE
                }
                else {
                    viewModel.typeCurrentFragment = FRAGMENT_INCOME
                }
            }
        })
    }
    companion object {
        const val FRAGMENT_EXPENSE = 0
        const val FRAGMENT_INCOME = 1
    }
    override fun onClickInputData() {
        if (viewModel.typeCurrentFragment == FRAGMENT_EXPENSE) {
            viewModel.submitExpense()
        }
        else {
            viewModel.submitIncome()
        }
    }
} 