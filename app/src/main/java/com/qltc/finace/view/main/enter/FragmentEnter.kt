package com.qltc.finace.view.main.enter

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
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
        setupViewPager()
        onViewPagerChange()
    }

    private fun setupViewPager() {
        viewBinding.vpgHome.adapter = ViewPagerAdapter(this)

        TabLayoutMediator(viewBinding.tabLayout, viewBinding.vpgHome) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.expense)
                1 -> tab.text = getString(R.string.income)
            }
        }.attach()

        // Xử lý selected_tab argument
        arguments?.getInt("selected_tab", -1)?.let { selectedTab ->
            if (selectedTab != -1) {
                viewBinding.vpgHome.post {
                    viewBinding.vpgHome.setCurrentItem(selectedTab, false)
                }
            }
        }
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
    
    override fun onClickInputData() {
        if (viewModel.typeCurrentFragment == FRAGMENT_EXPENSE) {
            viewModel.submitExpense()
        }
        else {
            viewModel.submitIncome()
        }
    }

    override fun onBackPressed() {
        try {
            findNavController().popBackStack()
        } catch (e: Exception) {
            // Fallback nếu popBackStack() gặp lỗi
            try {
                findNavController().popBackStack(R.id.frag_home, false)
            } catch (e2: Exception) {
                try {
                    // Phương pháp thay thế cuối cùng
                    findNavController().navigate(R.id.frag_home)
                } catch (e3: Exception) {
                    // Ghi log lỗi
                    e3.printStackTrace()
                }
            }
        }
    }

    companion object {
        const val FRAGMENT_EXPENSE = 1
        const val FRAGMENT_INCOME = 0
    }
} 