package com.qltc.finace.view.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.qltc.finace.base.BaseViewModel
import com.qltc.finace.data.entity.Category
import com.qltc.finace.data.entity.Expense
import com.qltc.finace.data.entity.Income
import com.qltc.finace.data.repository.local.category.CategoryRepository
import com.qltc.finace.data.repository.local.expense.ExpenseRepository
import com.qltc.finace.data.repository.local.income.InComeRepository
import com.qltc.finace.extension.toLocalDate
import com.qltc.finace.view.main.calendar.FinancialRecord
import com.qltc.finace.data.entity.CategoryOverView
import com.qltc.finace.data.Fb
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.YearMonth
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: InComeRepository,
    private val categoryRepository: CategoryRepository
) : BaseViewModel() {

    companion object {
        const val TAB_INCOME = 0
        const val TAB_EXPENSE = 1
    }

    var selectedTabIndex: Int = TAB_INCOME
    private var listExpense = mutableListOf<Expense>()
    private var listIncome = mutableListOf<Income>()
    private var listCategory = mutableListOf<Category>()
    private var mapCategory: Map<String, Category> = mutableMapOf()

    private val _isDataRefreshed = MutableLiveData<Boolean>()
    val isDataRefreshed: LiveData<Boolean> = _isDataRefreshed

    private val _currentBalance = MutableLiveData<Long>(0)
    val currentBalance: LiveData<Long> = _currentBalance

    private val _monthlyIncome = MutableLiveData<Long>(0)
    val monthlyIncome: LiveData<Long> = _monthlyIncome

    private val _monthlyExpense = MutableLiveData<Long>(0)
    val monthlyExpense: LiveData<Long> = _monthlyExpense

    private val _recentTransactions = MutableLiveData<List<FinancialRecord>>(emptyList())
    val recentTransactions: LiveData<List<FinancialRecord>> = _recentTransactions

    private val _topExpenseCategories = MutableLiveData<List<CategoryOverView>>(emptyList())
    private val _topIncomeCategories = MutableLiveData<List<CategoryOverView>>(emptyList())
    
    private val _topCategories = MutableLiveData<List<CategoryOverView>>(emptyList())
    val topCategories: LiveData<List<CategoryOverView>> = _topCategories

    private val _isBalanceVisible = MutableLiveData(true)
    val isBalanceVisible: LiveData<Boolean> = _isBalanceVisible

    private val _username = MutableLiveData<String>("")
    val username: LiveData<String> = _username

    private val _balanceChange = MutableLiveData<Double>(0.0)
    val balanceChange: LiveData<Double> = _balanceChange

    private val _budgetProgress = MutableLiveData<Int>(0)
    val budgetProgress: LiveData<Int> = _budgetProgress

    private val _remainingBudget = MutableLiveData<Long>(0)
    val remainingBudget: LiveData<Long> = _remainingBudget

    private val _remainingDays = MutableLiveData<Int>(0)
    val remainingDays: LiveData<Int> = _remainingDays

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val lExpense = expenseRepository.getAllExpense()
            val lIncome = incomeRepository.getAllIncome()
            val lCategory = categoryRepository.getAll()

            withContext(Dispatchers.Main) {
                listExpense = lExpense
                listIncome = lIncome
                listCategory = lCategory
                mapCategory = lCategory.associateBy { it.idCategory ?: "otherCategory" }

                loadUsername()
                updateCurrentMonthData()
                
                // Clear previous category data before recalculating
                _topExpenseCategories.value = emptyList()
                _topIncomeCategories.value = emptyList()
                
                // These methods depend on mapCategory, so they should be called after it's initialized
                updateTopExpenseCategories()
                updateTopIncomeCategories()
                
                updateRecentTransactions()
                calculateTotalBalance()
                calculateBudgetProgress()
                calculateBalanceChange()

                // Restore the selected tab's data
                selectTab(selectedTabIndex)
                
                // Thông báo dữ liệu đã được làm mới
                _isDataRefreshed.value = true
            }
        }
    }

    private fun updateCurrentMonthData() {
        val currentMonth = YearMonth.now()
        var monthlyIncomeTotal = 0L
        var monthlyExpenseTotal = 0L

        // Calculate monthly income
        listIncome.filter { income ->
            YearMonth.from(income.date.toLocalDate()) == currentMonth
        }.forEach { income ->
            income.income?.let { monthlyIncomeTotal += it }
        }

        // Calculate monthly expense
        listExpense.filter { expense ->
            YearMonth.from(expense.date.toLocalDate()) == currentMonth
        }.forEach { expense ->
            expense.expense?.let { monthlyExpenseTotal += it }
        }

        _monthlyIncome.value = monthlyIncomeTotal
        _monthlyExpense.value = monthlyExpenseTotal
    }

    private fun updateRecentTransactions() {
        val allTransactions = mutableListOf<FinancialRecord>()

        // Add expenses
        listExpense.forEach { expense ->
            allTransactions.add(
                FinancialRecord(
                    idCategory = expense.idCategory,
                    id = expense.idExpense,
                    idUser = expense.idUser,
                    noteExpenseIncome = expense.note,
                    date = expense.date,
                    money = expense.expense,
                    typeExpenseOrIncome = FinancialRecord.TYPE_EXPENSE,
                    titleCategory = mapCategory[expense.idCategory]?.title,
                    icon = mapCategory[expense.idCategory]?.icon
                )
            )
        }

        // Add incomes
        listIncome.forEach { income ->
            allTransactions.add(
                FinancialRecord(
                    idCategory = income.idCategory,
                    id = income.idIncome,
                    idUser = income.idUser,
                    noteExpenseIncome = income.note,
                    date = income.date,
                    money = income.income,
                    typeExpenseOrIncome = FinancialRecord.TYPE_INCOME,
                    titleCategory = mapCategory[income.idCategory]?.title,
                    icon = mapCategory[income.idCategory]?.icon
                )
            )
        }

        // Sort by date (newest first) and take top 5
        val recentTransactions = allTransactions
            .sortedByDescending { it.date.toLocalDate() }
            .take(5)

        _recentTransactions.value = recentTransactions
    }

    private fun calculateTotalBalance() {
        var total = 0L
        listIncome.forEach { income ->
            income.income?.let { total += it }
        }
        listExpense.forEach { expense ->
            expense.expense?.let { total -= it }
        }
        _currentBalance.value = total
    }

    private fun calculateBalanceChange() {
        val currentMonth = YearMonth.now()
        val previousMonth = currentMonth.minusMonths(1)

        val incomeCurrentMonth = listIncome.filter { YearMonth.from(it.date.toLocalDate()) == currentMonth }.sumOf { it.income ?: 0 }
        val expenseCurrentMonth = listExpense.filter { YearMonth.from(it.date.toLocalDate()) == currentMonth }.sumOf { it.expense ?: 0 }
        val netCurrentMonth = incomeCurrentMonth - expenseCurrentMonth

        val incomePreviousMonth = listIncome.filter { YearMonth.from(it.date.toLocalDate()) == previousMonth }.sumOf { it.income ?: 0 }
        val expensePreviousMonth = listExpense.filter { YearMonth.from(it.date.toLocalDate()) == previousMonth }.sumOf { it.expense ?: 0 }
        val netPreviousMonth = incomePreviousMonth - expensePreviousMonth

        val change = if (netPreviousMonth != 0L) {
            (netCurrentMonth - netPreviousMonth).toDouble() / netPreviousMonth.toDouble() * 100
        } else if (netCurrentMonth > 0L) {
            100.0
        } else {
            0.0
        }
        _balanceChange.value = change
    }

    private fun calculateBudgetProgress() {
        val totalBudget = _monthlyIncome.value ?: 0L // Use monthly income as budget
        val currentExpense = _monthlyExpense.value ?: 0L

        val remaining = totalBudget - currentExpense
        _remainingBudget.value = if (remaining > 0) remaining else 0

        val progress = if (totalBudget > 0) {
            (remaining.toDouble() / totalBudget.toDouble() * 100).toInt()
        } else {
            0
        }
        _budgetProgress.value = progress.coerceIn(0, 100)

        // Calculate remaining days in the month
        val currentMonth = YearMonth.now()
        val today = java.time.LocalDate.now()
        val daysInMonth = currentMonth.lengthOfMonth()
        val remainingDays = daysInMonth - today.dayOfMonth
        _remainingDays.value = if (remainingDays >= 0) remainingDays else 0
    }

    fun selectTab(position: Int) {
        selectedTabIndex = position
        when (position) {
            TAB_EXPENSE -> _topCategories.value = _topExpenseCategories.value
            else -> _topCategories.value = _topIncomeCategories.value
        }
    }

    fun toggleBalanceVisibility() {
        _isBalanceVisible.value = !(_isBalanceVisible.value ?: true)
    }

    fun refreshData() {
        loadData()
    }

    /**
     * Refreshes only the expense data, optimized for when the expense tab is active
     */
    fun refreshExpenseData() {
        viewModelScope.launch(Dispatchers.IO) {
            val lExpense = expenseRepository.getAllExpense()
            
            withContext(Dispatchers.Main) {
                listExpense = lExpense
                updateTopExpenseCategories()
                updateRecentTransactions()
                calculateTotalBalance()
                calculateBudgetProgress()
                calculateBalanceChange()
                
                // Update the UI with expense categories if expense tab is selected
                if (selectedTabIndex == TAB_EXPENSE) {
                    _topCategories.value = _topExpenseCategories.value
                }
                
                // Thông báo dữ liệu đã được làm mới
                _isDataRefreshed.value = true
            }
        }
    }
    
    /**
     * Refreshes only the income data, optimized for when the income tab is active
     */
    fun refreshIncomeData() {
        viewModelScope.launch(Dispatchers.IO) {
            val lIncome = incomeRepository.getAllIncome()
            
            withContext(Dispatchers.Main) {
                listIncome = lIncome
                updateTopIncomeCategories()
                updateRecentTransactions()
                calculateTotalBalance()
                calculateBudgetProgress()
                calculateBalanceChange()
                
                // Update the UI with income categories if income tab is selected
                if (selectedTabIndex == TAB_INCOME) {
                    _topCategories.value = _topIncomeCategories.value
                }
                
                // Thông báo dữ liệu đã được làm mới
                _isDataRefreshed.value = true
            }
        }
    }

    private fun loadUsername() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        _username.value = currentUser?.displayName ?: currentUser?.email ?: ""
    }

    private fun updateTopExpenseCategories() {
        val currentMonth = YearMonth.now()
        
        // Ensure mapCategory is initialized
        if (mapCategory.isEmpty()) {
            mapCategory = listCategory.associateBy { it.idCategory ?: "otherCategory" }
        }
        
        val topExpenseCategoriesList = listExpense
            .filter { YearMonth.from(it.date.toLocalDate()) == currentMonth }
            .groupBy { it.idCategory }
            .map { (categoryId, expenses) ->
                val totalAmount = expenses.sumOf { it.expense ?: 0L }
                val category = mapCategory[categoryId] 
                    ?: listCategory.find { it.idCategory == categoryId } 
                    ?: Category(idCategory = categoryId, title = "Không rõ", type = Fb.CategoryExpense)
                
                // Create financial records for this category
                val records = expenses.map { expense ->
                    FinancialRecord(
                        idCategory = expense.idCategory,
                        id = expense.idExpense,
                        idUser = expense.idUser,
                        noteExpenseIncome = expense.note,
                        date = expense.date,
                        money = expense.expense,
                        typeExpenseOrIncome = FinancialRecord.TYPE_EXPENSE,
                        titleCategory = category.title,
                        icon = category.icon
                    )
                }
                
                CategoryOverView(
                    category = category,
                    total = totalAmount,
                    listRecord = records // Include records instead of empty list
                )
            }
            .sortedByDescending { it.total }
            .take(3)

        _topExpenseCategories.value = topExpenseCategoriesList
    }

    private fun updateTopIncomeCategories() {
        val currentMonth = YearMonth.now()
        
        // Ensure mapCategory is initialized
        if (mapCategory.isEmpty()) {
            mapCategory = listCategory.associateBy { it.idCategory ?: "otherCategory" }
        }
        
        val topIncomeCategoriesList = listIncome
            .filter { YearMonth.from(it.date.toLocalDate()) == currentMonth }
            .groupBy { it.idCategory }
            .map { (categoryId, incomes) ->
                val totalAmount = incomes.sumOf { it.income ?: 0L }
                val category = mapCategory[categoryId] 
                    ?: listCategory.find { it.idCategory == categoryId } 
                    ?: Category(idCategory = categoryId, title = "Không rõ", type = Fb.CategoryIncome)
                
                // Create financial records for this category
                val records = incomes.map { income ->
                    FinancialRecord(
                        idCategory = income.idCategory,
                        id = income.idIncome,
                        idUser = income.idUser,
                        noteExpenseIncome = income.note,
                        date = income.date,
                        money = income.income,
                        typeExpenseOrIncome = FinancialRecord.TYPE_INCOME,
                        titleCategory = category.title,
                        icon = category.icon
                    )
                }
                
                CategoryOverView(
                    category = category,
                    total = totalAmount,
                    listRecord = records // Include records instead of empty list
                )
            }
            .sortedByDescending { it.total }
            .take(3)

        _topIncomeCategories.value = topIncomeCategoriesList
    }

    /**
     * Lấy tổng chi tiêu trong một tháng cụ thể
     */
    fun getMonthlyExpense(yearMonth: YearMonth): Long {
        return listExpense
            .filter { YearMonth.from(it.date.toLocalDate()) == yearMonth }
            .sumOf { it.expense ?: 0L }
    }
    
    /**
     * Lấy tổng thu nhập trong một tháng cụ thể
     */
    fun getMonthlyIncome(yearMonth: YearMonth): Long {
        return listIncome
            .filter { YearMonth.from(it.date.toLocalDate()) == yearMonth }
            .sumOf { it.income ?: 0L }
    }
} 