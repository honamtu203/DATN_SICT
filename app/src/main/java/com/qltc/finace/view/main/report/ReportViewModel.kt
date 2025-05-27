package com.qltc.finace.view.main.report

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.PieEntry
import com.qltc.finace.R
import com.qltc.finace.base.BaseViewModel
import com.qltc.finace.base.SingleLiveData
import com.qltc.finace.base.TAG
import com.qltc.finace.data.entity.Category
import com.qltc.finace.data.entity.CategoryExpenseDetail
import com.qltc.finace.data.entity.CategoryIncomeDetail
import com.qltc.finace.data.entity.Expense
import com.qltc.finace.data.entity.CategoryOverView
import com.qltc.finace.data.entity.Income
import com.qltc.finace.data.entity.toPieEntry
import com.qltc.finace.data.repository.local.category.CategoryRepository
import com.qltc.finace.data.repository.local.expense.ExpenseRepository
import com.qltc.finace.data.repository.local.income.InComeRepository
import com.qltc.finace.extension.sumExpenseMoney
import com.qltc.finace.extension.sumIncomeMoney
import com.qltc.finace.view.main.calendar.FinancialRecord
import com.qltc.finace.view.main.calendar.toFinancialRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    @ApplicationContext val applicationContext: Context,
    val inComeRepository: InComeRepository,
    val expenseRepository: ExpenseRepository,
    val categoryRepository: CategoryRepository
) : BaseViewModel() {
    var typeReport = MutableLiveData(FragmentReport.CHOOSE_EXPENSE)
    var date: LocalDate = LocalDate.now()
    var listIncome: MutableList<Income> = mutableListOf()
    var listExpense: MutableList<Expense> = mutableListOf()

    // Add observer for typeReport changes to update data accordingly
    init {
        // Không làm gì tự động khi thay đổi tab
        // Observer sẽ được quản lý bởi FragmentReport
    }

    var dataPieChar = MutableLiveData<MutableList<PieEntry>>(mutableListOf())
    var listCategory = mutableListOf<Category>()
    var total = MutableLiveData(0L)

    var listCategoryExpenseDetailDec:
            MutableList<CategoryExpenseDetail> = mutableListOf()
    var listIncomeWithCategoryDec:
            MutableLiveData<MutableList<Triple<Category, Long, List<Income>>>> = MutableLiveData(mutableListOf())

    var dataRcv : MutableLiveData<MutableList<CategoryOverView>> = MutableLiveData(mutableListOf())

    // Thêm LiveData để theo dõi trạng thái refresh
    private val _isDataRefreshed = MutableLiveData<Boolean>()
    val isDataRefreshed: LiveData<Boolean> = _isDataRefreshed

    // Add new LiveData for PieChart income data
    var dataIncomePieChar = MutableLiveData<MutableList<PieEntry>>(mutableListOf())
    // Add new LiveData for income RecyclerView data
    var dataIncomeRcv : MutableLiveData<MutableList<CategoryOverView>> = MutableLiveData(mutableListOf())

    fun getAllData(callBack : (Int) -> Unit) {
        Log.d(TAG, "getAllData called - current typeReport: ${typeReport.value}")
        viewModelScope.launch(Dispatchers.IO) {
            val lExpense = expenseRepository.getAllExpense()
            val lCategory = categoryRepository.getAll()
            val lIncome = inComeRepository.getAllIncome()
            withContext(Dispatchers.Main) {
                Log.d(TAG, "Data loaded: ${lExpense.size} expenses, ${lIncome.size} incomes")
                listExpense = lExpense
                listCategory = lCategory
                listIncome = lIncome
                Log.d(TAG, "Invoking callback with typeReport: ${typeReport.value}")
                callBack.invoke(typeReport.value ?: FragmentReport.CHOOSE_EXPENSE)
                calculateTotal()
            }
        }
    }
//    fun filterExpenseByCategory() {
//        val l = listCategory.map { item ->
//            val l = getExpenseByCategory(item)
//            CategoryExpenseDetail(
//                category = item,
//                totalAmount = l.sumExpenseMoney(),
//                listExpense = l
//            )
//        }.toMutableList()
//        listExpenseWithCategoryDec.sortedByDescending { it.totalAmount }.toMutableList()
//    }
    private fun getExpenseByCategory(category: Category): List<Expense> {
        return listExpense.filter { it.idCategory == category.idCategory }
    }
    private fun getExpenseByMonth(month: YearMonth): List<Expense> {
        return listExpense.filter { it.getYearMonth() == month.toString() }
    }

    fun prepareDataPieChartExpenseByMonth(month: YearMonth) {
        viewModelScope.launch(Dispatchers.IO) {
            val listCategoryExpenseDetailOfMonthDec: MutableList<CategoryExpenseDetail> =
                getExpenseWithCategoryOfMonth(month)
            listCategoryExpenseDetailOfMonthDec.sortByDescending { it.totalAmount }

            val listPieEntry = addItemEntry(listCategoryExpenseDetailOfMonthDec)
            addItemEntryOther(listCategoryExpenseDetailOfMonthDec)?.let {
                listPieEntry.add(POSITION_ITEM_OTHER,it)
            }
            withContext(Dispatchers.Main) {
                this@ReportViewModel.listCategoryExpenseDetailDec = listCategoryExpenseDetailOfMonthDec
                this@ReportViewModel.dataPieChar.value = listPieEntry
            }
        }
    }
    private fun addItemEntry(listCategoryExpenseDetail: MutableList<CategoryExpenseDetail>) : MutableList<PieEntry> {
        val listPieEntry = listCategoryExpenseDetail
            .take(COUNT_ITEM_PIE_CHART + 1)
            .map { it.toPieEntry() }
            .toMutableList()
        return listPieEntry
    }
    private fun getExpenseWithCategoryOfMonth(month: YearMonth): MutableList<CategoryExpenseDetail> {
        val listExpenseOfMonth = getExpenseByMonth(month)
        return listExpenseOfMonth.groupBy { it.idCategory }.map { (idCategory, listExpense) ->
                CategoryExpenseDetail(
                    category = getCategoryObject(idCategory),
                    totalAmount = listExpense.sumExpenseMoney(),
                    listExpense = listExpense
                )
            }.toMutableList()
    }

    // Thêm getter cho CategoryExpenseDetail
    fun getCategoryExpenseDetailByID(categoryID: String): CategoryExpenseDetail? {
        return listCategoryExpenseDetailDec.find { it.category?.idCategory == categoryID }
    }

    // Thêm phương thức refreshData() để tải lại dữ liệu từ repositories
    fun refreshData() {
        _isDataRefreshed.value = false // Đánh dấu bắt đầu refresh
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val lExpense = expenseRepository.getAllExpense()
                val lCategory = categoryRepository.getAll()
                val lIncome = inComeRepository.getAllIncome()
                
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Refreshing data: ${lExpense.size} expenses, ${lIncome.size} incomes")
                    listExpense = lExpense
                    listCategory = lCategory
                    listIncome = lIncome
                    calculateTotal()
                    
                    // Tự động cập nhật biểu đồ và danh sách với dữ liệu mới
                    val currentMonth = YearMonth.from(date)
                    if (typeReport.value == FragmentReport.CHOOSE_EXPENSE) {
                    prepareDataPieChartExpenseByMonth(currentMonth)
                    } else {
                        filterDataIncomeByMonth(currentMonth)
                    }
                    
                    _isDataRefreshed.value = true // Đánh dấu đã refresh xong
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing data", e)
                withContext(Dispatchers.Main) {
                    _isDataRefreshed.value = false // Đánh dấu refresh thất bại
                }
            }
        }
    }

    // Cải thiện phương thức getExpensesByCategoryAndMonth để truy vấn trực tiếp từ repository
    fun getExpensesByCategoryAndMonth(categoryID: String, yearMonth: YearMonth): List<Expense> {
        // Luôn lấy dữ liệu mới nhất từ listExpense
        val filteredExpenses = listExpense.filter { expense ->
            expense.idCategory == categoryID && expense.getYearMonth() == yearMonth.toString()
        }.toList() // Tạo một list mới để tránh cache
        
        Log.d(TAG, "Found ${filteredExpenses.size} expenses for category $categoryID in month $yearMonth")
        return filteredExpenses
    }

    private fun getCategoryObject(idCategory: String?) = listCategory.first { it.idCategory == idCategory }
    private fun addItemEntryOther(list: MutableList<CategoryExpenseDetail>) : PieEntry? {
        if (list.size > MAX_ITEM_IN_PIE_CHART) {
            var total = 0L
            val lExpenseOther = mutableListOf<Expense>()
            for (i in MAX_ITEM_IN_PIE_CHART until list.size) {
                total += list[i].totalAmount
                lExpenseOther.addAll(list[i].listExpense?: mutableListOf())
            }
            return PieEntry(
                    total.toFloat(),
                    applicationContext.getString(R.string.other),
                    lExpenseOther
            )
        }
        return null
    }
    fun prepareRecyclerViewExpense(yearMonth: YearMonth) {
        val l = mutableListOf<CategoryOverView>()
        for (item in listCategoryExpenseDetailDec) {
            val lExpense = item.listExpense?.filter { expense ->
                yearMonth.toString() == expense.getYearMonth()
            }
            if (lExpense?.size != 0) {
                val lFinancialRecord =
                    lExpense?.map { expense -> expense.toFinancialRecord(item.category) }
                l.add(
                    CategoryOverView(
                        total = item.totalAmount,
                        category = item.category!!,
                        listRecord = lFinancialRecord
                    )
                )
            }
        }
        dataRcv.postValue(l)
    }
    private fun calculateTotal() {
        viewModelScope.launch(Dispatchers.IO) {
            var total = 0L
            for (item in listIncome) {
                item.income?.let { total += it }
            }
            for (item in listExpense) {
                item.expense?.let { total -= it }
            }
            withContext(Dispatchers.Main) {
                Log.d(TAG, "calculateTotal: $total")
                this@ReportViewModel.total.value = total
            }
        }
    }
    companion object {
        const val MAX_ITEM_IN_PIE_CHART = 6
        const val POSITION_ITEM_OTHER = 2
        const val COUNT_ITEM_PIE_CHART = 4
    }
    fun test() {
        TODO()
    }

    // Helper method to get YearMonth from date
    private fun getYearMonth(date: LocalDate): YearMonth = YearMonth.from(date)

    // Get income by category
    private fun getIncomeByCategory(category: Category): List<Income> {
        return listIncome.filter { it.idCategory == category.idCategory }
    }

    // Get income by month
    private fun getIncomeByMonth(month: YearMonth): List<Income> {
        return listIncome.filter { it.getYearMonth() == month.toString() }
    }

    // Filter income data by month
    fun filterDataIncomeByMonth(month: YearMonth) {
        viewModelScope.launch(Dispatchers.IO) {
            val listCategoryIncomeDetailOfMonthDec: MutableList<CategoryIncomeDetail> =
                getIncomeWithCategoryOfMonth(month)
            listCategoryIncomeDetailOfMonthDec.sortByDescending { it.totalAmount }

            val listPieEntry = addItemIncomeEntry(listCategoryIncomeDetailOfMonthDec)
            addItemIncomeEntryOther(listCategoryIncomeDetailOfMonthDec)?.let { pieEntry: PieEntry ->
                listPieEntry.add(POSITION_ITEM_OTHER, pieEntry)
            }
            withContext(Dispatchers.Main) {
                this@ReportViewModel.dataIncomePieChar.value = listPieEntry
                // Prepare RecyclerView data after updating pie chart
                rcvIncomePrepare(month)
            }
        }
    }

    private fun addItemIncomeEntry(listCategoryIncomeDetail: MutableList<CategoryIncomeDetail>): MutableList<PieEntry> {
        val listPieEntry = listCategoryIncomeDetail
            .take(COUNT_ITEM_PIE_CHART + 1)
            .map { incomeDetail: CategoryIncomeDetail -> incomeDetail.toPieEntry() }
            .toMutableList()
        return listPieEntry
    }

    private fun getIncomeWithCategoryOfMonth(month: YearMonth): MutableList<CategoryIncomeDetail> {
        val listIncomeOfMonth = getIncomeByMonth(month)
        return listIncomeOfMonth.groupBy { income: Income -> income.idCategory }.map { entry ->
            val idCategory = entry.key
            val listIncome = entry.value
            CategoryIncomeDetail(
                category = getCategoryObject(idCategory),
                totalAmount = listIncome.sumIncomeMoney(),
                listIncome = listIncome
            )
        }.toMutableList()
    }

    private fun addItemIncomeEntryOther(list: MutableList<CategoryIncomeDetail>): PieEntry? {
        if (list.size > MAX_ITEM_IN_PIE_CHART) {
            var total = 0L
            val lIncomeOther = mutableListOf<Income>()
            for (i in MAX_ITEM_IN_PIE_CHART until list.size) {
                total += list[i].totalAmount
                lIncomeOther.addAll(list[i].listIncome ?: mutableListOf())
            }
            return PieEntry(
                total.toFloat(),
                applicationContext.getString(R.string.other),
                lIncomeOther
            )
        }
        return null
    }

    // Prepare RecyclerView for income data
    fun rcvIncomePrepare(yearMonth: YearMonth) {
        val l = mutableListOf<CategoryOverView>()
        val incomesWithCategory = getIncomeWithCategoryOfMonth(yearMonth)
        
        for (item in incomesWithCategory) {
            val lIncome = item.listIncome?.filter { income: Income ->
                yearMonth.toString() == income.getYearMonth()
            }
            if (!lIncome.isNullOrEmpty()) {
                val lFinancialRecord =
                    lIncome.map { income: Income -> income.toFinancialRecord(item.category) }
                l.add(
                    CategoryOverView(
                        total = item.totalAmount,
                        category = item.category!!,
                        listRecord = lFinancialRecord
                    )
                )
            }
        }
        dataIncomeRcv.postValue(l)
    }

    // Calculate total income for a specific month
    fun calculateTotalIncomeByMonth(month: YearMonth): Long {
        var totalIncome = 0L
        val listIncomeOfMonth = getIncomeByMonth(month)
        for (item in listIncomeOfMonth) {
            item.income?.let { totalIncome += it }
        }
        return totalIncome
    }
}