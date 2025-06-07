package com.qltc.finace.view.main.home

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.tabs.TabLayout
import com.qltc.finace.view.adapter.AdapterTotalCategory
import com.qltc.finace.data.entity.CategoryOverView
import com.qltc.finace.view.adapter.AdapterExpenseIncomeReport
import com.qltc.finace.view.adapter.AdapterTopCategory
import com.qltc.finace.view.main.calendar.FinancialRecord
import com.qltc.finace.data.Resource
import java.text.NumberFormat
import java.time.YearMonth
import java.util.Locale

@AndroidEntryPoint
class FragmentHome : BaseFragment<FragmentHomeBinding, HomeViewModel>(),
    HomeListener,
    AdapterExpenseIncomeReport.OnClickListener,
    AdapterTopCategory.OnClickListener {
    
    override val layoutID: Int = R.layout.fragment_home
    override val viewModel: HomeViewModel by viewModels()
    private val topCategoryAdapter by lazy { AdapterTopCategory(this) }
    private val adapter by lazy { AdapterExpenseIncomeReport(this) }

    private val numberFormat by lazy {
        NumberFormat.getNumberInstance(Locale("vi", "VN")).apply {
            maximumFractionDigits = 0
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeData()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
        // Restore the visual state of the tab layout to match the ViewModel
        val selectedTab = viewBinding.tabOverview.getTabAt(viewModel.selectedTabIndex)
        if (selectedTab != null) {
            // We need to select the tab programmatically, but this might not trigger the listener
            selectedTab.select()
            // So we also explicitly call onTabSelected to ensure data is refreshed
            onTabSelected(viewModel.selectedTabIndex)
        }
    }

    private fun setupViews() {
        viewBinding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@FragmentHome.viewModel
            listener = this@FragmentHome
            rvTopCategories.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = topCategoryAdapter
            }

            rvRecentTransactions.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = this@FragmentHome.adapter
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
                    tab?.let { onTabSelected(it.position) }
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })

            // Setup empty states
            tvEmptyTransactions.text = getString(R.string.no_transactions)
            tvEmptyCategories.text = getString(R.string.no_categories)
        }

        // Setup Observer for data refresh
        viewModel.isDataRefreshed.observe(viewLifecycleOwner) { isRefreshed ->
            if (isRefreshed) {
                // Cập nhật biểu đồ khi dữ liệu được làm mới
                updateBarChart(viewModel.selectedTabIndex == HomeViewModel.TAB_EXPENSE)
            }
        }
    }

    private fun observeData() {
        with(viewModel) {
            // Observe visibility states
            isBalanceVisible.observe(viewLifecycleOwner) { isVisible: Boolean ->
                viewBinding.tvBalance.text = if (isVisible) viewBinding.tvBalance.text else "****"
            }

            // Observe user data
            username.observe(viewLifecycleOwner) { name: String ->
                viewBinding.tvUsername.text = getString(R.string.welcome_user, name)
            }

            // Observe budget data
            budgetProgress.observe(viewLifecycleOwner) { progress: Int ->
                viewBinding.progressBudget.progress = progress
            }

            // Observe top categories
            topCategories.observe(viewLifecycleOwner) { categories ->
                topCategoryAdapter.submitList(categories)
                viewBinding.tvEmptyCategories.isVisible = categories.isEmpty()
            }

            remainingBudget.observe(viewLifecycleOwner) { remaining: Long ->
                viewBinding.tvRemainingAmount.text = remaining.toString()
            }

            remainingDays.observe(viewLifecycleOwner) { days: Int ->
                viewBinding.tvRemainingDays.text = getString(R.string.remaining_days, days)
            }

            // Observe transactions
            recentTransactions.observe(viewLifecycleOwner) { transactions: List<FinancialRecord> ->
                adapter.submitList(transactions)
                viewBinding.tvEmptyTransactions.visibility = 
                    if (transactions.isEmpty()) View.VISIBLE else View.GONE
            }

            // Observe balance change
            balanceChange.observe(viewLifecycleOwner) { change: Double ->
                viewBinding.tvBalanceChange.text = getString(R.string.balance_change_format, change)
            }
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
        // TODO: Implement notification handling
    }

    override fun onToggleBalanceClick() {
        viewModel.toggleBalanceVisibility()
    }

    override fun onTabSelected(position: Int) {
        // Update the selected tab in the ViewModel
        viewModel.selectTab(position)
        
        // Force a refresh of the specific data for this tab
        if (position == HomeViewModel.TAB_EXPENSE) {
            viewModel.refreshExpenseData()
        } else {
            viewModel.refreshIncomeData()
        }
        
        // Cập nhật biểu đồ theo tab đã chọn
        updateBarChart(position == HomeViewModel.TAB_EXPENSE)
    }

    override fun onViewAllTransactionsClick() {
        safeNavigate(R.id.frg_all_income_expense)
    }

    override fun onClickItemEI(item: FinancialRecord) {
        safeNavigate(
            R.id.frg_edit_i_e,
            Bundle().apply {
                putString("transaction_id", item.id)
            }
        )
    }


    // Tuyệt đối không sửa
    override fun onIncomeCardClick() {
        safeNavigate(
            R.id.frag_enter,
            Bundle().apply {
                putInt("selected_tab", 0) // 0 for income tab
            }
        )
    }
    // Tuyệt đối không sửa
    override fun onExpenseCardClick() {
        safeNavigate(
            R.id.frag_enter,
            Bundle().apply {
                putInt("selected_tab", 1) // 1 for expense tab
            }
        )

    }

    override fun onClickCategory(item: CategoryOverView) {
        // Handle click on top category item
    }

    /**
     * Cập nhật BarChart để hiển thị thu nhập hoặc chi tiêu 6 tháng gần nhất
     * 
     * @param isExpenseTab true nếu đang hiển thị tab Chi tiêu, false nếu đang hiển thị tab Thu nhập
     */
    private fun updateBarChart(isExpenseTab: Boolean) {
        val entries = ArrayList<BarEntry>()
        val currentMonth = YearMonth.now()
        val months = ArrayList<String>()
        
        // Tạo dữ liệu cho 6 tháng gần nhất
        for (i in 5 downTo 0) {
            val monthToShow = currentMonth.minusMonths(i.toLong())
            val value = if (isExpenseTab) {
                viewModel.getMonthlyExpense(monthToShow)
            } else {
                viewModel.getMonthlyIncome(monthToShow)
            }
            entries.add(BarEntry((5-i).toFloat(), value.toFloat()))
            // Format tháng dạng "TX"
            months.add("T${monthToShow.monthValue}")
        }
        
        // Cập nhật các TextView hiển thị tháng
        viewBinding.apply {
            // Danh sách các TextView để dễ thao tác
            val monthViews = listOf(tvMonth1, tvMonth2, tvMonth3, tvMonth4, tvMonth5, tvMonth6)
            
            // Cập nhật tất cả TextView
            for (i in months.indices) {
                monthViews[i].apply {
                    text = months[i]
                    // Đặt tháng hiện tại màu orange và các tháng khác màu đen
                    if (months[i] == "T${currentMonth.monthValue}") {
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
                        setTypeface(null, android.graphics.Typeface.BOLD)
                    } else {
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                        setTypeface(null, android.graphics.Typeface.NORMAL)
                    }
                }
            }
        }
        
        val dataSet = BarDataSet(entries, "").apply {
            color = if (isExpenseTab) {
                ContextCompat.getColor(requireContext(), R.color.expense_color) 
            } else {
                ContextCompat.getColor(requireContext(), R.color.primary_color)
            }
            valueTextColor = Color.GRAY
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    if (value < 1000) return ""  // Không hiển thị giá trị quá nhỏ để tránh lộn xộn
                    return numberFormat.format(value.toLong())
                }
            }
        }
        
        val barData = BarData(dataSet).apply {
            barWidth = 0.5f  // Tăng độ rộng của cột để khớp với nhãn
        }
        
        viewBinding.barChart.apply {
            data = barData
            xAxis.apply {
                setDrawLabels(false)  // Tắt nhãn mặc định vì chúng ta sử dụng TextView
                setDrawGridLines(false)
                setDrawAxisLine(false)
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                setDrawAxisLine(false)
                setDrawZeroLine(false)
            }
            
            axisRight.isEnabled = false
            
            description.isEnabled = false
            legend.isEnabled = false
            
            // Điều chỉnh padding để khớp với LinearLayout chứa các TextView
            setExtraOffsets(0f, 0f, 0f, 0f)
            
            // Đảm bảo số lượng cột hiển thị đúng với số lượng tháng
            setVisibleXRangeMaximum(6f)
            
            // Chỉnh animation
            animateY(500)
            
            invalidate()  // Refresh biểu đồ
        }
    }

} 