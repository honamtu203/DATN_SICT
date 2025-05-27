package com.qltc.finace.view.main.report

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.tabs.TabLayout
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.base.Constant
import com.qltc.finace.data.entity.CategoryOverView
import com.qltc.finace.databinding.FagmentReportBinding
import com.qltc.finace.extension.formatDateTime
import com.qltc.finace.extension.formatMonthVN
import com.qltc.finace.extension.navigateWithAnim
import com.qltc.finace.view.adapter.AdapterTotalCategory
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.YearMonth

@AndroidEntryPoint
class FragmentReport : BaseFragment<FagmentReportBinding, ReportViewModel>(), ReportListener,
    AdapterTotalCategory.OnClickListener, OnChartValueSelectedListener {
    companion object {
        const val CHOOSE_EXPENSE = 0
        const val CHOOSE_INCOME = 1
    }
    override val layoutID: Int = R.layout.fagment_report
    override val viewModel: ReportViewModel by activityViewModels()
    private val adapterRcv by lazy { AdapterTotalCategory(this) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        android.util.Log.d("FragmentReport", "onViewCreated - Current typeReport: ${viewModel.typeReport.value}")
        
        viewBinding.apply {
            viewModel = this@FragmentReport.viewModel
        }
        
        // Kiểm tra xem dữ liệu đã có sẵn chưa
        if (viewModel.listExpense.isNotEmpty() || viewModel.listIncome.isNotEmpty()) {
            android.util.Log.d("FragmentReport", "Data already available, restoring tab state immediately")
            android.util.Log.d("FragmentReport", "Current typeReport: ${viewModel.typeReport.value}")
            // Dữ liệu đã có, khôi phục tab ngay lập tức
            restoreTabState()
        } else {
            android.util.Log.d("FragmentReport", "Loading data first...")
            // Load dữ liệu và khôi phục trạng thái tab
        viewModel.getAllData (callBack = {
                android.util.Log.d("FragmentReport", "getAllData callback - typeReport: ${viewModel.typeReport.value}")
                android.util.Log.d("FragmentReport", "TabLayout ready: ${viewBinding.tabLayoutReport.tabCount > 0}")
                // Sau khi load dữ liệu xong, khôi phục tab dựa trên viewModel.typeReport.value
                restoreTabState()
            })
        }
        
        setTimeDefault()
        setUpTabLayout()
        setUpRecyclerView()
        setUpPieChart()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
    }
    override fun openDayPicker() {
        val currentYearMonth = YearMonth.from(viewModel.date)
        val dialog = MonthYearPickerDialog(
            requireContext(),
            currentYearMonth
        ) { selectedYearMonth ->
            // Cập nhật viewModel.date với ngày đầu tiên của tháng được chọn
            viewModel.date = selectedYearMonth.atDay(1)
            
            // Cập nhật hiển thị tháng/năm trên pickTime
            val monthText = "Tháng ${selectedYearMonth.monthValue}/${selectedYearMonth.year}"
            viewBinding.pickTime.text = monthText
            
                // Cập nhật dữ liệu dựa vào tab hiện tại
                if (viewModel?.typeReport?.value == CHOOSE_EXPENSE) {
                viewModel.prepareDataPieChartExpenseByMonth(selectedYearMonth)
                    notifyRecyclerViewNeedUpdate()
                }
                else {
                viewModel.filterDataIncomeByMonth(selectedYearMonth)
                    updateIncomeRecyclerView()
                }
                updateMonthDisplay()
        }
        dialog.show()
    }

    override fun openViewAll() {
        findNavController().navigateWithAnim(R.id.frg_all_income_expense, bundleOf())
    }

    private fun setTimeDefault() {
        val yearMonth = YearMonth.from(viewModel.date)
        val monthText = "Tháng ${yearMonth.monthValue}/${yearMonth.year}"
        viewBinding.pickTime.text = monthText
        updateMonthDisplay()
    }

    override fun onClickItemEI(item: CategoryOverView) {
        findNavController().navigateWithAnim(R.id.frg_list_data, bundleOf(
            Constant.KEY_ITEM_CATEGORY_OF_DATA to item.category.idCategory,
            Constant.TITLE_CATEGORY to item.category.title,
            // Truyền thêm thông tin về loại dữ liệu hiện tại (expense/income)
            Constant.KEY_DATA_TYPE to (viewModel.typeReport.value ?: CHOOSE_EXPENSE)
        ))
    }
    private fun setUpTabLayout() {
        viewBinding.apply {
            listener = this@FragmentReport
        }
        setUpTabLayoutListener()
    }
    
    private fun setUpTabLayoutListener() {
        viewBinding.tabLayoutReport.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
                override fun onTabSelected(p0: TabLayout.Tab?) {
                android.util.Log.d("FragmentReport", "Tab selected: ${p0?.position}")
                    if (p0?.position == CHOOSE_EXPENSE) {
                    android.util.Log.d("FragmentReport", "Switching to expense tab")
                        viewModel?.typeReport?.value = CHOOSE_EXPENSE
                        // Hiển thị dữ liệu chi tiêu
                        viewBinding.mChart.centerText = "Khoản chi"
                        observeExpenseData()
                    }
                    else if (p0?.position == CHOOSE_INCOME) {
                    android.util.Log.d("FragmentReport", "Switching to income tab")
                        viewModel?.typeReport?.value = CHOOSE_INCOME
                        // Hiển thị dữ liệu khoản thu
                        viewBinding.mChart.centerText = "Khoản thu"
                        observeIncomeData()
                    }
                }

                override fun onTabUnselected(p0: TabLayout.Tab?) {}

                override fun onTabReselected(p0: TabLayout.Tab?) {}
            })
    }
    private fun setUpRecyclerView() {
        viewBinding.apply {
            rcv.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            rcv.adapter = this@FragmentReport.adapterRcv
            rcv.isNestedScrollingEnabled = false;
            val itemDeclaration =
                DividerItemDecoration(this@FragmentReport.context, DividerItemDecoration.VERTICAL)
            rcv.addItemDecoration(itemDeclaration)
        }
        // Observer sẽ được setup trong observeExpenseData() hoặc observeIncomeData()
        // Không setup observer ở đây để tránh conflict
        updateMonthDisplay()
    }
    private fun setUpPieChart() {
        viewBinding.mChart.apply {
            setPieChartReportDefault(
                if (viewModel.typeReport.value == CHOOSE_EXPENSE) "Khoản chi" else "Khoản thu"
            )
            setOnChartValueSelectedListener(this@FragmentReport)
        }
        
        // Không khởi tạo observer ở đây nữa
        // Observer sẽ được khởi tạo trong restoreTabState() sau khi load dữ liệu xong
    }
    private fun notifyRecyclerViewNeedUpdate() {
        viewModel.prepareRecyclerViewExpense(YearMonth.from(viewModel.date))
        updateMonthDisplay()
    }
    private fun updateMonthDisplay() {
        val monthText = context?.getString(R.string.month) + " " + YearMonth.from(viewModel.date).formatMonthVN()
        viewBinding.monthSelected.text = monthText
    }
    override fun onResume() {
        super.onResume()
        android.util.Log.d("FragmentReport", "onResume - Current typeReport: ${viewModel.typeReport.value}")
        // Cập nhật dữ liệu từ database mỗi khi fragment được hiển thị lại
        // refreshData() sẽ tự động cập nhật dữ liệu dựa trên typeReport.value hiện tại
        viewModel.refreshData()
        updateMonthDisplay()
    }
    override fun onValueSelected(p0: Entry?, p1: Highlight?) {

    }

    override fun onNothingSelected() {

    }

    private fun observeExpenseData() {
        android.util.Log.d("FragmentReport", "Setting up expense observers")
        
        // Remove any existing observers to avoid conflicts
        viewModel.dataPieChar.removeObservers(viewLifecycleOwner)
        viewModel.dataRcv.removeObservers(viewLifecycleOwner)
        viewModel.dataIncomePieChar.removeObservers(viewLifecycleOwner)
        viewModel.dataIncomeRcv.removeObservers(viewLifecycleOwner)
        
        // Đăng ký observer cho dữ liệu chi tiêu
        viewModel.dataPieChar.observe(viewLifecycleOwner) {
            android.util.Log.d("FragmentReport", "Expense pie chart data updated: ${it.size} entries")
            viewBinding.mChart.submitList(it)
            notifyRecyclerViewNeedUpdate()
        }
        viewModel.dataRcv.observe(viewLifecycleOwner) {
            android.util.Log.d("FragmentReport", "Expense recycler data updated: ${it.size} items")
            adapterRcv.submitList(it)
        }
        viewModel.prepareDataPieChartExpenseByMonth(YearMonth.from(viewModel.date))
    }
    
    private fun observeIncomeData() {
        android.util.Log.d("FragmentReport", "Setting up income observers")
        
        // Remove any existing observers to avoid conflicts
        viewModel.dataPieChar.removeObservers(viewLifecycleOwner)
        viewModel.dataRcv.removeObservers(viewLifecycleOwner)
        viewModel.dataIncomePieChar.removeObservers(viewLifecycleOwner)
        viewModel.dataIncomeRcv.removeObservers(viewLifecycleOwner)
        
        // Đăng ký observer cho dữ liệu khoản thu
        viewModel.dataIncomePieChar.observe(viewLifecycleOwner) {
            android.util.Log.d("FragmentReport", "Income pie chart data updated: ${it.size} entries")
            viewBinding.mChart.submitList(it)
            updateIncomeRecyclerView()
        }
        viewModel.dataIncomeRcv.observe(viewLifecycleOwner) {
            android.util.Log.d("FragmentReport", "Income recycler data updated: ${it.size} items")
            adapterRcv.submitList(it)
        }
        viewModel.filterDataIncomeByMonth(YearMonth.from(viewModel.date))
    }
    
    private fun updateIncomeRecyclerView() {
        viewModel.rcvIncomePrepare(YearMonth.from(viewModel.date))
        updateMonthDisplay()
    }
    
    /**
     * Khôi phục trạng thái tab dựa trên viewModel.typeReport.value
     * Được gọi sau khi load dữ liệu xong
     */
    private fun restoreTabState() {
        val currentTabType = viewModel.typeReport.value ?: CHOOSE_EXPENSE
        
        android.util.Log.d("FragmentReport", "Restoring tab state: $currentTabType (0=expense, 1=income)")
        android.util.Log.d("FragmentReport", "TabLayout tab count: ${viewBinding.tabLayoutReport.tabCount}")
        
        // Post to UI thread để đảm bảo TabLayout đã sẵn sàng
        viewBinding.tabLayoutReport.post {
            // Tạm thời remove listener để tránh trigger khi set tab
            val listener = viewBinding.tabLayoutReport.getTabAt(0)?.parent as? TabLayout
            viewBinding.tabLayoutReport.clearOnTabSelectedListeners()
            
            // Set tab hiện tại
            val targetTab = viewBinding.tabLayoutReport.getTabAt(currentTabType)
            android.util.Log.d("FragmentReport", "Target tab: $targetTab, position: $currentTabType")
            
            if (targetTab != null) {
                viewBinding.tabLayoutReport.selectTab(targetTab)
                android.util.Log.d("FragmentReport", "Tab selected successfully. Current selected: ${viewBinding.tabLayoutReport.selectedTabPosition}")
            } else {
                android.util.Log.e("FragmentReport", "Target tab is null!")
            }
            
            // Restore listener
            setUpTabLayoutListener()
            
            // Khởi tạo dữ liệu cho tab hiện tại
            if (currentTabType == CHOOSE_EXPENSE) {
                android.util.Log.d("FragmentReport", "Initializing expense data")
                viewBinding.mChart.centerText = "Khoản chi"
                observeExpenseData()
            } else {
                android.util.Log.d("FragmentReport", "Initializing income data")
                viewBinding.mChart.centerText = "Khoản thu"
                observeIncomeData()
            }
        }
    }
}