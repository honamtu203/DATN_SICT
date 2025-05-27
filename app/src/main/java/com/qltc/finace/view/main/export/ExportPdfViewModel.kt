package com.qltc.finace.view.main.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.charts.PieChart
import com.qltc.finace.base.BaseViewModel
import com.qltc.finace.base.SingleLiveData
import com.qltc.finace.data.entity.CategoryExpenseDetail
import com.qltc.finace.data.entity.CategoryIncomeDetail
import com.qltc.finace.data.entity.Category
import com.qltc.finace.data.entity.Expense
import com.qltc.finace.data.entity.Income
import com.qltc.finace.data.repository.local.category.CategoryRepository
import com.qltc.finace.data.repository.local.expense.ExpenseRepository
import com.qltc.finace.data.repository.local.income.InComeRepository
import com.qltc.finace.utils.PdfExportHelper
import com.qltc.finace.extension.toMonthYearString
import com.qltc.finace.extension.toLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ExportPdfViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: InComeRepository,
    private val categoryRepository: CategoryRepository
) : BaseViewModel() {
    
    private val _pdfGenerationResult = SingleLiveData<Uri?>()
    val pdfGenerationResult: LiveData<Uri?> = _pdfGenerationResult
    
    private val _errorMessage = MutableLiveData<String>("")
    val errorMessage: LiveData<String> = _errorMessage
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _listCategory = MutableLiveData<List<Category>>(listOf())
    val listCategory: LiveData<List<Category>> = _listCategory

    // Store data in memory for filtering
    private var _listExpense = mutableListOf<Expense>()
    private var _listIncome = mutableListOf<Income>()
    
    // Flag to track if data is ready
    private val _isDataReady = SingleLiveData<Boolean>(false)
    val isDataReady: LiveData<Boolean> = _isDataReady
    
    private var currentJob: Job? = null
    
    init {
        loadAllData()
    }
    
    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }
    
    private fun loadAllData() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch all data from repositories
                val categories = categoryRepository.getAll()
                val expenses = expenseRepository.getAllExpense()
                val incomes = incomeRepository.getAllIncome()
                
                withContext(Dispatchers.Main) {
                    _listCategory.value = categories
                    _listExpense = expenses
                    _listIncome = incomes
                    _isDataReady.value = true
                    _isLoading.value = false
                    Log.d(TAG, "Data loaded: ${categories.size} categories, ${expenses.size} expenses, ${incomes.size} incomes")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading data: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Không thể tải dữ liệu: ${e.message}"
                    _isLoading.value = false
                }
            }
        }
    }
    
    fun generatePdf(
        context: Context,
        fileName: String,
        month: YearMonth,
        reportType: Int,
        displayOptions: Int,
        expensePieChart: PieChart? = null,
        incomePieChart: PieChart? = null
    ) {
        if (fileName.isBlank()) {
            _errorMessage.value = "Vui lòng nhập tên file"
            return
        }
        
        if (!fileName.matches("[a-zA-Z0-9_\\- ]+".toRegex())) {
            _errorMessage.value = "Tên file chỉ được chứa chữ cái, số, dấu gạch ngang, gạch dưới và khoảng trắng"
            return
        }
        
        if (!_isDataReady.value!!) {
            _errorMessage.value = "Đang tải dữ liệu, vui lòng thử lại sau"
            loadAllData() // Try to reload data
            return
        }
        
        _isLoading.value = true
        _errorMessage.value = ""
        
        currentJob?.cancel()
        
        currentJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                // Get the first and last day of the selected month
                val firstDayOfMonth = month.atDay(1)
                val lastDayOfMonth = month.atEndOfMonth()
                
                Log.d(TAG, "Export PDF for month: $month (${firstDayOfMonth} to ${lastDayOfMonth})")
                
                var expenseData: List<CategoryExpenseDetail>? = null
                var incomeData: List<CategoryIncomeDetail>? = null
                
                // Filter expenses and incomes for the selected month
                if (reportType == PdfExportHelper.TYPE_EXPENSE || reportType == PdfExportHelper.TYPE_BOTH) {
                    val expensesInMonth = _listExpense.filter { expense ->
                        try {
                            val expenseDate = expense.date.toLocalDate()
                            YearMonth.from(expenseDate) == month
                        } catch (e: Exception) {
                            Log.w(TAG, "Invalid date format in expense: ${expense.date}")
                            false
                        }
                    }
                    
                    if (expensesInMonth.isNotEmpty()) {
                        val expensesByCategory = expensesInMonth.groupBy { it.idCategory }
                        expenseData = expensesByCategory.mapNotNull { (categoryId, expenses) ->
                            val category = _listCategory.value?.find { it.idCategory == categoryId }
                            if (category != null) {
                                CategoryExpenseDetail(
                                    category = category,
                                    totalAmount = expenses.sumOf { it.expense ?: 0 },
                                    listExpense = expenses
                                )
                            } else null
                        }
                    } else {
                        Log.d(TAG, "No expenses found in month")
                    }
                }
                
                // Filter income for the selected month
                if (reportType == PdfExportHelper.TYPE_INCOME || reportType == PdfExportHelper.TYPE_BOTH) {
                    val incomesInMonth = _listIncome.filter { income ->
                        try {
                            val incomeDate = income.date.toLocalDate()
                            YearMonth.from(incomeDate) == month
                        } catch (e: Exception) {
                            Log.w(TAG, "Invalid date format in income: ${income.date}")
                            false
                        }
                    }
                    
                    if (incomesInMonth.isNotEmpty()) {
                        val incomesByCategory = incomesInMonth.groupBy { it.idCategory }
                        incomeData = incomesByCategory.mapNotNull { (categoryId, incomes) ->
                            val category = _listCategory.value?.find { it.idCategory == categoryId }
                            if (category != null) {
                                CategoryIncomeDetail(
                                    category = category,
                                    totalAmount = incomes.sumOf { it.income ?: 0 },
                                    listIncome = incomes
                                )
                            } else null
                        }
                    } else {
                        Log.d(TAG, "No incomes found in month")
                    }
                }
                
                // Format date for error messages
                val dateFormatter = DateTimeFormatter.ofPattern("MM/yyyy", Locale.getDefault())
                val monthFormatted = month.format(dateFormatter)
                
                // Check if there's no data to export
                if ((reportType == PdfExportHelper.TYPE_EXPENSE || reportType == PdfExportHelper.TYPE_BOTH) && 
                    (expenseData == null || expenseData.isEmpty())) {
                    
                    val message = if (_listExpense.isEmpty()) {
                        "Không tìm thấy dữ liệu chi tiêu nào"
                    } else {
                        "Không có dữ liệu chi tiêu trong tháng $monthFormatted"
                    }
                    
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = message
                        _isLoading.value = false
                    }
                    return@launch
                }
                
                if ((reportType == PdfExportHelper.TYPE_INCOME || reportType == PdfExportHelper.TYPE_BOTH) && 
                    (incomeData == null || incomeData.isEmpty())) {
                    
                    val message = if (_listIncome.isEmpty()) {
                        "Không tìm thấy dữ liệu thu nhập nào"
                    } else {
                        "Không có dữ liệu thu nhập trong tháng $monthFormatted"
                    }
                    
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = message
                        _isLoading.value = false
                    }
                    return@launch
                }
                
                try {
                    val pdfHelper = PdfExportHelper(context)
                    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.pdf")
                            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
                        }
                        pdfHelper.createPdfReportWithMediaStore(
                            contentValues = contentValues,
                            startDate = firstDayOfMonth,
                            endDate = lastDayOfMonth,
                            reportType = reportType,
                            displayOptions = displayOptions,
                            expenseData = expenseData,
                            incomeData = incomeData,
                            expensePieChart = expensePieChart,
                            incomePieChart = incomePieChart
                        )
                    } else {
                        pdfHelper.createPdfReport(
                            fileName = fileName,
                            startDate = firstDayOfMonth,
                            endDate = lastDayOfMonth,
                            reportType = reportType,
                            displayOptions = displayOptions,
                            expenseData = expenseData,
                            incomeData = incomeData,
                            expensePieChart = expensePieChart,
                            incomePieChart = incomePieChart
                        )
                    }
                    
                    withContext(Dispatchers.Main) {
                        _pdfGenerationResult.value = uri
                        _isLoading.value = false
                        
                        // Calculate total transactions exported
                        val totalExpenseTransactions = expenseData?.sumOf { it.listExpense?.size ?: 0 } ?: 0
                        val totalIncomeTransactions = incomeData?.sumOf { it.listIncome?.size ?: 0 } ?: 0
                        
                        Log.d(TAG, "PDF export successful. Exported $totalExpenseTransactions expenses and $totalIncomeTransactions incomes for month: $monthFormatted")
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Error writing PDF file: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = "Lỗi khi ghi file PDF: ${e.message}"
                        _isLoading.value = false
                    }
                } catch (e: SecurityException) {
                    Log.e(TAG, "Security exception when creating PDF: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = "Không có quyền truy cập bộ nhớ: ${e.message}"
                        _isLoading.value = false
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error generating PDF file: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = "Lỗi khi tạo file PDF: ${e.message}"
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating PDF: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Lỗi khi tạo PDF: ${e.message}"
                    _isLoading.value = false
                }
            }
        }
    }
    
    fun refreshData() {
        loadAllData()
    }
    
    /**
     * Lấy dữ liệu chi tiêu cho tháng được chọn
     */
    fun getExpenseDataForMonth(month: YearMonth): List<CategoryExpenseDetail>? {
        try {
            if (_listExpense == null || _listExpense.isEmpty() || _listCategory.value.isNullOrEmpty()) {
                Log.d(TAG, "No expense data available")
                return null
            }
            
            val firstDay = month.atDay(1)
            val lastDay = month.atEndOfMonth()
            
            // Filter expenses for the month
            val expensesForMonth = _listExpense.filter { expense ->
                val expenseDate = expense.date?.let { LocalDate.parse(it) }
                expenseDate != null && !expenseDate.isBefore(firstDay) && !expenseDate.isAfter(lastDay)
            }
            
            if (expensesForMonth.isEmpty()) {
                Log.d(TAG, "No expenses found for month: $month")
                return null
            }
            
            // Group by category
            val groupedExpenses = expensesForMonth.groupBy { it.idCategory }
            val result = mutableListOf<CategoryExpenseDetail>()
            
            // Create CategoryExpenseDetail for each category
            groupedExpenses.forEach { (categoryId, expenses) ->
                val category = _listCategory.value?.find { it.idCategory == categoryId }
                val totalAmount = expenses.sumOf { it.expense ?: 0 }
                
                if (totalAmount > 0) {
                    result.add(
                        CategoryExpenseDetail(
                            category = category,
                            totalAmount = totalAmount,
                            listExpense = expenses
                        )
                    )
                }
            }
            
            return result.sortedByDescending { it.totalAmount }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting expense data for month: ${e.message}", e)
            return null
        }
    }
    
    /**
     * Lấy dữ liệu thu nhập cho tháng được chọn
     */
    fun getIncomeDataForMonth(month: YearMonth): List<CategoryIncomeDetail>? {
        try {
            if (_listIncome == null || _listIncome.isEmpty() || _listCategory.value.isNullOrEmpty()) {
                Log.d(TAG, "No income data available")
                return null
            }
            
            val firstDay = month.atDay(1)
            val lastDay = month.atEndOfMonth()
            
            // Filter incomes for the month
            val incomesForMonth = _listIncome.filter { income ->
                val incomeDate = income.date?.let { LocalDate.parse(it) }
                incomeDate != null && !incomeDate.isBefore(firstDay) && !incomeDate.isAfter(lastDay)
            }
            
            if (incomesForMonth.isEmpty()) {
                Log.d(TAG, "No incomes found for month: $month")
                return null
            }
            
            // Group by category
            val groupedIncomes = incomesForMonth.groupBy { it.idCategory }
            val result = mutableListOf<CategoryIncomeDetail>()
            
            // Create CategoryIncomeDetail for each category
            groupedIncomes.forEach { (categoryId, incomes) ->
                val category = _listCategory.value?.find { it.idCategory == categoryId }
                val totalAmount = incomes.sumOf { it.income ?: 0 }
                
                if (totalAmount > 0) {
                    result.add(
                        CategoryIncomeDetail(
                            category = category,
                            totalAmount = totalAmount,
                            listIncome = incomes
                        )
                    )
                }
            }
            
            return result.sortedByDescending { it.totalAmount }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting income data for month: ${e.message}", e)
            return null
        }
    }
    
    companion object {
        private const val TAG = "ExportPdfViewModel"
    }
} 