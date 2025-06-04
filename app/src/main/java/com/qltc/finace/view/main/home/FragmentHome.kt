package com.qltc.finace.view.main.home

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import com.google.firebase.auth.FirebaseAuth
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.tabs.TabLayout
import com.qltc.finace.view.adapter.AdapterTotalCategory
import com.qltc.finace.data.entity.CategoryOverView
import com.qltc.finace.view.adapter.AdapterExpenseIncomeReport
import com.qltc.finace.view.main.calendar.FinancialRecord
import com.qltc.finace.data.Resource
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class FragmentHome : BaseFragment<FragmentHomeBinding, HomeViewModel>(), 
    HomeListener, 
    AdapterTotalCategory.OnClickListener {
    
    override val layoutID: Int = R.layout.fragment_home
    override val viewModel: HomeViewModel by viewModels()
    private val categoryAdapter by lazy { AdapterTotalCategory(this) }
    private val recentTransactionsAdapter by lazy { 
        AdapterExpenseIncomeReport(object : AdapterExpenseIncomeReport.OnClickListener {
            override fun onClickItemEI(item: FinancialRecord) {
                safeNavigate(
                    R.id.frg_edit_i_e,
                    Bundle().apply {
                        putString("transaction_id", item.id)
                    }
                )
            }
        })
    }

    private val numberFormat by lazy {
        NumberFormat.getNumberInstance(Locale("vi", "VN")).apply {
            maximumFractionDigits = 0
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            setupViews()
            setupObservers()
            setupClickListeners()
            viewModel.loadInitialData()
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Đã xảy ra lỗi khi khởi tạo màn hình")
        }
    }

    private fun setupViews() {
        viewBinding.apply {
            lifecycleOwner = viewLifecycleOwner
            this.viewModel = this@FragmentHome.viewModel
            listener = this@FragmentHome

            // Setup RecyclerViews
            rvTopCategories.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = categoryAdapter
            }

            rvRecentTransactions.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = recentTransactionsAdapter
            }

            // Setup Chart
            barChart.apply {
                description.isEnabled = false
                legend.isEnabled = false
                setDrawGridBackground(false)
                setDrawBorders(false)
                
                xAxis.apply {
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    setDrawLabels(false)
                }
                
                axisLeft.apply {
                    setDrawGridLines(true)
                    setDrawAxisLine(false)
                    textColor = Color.GRAY
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return numberFormat.format(value.toLong())
                        }
                    }
                }
                
                axisRight.isEnabled = false
                setTouchEnabled(false)
                animateY(1000)
            }

            // Setup TabLayout
            tabOverview.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.let { viewModel?.onTabSelected(it.position) }
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    private fun setupClickListeners() {
        viewBinding.apply {
            // Setup error retry button
            btnRetry.setOnClickListener {
                viewModel?.retryLoading()
            }

            // Setup income/expense cards
            layoutIncome.setOnClickListener {
                listener?.onIncomeCardClick()
            }

            layoutExpense.setOnClickListener {
                listener?.onExpenseCardClick()
            }

            // Setup view all buttons
            btnViewDetails.setOnClickListener {
                safeNavigate(R.id.frg_category_detail)
            }

            btnViewAllTransactions.setOnClickListener {
                listener?.onViewAllTransactionsClick()
            }
        }
    }

    private fun setupObservers() {
        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            handleDataState(state)
        }

        viewModel.chartData.observe(viewLifecycleOwner) { monthlyDataList ->
            monthlyDataList?.let {
                if (it.isEmpty()) {
                    showEmptyChart()
                } else {
                    updateChart(it)
                    updateMonthLabels(it)
                }
            }
        }

        viewModel.topCategories.observe(viewLifecycleOwner) { categories ->
            categories?.let {
                if (it.isEmpty()) {
                    showEmptyCategories()
                } else {
                    showCategories(it)
                }
            }
        }

        viewModel.recentTransactions.observe(viewLifecycleOwner) { transactions ->
            transactions?.let {
                if (it.isEmpty()) {
                    showEmptyTransactions()
                } else {
                    showTransactions(it)
                }
            }
        }

        // Observe balance visibility
        viewModel.isBalanceVisible.observe(viewLifecycleOwner) { isVisible ->
            updateBalanceVisibility(isVisible)
        }

        // Observe budget progress
        viewModel.budgetProgress.observe(viewLifecycleOwner) { progress ->
            viewBinding.progressBudget.progress = progress
        }
    }

    private fun updateBalanceVisibility(isVisible: Boolean) {
        viewBinding.apply {
            if (isVisible) {
                tvBalance.text = formatCurrency(viewModel?.currentBalance?.value ?: 0)
                btnToggleBalance.setImageResource(R.drawable.ic_visibility)
            } else {
                tvBalance.text = "****"
                btnToggleBalance.setImageResource(R.drawable.ic_visibility_off)
            }
        }
    }

    private fun handleDataState(state: Resource<Unit>) {
        viewBinding.apply {
            when (state) {
                is Resource.Loading -> {
                    progressBar.isVisible = true
                    contentLayout.isVisible = false
                    errorLayout.isVisible = false
                }
                is Resource.Success -> {
                    progressBar.isVisible = false
                    contentLayout.isVisible = true
                    errorLayout.isVisible = false
                }
                is Resource.Error -> {
                    progressBar.isVisible = false
                    contentLayout.isVisible = false
                    errorLayout.isVisible = true
                    tvError.text = state.message
                }
            }
        }
    }

    private fun showEmptyChart() {
        viewBinding.apply {
            barChart.setNoDataText("Không có dữ liệu")
            barChart.setNoDataTextColor(Color.GRAY)
            barChart.invalidate()
        }
    }

    private fun showEmptyCategories() {
        viewBinding.apply {
            rvTopCategories.isVisible = false
            tvEmptyCategories.isVisible = true
            tvEmptyCategories.text = "Chưa có danh mục nào"
        }
    }

    private fun showCategories(categories: List<CategoryOverView>) {
        viewBinding.apply {
            rvTopCategories.isVisible = true
            tvEmptyCategories.isVisible = false
            categoryAdapter.submitList(categories)
        }
    }

    private fun showEmptyTransactions() {
        viewBinding.apply {
            rvRecentTransactions.isVisible = false
            tvEmptyTransactions.isVisible = true
            tvEmptyTransactions.text = "Chưa có giao dịch nào"
        }
    }

    private fun showTransactions(transactions: List<FinancialRecord>) {
        viewBinding.apply {
            rvRecentTransactions.isVisible = true
            tvEmptyTransactions.isVisible = false
            recentTransactionsAdapter.submitList(transactions)
        }
    }

    private fun updateChart(monthlyDataList: List<MonthlyData>) {
        try {
            val entries = monthlyDataList.mapIndexed { index, data ->
                when (viewModel?.selectedTab?.value) {
                    HomeViewModel.TAB_INCOME -> BarEntry(index.toFloat(), data.income.toFloat())
                    else -> BarEntry(index.toFloat(), data.expense.toFloat())
                }
            }

            val dataSet = BarDataSet(entries, "").apply {
                color = when (viewModel?.selectedTab?.value) {
                    HomeViewModel.TAB_INCOME -> resources.getColor(R.color.green_2D9849, null)
                    else -> resources.getColor(R.color.red, null)
                }
                setDrawValues(false)
            }

            viewBinding.barChart.apply {
                data = BarData(dataSet)
                invalidate()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Lỗi khi cập nhật biểu đồ")
        }
    }

    private fun updateMonthLabels(monthlyDataList: List<MonthlyData>) {
        try {
            val monthNames = listOf("T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12")
            
            viewBinding.apply {
                if (monthlyDataList.size >= 6) {
                    tvMonth1.text = monthNames[monthlyDataList[0].month - 1]
                    tvMonth2.text = monthNames[monthlyDataList[1].month - 1]
                    tvMonth3.text = monthNames[monthlyDataList[2].month - 1]
                    tvMonth4.text = monthNames[monthlyDataList[3].month - 1]
                    tvMonth5.text = monthNames[monthlyDataList[4].month - 1]
                    tvMonth6.text = monthNames[monthlyDataList[5].month - 1]
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Lỗi khi cập nhật nhãn tháng")
        }
    }

    private fun formatCurrency(amount: Long): String {
        return "${numberFormat.format(amount)} đ"
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun safeNavigate(destinationId: Int, args: Bundle? = null) {
        try {
            findNavController().navigate(destinationId, args)
        } catch (e: Exception) {
            e.printStackTrace()
            showError("Lỗi khi chuyển màn hình")
        }
    }

    // HomeListener implementations
    override fun onNotificationClick() {
        // Handle notification click
    }

    override fun onToggleBalanceClick() {
        viewModel?.toggleBalanceVisibility()
    }

    override fun onTabSelected(position: Int) {
        viewModel?.onTabSelected(position)
    }

    override fun onViewAllTransactionsClick() {
        safeNavigate(R.id.frg_all_income_expense)
    }

    override fun onClickItemEI(item: CategoryOverView) {
        safeNavigate(
            R.id.frg_category_detail,
            Bundle().apply {
                putString("category_id", item.category.idCategory)
            }
        )
    }

    override fun onIncomeCardClick() {
        safeNavigate(
            R.id.frag_enter,
            Bundle().apply {
                putInt("selected_tab", 0) // 0 for income tab
            }
        )
    }

    override fun onExpenseCardClick() {
        safeNavigate(
            R.id.frag_enter,
            Bundle().apply {
                putInt("selected_tab", 1) // 1 for expense tab
            }
        )
    }
} 