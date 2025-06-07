package com.qltc.finace.view.main.list_data_form_category

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.base.Constant
import com.qltc.finace.databinding.FragmentListDataFromCategoryBinding
import com.qltc.finace.extension.toLocalDate
import com.qltc.finace.extension.toMonthYearString
import com.qltc.finace.view.adapter.AdapterExpenseIncomeReport
import com.qltc.finace.view.main.calendar.FinancialRecord
import com.qltc.finace.view.main.calendar.toFinancialRecord
import com.qltc.finace.view.main.report.ReportViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class FragmentListData : BaseFragment<FragmentListDataFromCategoryBinding, ReportViewModel>(),
    ListDataListener, AdapterExpenseIncomeReport.OnClickListener {

    override val viewModel: ReportViewModel by activityViewModels()
    override val layoutID: Int = R.layout.fragment_list_data_from_category
    private val adapter by lazy { AdapterExpenseIncomeReport(this) }
    private var idCategory = ""
    private var titleCategory = ""
    private var dataType = 0 // 0 = expense, 1 = income
    
    // Tạo LiveData riêng cho danh sách FinancialRecord đã lọc theo danh mục
    private val filteredRecords = MutableLiveData<List<FinancialRecord>>(emptyList())
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.apply {
            rcv.adapter = this@FragmentListData.adapter
            rcv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            listener = this@FragmentListData
        }
        try {
            titleCategory = arguments?.getString(Constant.TITLE_CATEGORY) ?: ""
            idCategory = arguments?.getString(Constant.KEY_ITEM_CATEGORY_OF_DATA) ?: ""
            dataType = arguments?.getInt(Constant.KEY_DATA_TYPE, 0) ?: 0
            Log.d("FragmentListData", "Arguments - Category: $titleCategory, ID: $idCategory, DataType: $dataType")
        }
        catch (e : Exception) {
            e.message?.let { Log.e("FragmentListData", "argument error $it") }
        }
        
        if (idCategory.isEmpty()) {
            findNavController().popBackStack()
            return
        }

        viewBinding.title.text = titleCategory
        
        // Quan sát filteredRecords để cập nhật UI
        filteredRecords.observe(viewLifecycleOwner) { records ->
            try {
                Log.d("FragmentListData", "LiveData observer triggered with ${records?.size ?: 0} records")
                
                if (records == null || records.isEmpty()) {
                    Log.d("FragmentListData", "No records found - showing empty view")
                    viewBinding.emptyView.visibility = View.VISIBLE
                    viewBinding.rcv.visibility = View.GONE
                } else {
                    Log.d("FragmentListData", "Found ${records.size} records - showing RecyclerView")
                    viewBinding.emptyView.visibility = View.GONE
                    viewBinding.rcv.visibility = View.VISIBLE
                    
                    // Log thông tin về records trước khi submit
                    records.take(3).forEach { record ->
                        Log.d("FragmentListData", "Record: ${record.titleCategory}, Money: ${record.money}, Type: ${record.typeExpenseOrIncome}")
                    }
                    
                    adapter.submitList(records)
                    Log.d("FragmentListData", "Submitted ${records.size} records to adapter")
                }
                
                // Log trạng thái cuối cùng của UI
                Log.d("FragmentListData", "Final UI state - EmptyView: ${viewBinding.emptyView.visibility}, RecyclerView: ${viewBinding.rcv.visibility}")
                
            } catch (e: Exception) {
                Log.e("FragmentListData", "Error in LiveData observer: ${e.message}", e)
            }
        }
        
        // Quan sát trạng thái refresh data
        viewModel.isDataRefreshed.observe(viewLifecycleOwner) { isRefreshed ->
            if (isRefreshed) {
                loadCategoryData()
            }
        }
        
        // Load dữ liệu ngay lập tức
        loadCategoryData()
    }
    
    override fun onClickItemEI(item: FinancialRecord) {
        // Có thể triển khai chức năng khi click vào item nếu cần
    }

    override fun onClickBack() {
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

    private fun loadCategoryData() {
        Log.d("FragmentListData", "Loading data for category: $idCategory, dataType: $dataType")
        debugDataInfo()
        
        // Hiển thị trạng thái loading nếu có
        try {
            viewBinding.progressCircular.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.e("FragmentListData", "Error showing progress: ${e.message}")
        }
        
        // Lấy tháng hiện tại từ ViewModel
        val currentMonth = YearMonth.from(viewModel.date)
        Log.d("FragmentListData", "Current month: $currentMonth, Date from viewModel: ${viewModel.date}")
        
        // Sử dụng lifecycleScope thay vì viewModelScope vì đây là Fragment
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Sử dụng dữ liệu đã có trong ViewModel thay vì gọi repository trực tiếp
                // Điều này đảm bảo dữ liệu đồng bộ với những gì đang hiển thị trong FragmentReport
                val categories = viewModel.listCategory
                val categoryMap = categories.associateBy { it.idCategory ?: "otherCategory" }
                
                val records = mutableListOf<FinancialRecord>()
                
                Log.d("FragmentListData", "Using ViewModel data - Categories: ${categories.size}, Expenses: ${viewModel.listExpense.size}, Incomes: ${viewModel.listIncome.size}")
                
                // Kiểm tra nếu ViewModel chưa có dữ liệu, load từ repository
                if (viewModel.listExpense.isEmpty() && viewModel.listIncome.isEmpty()) {
                    Log.d("FragmentListData", "ViewModel data is empty, loading from repository...")
                    withContext(Dispatchers.Main) {
                        // Gọi getAllData để load dữ liệu vào ViewModel
                        viewModel.getAllData { 
                            // Sau khi load xong, gọi lại loadCategoryData
                            loadCategoryData()
                        }
                    }
                    return@launch
                }
                
                // Xử lý dựa trên loại dữ liệu (expense hoặc income)
                if (dataType == 0) { // Expense
                    val allExpenses = viewModel.listExpense
                    Log.d("FragmentListData", "Total expenses from repository: ${allExpenses.size}")
                    
                    // Log một vài expense để debug
                    allExpenses.take(3).forEach { expense ->
                        Log.d("FragmentListData", "Sample expense - ID: ${expense.idExpense}, Category: ${expense.idCategory}, Date: ${expense.date}, YearMonth: ${expense.getYearMonth()}")
                    }
                    
                    val currentMonthString = currentMonth.toString()
                    Log.d("FragmentListData", "Filtering for month: $currentMonthString, category: $idCategory")
                    
                    val filteredExpenses = allExpenses.filter { expense ->
                        val categoryMatch = expense.idCategory == idCategory
                        val monthMatch = expense.getYearMonth() == currentMonthString
                        Log.d("FragmentListData", "Expense ${expense.idExpense}: categoryMatch=$categoryMatch (${expense.idCategory} vs $idCategory), monthMatch=$monthMatch (${expense.getYearMonth()} vs $currentMonthString)")
                        categoryMatch && monthMatch
                    }
                    
                    Log.d("FragmentListData", "Filtered expenses: ${filteredExpenses.size}")
                    
                    records.addAll(filteredExpenses.map { expense ->
                        val category = categoryMap[expense.idCategory]
                    FinancialRecord(
                        idCategory = expense.idCategory,
                        id = expense.idExpense,
                        idUser = expense.idUser,
                        noteExpenseIncome = expense.note,
                        date = expense.date,
                        money = expense.expense,
                        typeExpenseOrIncome = FinancialRecord.TYPE_EXPENSE,
                        titleCategory = category?.title,
                            icon = category?.icon
                        )
                    })
                    
                    Log.d("FragmentListData", "Found ${records.size} expense records for category $idCategory")
                    
                } else { // Income
                    val allIncomes = viewModel.listIncome
                    Log.d("FragmentListData", "Total incomes from repository: ${allIncomes.size}")
                    
                    // Log một vài income để debug
                    allIncomes.take(3).forEach { income ->
                        Log.d("FragmentListData", "Sample income - ID: ${income.idIncome}, Category: ${income.idCategory}, Date: ${income.date}, YearMonth: ${income.getYearMonth()}")
                    }
                    
                    val currentMonthString = currentMonth.toString()
                    Log.d("FragmentListData", "Filtering for month: $currentMonthString, category: $idCategory")
                    
                    val filteredIncomes = allIncomes.filter { income ->
                        val categoryMatch = income.idCategory == idCategory
                        val monthMatch = income.getYearMonth() == currentMonthString
                        Log.d("FragmentListData", "Income ${income.idIncome}: categoryMatch=$categoryMatch (${income.idCategory} vs $idCategory), monthMatch=$monthMatch (${income.getYearMonth()} vs $currentMonthString)")
                        categoryMatch && monthMatch
                    }
                    
                    Log.d("FragmentListData", "Filtered incomes: ${filteredIncomes.size}")
                    
                    records.addAll(filteredIncomes.map { income ->
                        val category = categoryMap[income.idCategory]
                        FinancialRecord(
                            idCategory = income.idCategory,
                            id = income.idIncome,
                            idUser = income.idUser,
                            noteExpenseIncome = income.note,
                            date = income.date,
                            money = income.income,
                            typeExpenseOrIncome = FinancialRecord.TYPE_INCOME,
                            titleCategory = category?.title,
                        icon = category?.icon
                    )
                    })
                    
                    Log.d("FragmentListData", "Found ${records.size} income records for category $idCategory")
                }
                
                // Log để debug thông tin
                if (records.isNotEmpty()) {
                    val sampleRecord = records.first()
                    Log.d("FragmentListData", "Sample record - Category: ${sampleRecord.titleCategory}, Icon: ${sampleRecord.icon}, Type: ${sampleRecord.typeExpenseOrIncome}")
                }
                
                // Cập nhật UI trên main thread
                withContext(Dispatchers.Main) {
                    try {
                        Log.d("FragmentListData", "Updating UI on main thread with ${records.size} records")
                        viewBinding.progressCircular.visibility = View.GONE
                        
                        // Đảm bảo cập nhật LiveData
                        filteredRecords.value = records
                        
                        // Log trạng thái UI sau khi cập nhật
                        Log.d("FragmentListData", "UI updated - EmptyView visible: ${viewBinding.emptyView.visibility == View.VISIBLE}, RecyclerView visible: ${viewBinding.rcv.visibility == View.VISIBLE}")
                        
                    } catch (e: Exception) {
                        Log.e("FragmentListData", "Error updating UI: ${e.message}")
                    }
                }
                
            } catch (e: Exception) {
                Log.e("FragmentListData", "Error loading data", e)
                withContext(Dispatchers.Main) {
                    try {
                        viewBinding.progressCircular.visibility = View.GONE
                        viewBinding.emptyView.visibility = View.VISIBLE
                        viewBinding.rcv.visibility = View.GONE
                    } catch (e: Exception) {
                        Log.e("FragmentListData", "Error updating UI after exception: ${e.message}")
                    }
                }
            }
        }
    }

    // Đảm bảo fragment tải lại dữ liệu mỗi khi hiển thị
    override fun onResume() {
        super.onResume()
        if (idCategory.isNotEmpty()) {
            Log.d("FragmentListData", "onResume - reloading data for category: $idCategory, type: $dataType")
            loadCategoryData() // Tải dữ liệu trực tiếp
        }
    }
    
    // Method để debug và kiểm tra dữ liệu
    private fun debugDataInfo() {
        Log.d("FragmentListData", "=== DEBUG INFO ===")
        Log.d("FragmentListData", "Category ID: $idCategory")
        Log.d("FragmentListData", "Category Title: $titleCategory") 
        Log.d("FragmentListData", "Data Type: $dataType (0=expense, 1=income)")
        Log.d("FragmentListData", "Current Month: ${YearMonth.from(viewModel.date)}")
        Log.d("FragmentListData", "Total Expenses in ViewModel: ${viewModel.listExpense.size}")
        Log.d("FragmentListData", "Total Incomes in ViewModel: ${viewModel.listIncome.size}")
        
        // Debug categories có sẵn
        val categoriesForType = if (dataType == 0) {
            viewModel.listExpense.map { it.idCategory }.distinct()
        } else {
            viewModel.listIncome.map { it.idCategory }.distinct()
        }
        Log.d("FragmentListData", "Available categories for type $dataType: $categoriesForType")
        
        // Debug months có sẵn
        val monthsForCategory = if (dataType == 0) {
            viewModel.listExpense.filter { it.idCategory == idCategory }.map { it.getYearMonth() }.distinct()
        } else {
            viewModel.listIncome.filter { it.idCategory == idCategory }.map { it.getYearMonth() }.distinct()
        }
        Log.d("FragmentListData", "Available months for category $idCategory: $monthsForCategory")
        Log.d("FragmentListData", "==================")
    }
}