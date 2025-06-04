package com.qltc.finace.view.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.qltc.finace.base.BaseViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import com.qltc.finace.data.entity.Category
import com.qltc.finace.data.entity.CategoryOverView
import com.qltc.finace.view.main.calendar.FinancialRecord
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.qltc.finace.data.Resource
import java.time.temporal.TemporalAdjusters

@HiltViewModel
class HomeViewModel @Inject constructor() : BaseViewModel() {
    private val _username = MutableLiveData<String>()
    val username: LiveData<String> = _username

    private val _currentBalance = MutableLiveData<Long>()
    val currentBalance: LiveData<Long> = _currentBalance

    private val _monthlyIncome = MutableLiveData<Long>()
    val monthlyIncome: LiveData<Long> = _monthlyIncome

    private val _monthlyExpense = MutableLiveData<Long>()
    val monthlyExpense: LiveData<Long> = _monthlyExpense

    private val _balanceChange = MutableLiveData<Double>()
    val balanceChange: LiveData<Double> = _balanceChange

    private val _isBalanceVisible = MutableLiveData(true)
    val isBalanceVisible: LiveData<Boolean> = _isBalanceVisible

    private val _chartData = MutableLiveData<List<MonthlyData>>()
    val chartData: LiveData<List<MonthlyData>> = _chartData

    private val _selectedTab = MutableLiveData(TAB_INCOME)
    val selectedTab: LiveData<Int> = _selectedTab

    private val _topCategories = MutableLiveData<List<CategoryOverView>>()
    val topCategories: LiveData<List<CategoryOverView>> = _topCategories

    // Budget tracking
    private val _monthlyBudget = MutableLiveData<Long>()
    val monthlyBudget: LiveData<Long> = _monthlyBudget

    private val _budgetProgress = MutableLiveData<Int>()
    val budgetProgress: LiveData<Int> = _budgetProgress

    private val _remainingBudget = MutableLiveData<Long>()
    val remainingBudget: LiveData<Long> = _remainingBudget

    private val _remainingDays = MutableLiveData<Int>()
    val remainingDays: LiveData<Int> = _remainingDays

    private val _recentTransactions = MutableLiveData<List<FinancialRecord>>()
    val recentTransactions: LiveData<List<FinancialRecord>> = _recentTransactions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _dataState = MutableLiveData<Resource<Unit>>()
    val dataState: LiveData<Resource<Unit>> = _dataState

    init {
        loadInitialData()
        calculateRemainingDays()
    }

    private fun calculateRemainingDays() {
        val today = LocalDate.now()
        val lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth())
        _remainingDays.value = lastDayOfMonth.dayOfMonth - today.dayOfMonth + 1
    }

    fun loadInitialData() {
        viewModelScope.launch {
            try {
                _dataState.value = Resource.Loading

                // Check if user is logged in
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    _dataState.value = Resource.Error("Vui lòng đăng nhập để xem thông tin")
                    return@launch
                }

                // Load all data concurrently
                withContext(Dispatchers.IO) {
                    launch { getCurrentUsername() }
                    launch { getCurrentBalance() }
                    launch { getMonthlyTransactions() }
                    launch { getChartData() }
                    launch { getTopCategories() }
                    launch { getRecentTransactions() }
                    launch { getMonthlyBudget() }
                }

                _dataState.value = Resource.Success(Unit)
            } catch (e: Exception) {
                _dataState.value = Resource.Error("Đã xảy ra lỗi: ${e.message}")
            }
        }
    }

    private suspend fun getCurrentUsername() {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            _username.postValue(currentUser?.displayName ?: "Người dùng")
        } catch (e: Exception) {
            _username.postValue("Người dùng")
        }
    }

    private suspend fun getCurrentBalance() {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val document = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                _currentBalance.postValue(document.getLong("balance") ?: 0)
            } else {
                _currentBalance.postValue(0)
            }
        } catch (e: Exception) {
            _currentBalance.postValue(0)
        }
    }

    private suspend fun getMonthlyTransactions() {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val currentMonth = LocalDate.now().monthValue
            val currentYear = LocalDate.now().year

            val documents = FirebaseFirestore.getInstance()
                .collection("transactions")
                .document(userId)
                .collection("records")
                .whereEqualTo("year", currentYear)
                .whereEqualTo("month", currentMonth)
                .get()
                .await()

            var income = 0L
            var expense = 0L
            
            for (doc in documents) {
                val amount = doc.getLong("amount") ?: 0
                when (doc.getString("type")) {
                    "income" -> income += amount
                    "expense" -> expense += amount
                }
            }
            
            _monthlyIncome.postValue(income)
            _monthlyExpense.postValue(expense)
            calculateBalanceChange(currentMonth, currentYear)
            
            // Update budget progress after getting monthly expense
            getMonthlyBudget()
        } catch (e: Exception) {
            _monthlyIncome.postValue(0)
            _monthlyExpense.postValue(0)
        }
    }

    private suspend fun calculateBalanceChange(currentMonth: Int, currentYear: Int) {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val previousMonth = if (currentMonth == 1) 12 else currentMonth - 1
            val previousYear = if (currentMonth == 1) currentYear - 1 else currentYear

            val documents = FirebaseFirestore.getInstance()
                .collection("transactions")
                .document(userId)
                .collection("records")
                .whereEqualTo("year", previousYear)
                .whereEqualTo("month", previousMonth)
                .get()
                .await()

            var previousIncome = 0L
            var previousExpense = 0L
            
            for (doc in documents) {
                val amount = doc.getLong("amount") ?: 0
                when (doc.getString("type")) {
                    "income" -> previousIncome += amount
                    "expense" -> previousExpense += amount
                }
            }

            val previousBalance = previousIncome - previousExpense
            val currentBalance = _monthlyIncome.value!! - _monthlyExpense.value!!
            
            val change = if (previousBalance != 0L) {
                ((currentBalance - previousBalance).toDouble() / previousBalance.toDouble()) * 100
            } else {
                0.0
            }
            _balanceChange.postValue(change)
        } catch (e: Exception) {
            _balanceChange.postValue(0.0)
        }
    }

    fun toggleBalanceVisibility() {
        _isBalanceVisible.value = !(_isBalanceVisible.value ?: true)
    }

    private suspend fun getChartData() {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val currentDate = LocalDate.now()
            val monthlyDataList = mutableListOf<MonthlyData>()

            // Get last 6 months data
            for (i in 5 downTo 0) {
                val targetDate = currentDate.minusMonths(i.toLong())
                val documents = FirebaseFirestore.getInstance()
                    .collection("transactions")
                    .document(userId)
                    .collection("records")
                    .whereEqualTo("year", targetDate.year)
                    .whereEqualTo("month", targetDate.monthValue)
                    .get()
                    .await()

                var income = 0L
                var expense = 0L
                
                for (doc in documents) {
                    val amount = doc.getLong("amount") ?: 0
                    when (doc.getString("type")) {
                        "income" -> income += amount
                        "expense" -> expense += amount
                    }
                }
                
                monthlyDataList.add(MonthlyData(targetDate.monthValue, targetDate.year, income, expense))
            }
            
            _chartData.postValue(monthlyDataList)
        } catch (e: Exception) {
            _chartData.postValue(emptyList())
        }
    }

    fun onTabSelected(position: Int) {
        _selectedTab.value = position
        viewModelScope.launch {
            getTopCategories()
        }
    }

    private suspend fun getTopCategories() {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val currentMonth = YearMonth.now()

            // Get categories
            val categoryDocs = FirebaseFirestore.getInstance()
                .collection("categories")
                .get()
                .await()

            val categories = categoryDocs.mapNotNull { doc ->
                try {
                    Category(
                        idCategory = doc.id,
                        title = doc.getString("title") ?: return@mapNotNull null,
                        icon = doc.getString("icon") ?: return@mapNotNull null
                    )
                } catch (e: Exception) {
                    null
                }
            }

            // Get transactions
            val transactionDocs = FirebaseFirestore.getInstance()
                .collection("transactions")
                .document(userId)
                .collection("records")
                .whereEqualTo("year", currentMonth.year)
                .whereEqualTo("month", currentMonth.monthValue)
                .get()
                .await()

            // Process transactions
            val categoryTotals = mutableMapOf<String, Long>()
            val categoryTransactions = mutableMapOf<String, MutableList<FinancialRecord>>()

            transactionDocs.forEach { doc ->
                try {
                    val amount = doc.getLong("amount") ?: 0
                    val type = doc.getString("type") ?: return@forEach
                    val categoryId = doc.getString("categoryId") ?: return@forEach
                    
                    // Only process transactions matching current tab
                    if ((selectedTab.value == TAB_INCOME && type == "income") ||
                        (selectedTab.value == TAB_EXPENSE && type == "expense")) {
                        
                        categoryTotals[categoryId] = (categoryTotals[categoryId] ?: 0) + amount
                        
                        val category = categories.find { it.idCategory == categoryId } ?: return@forEach
                        val record = FinancialRecord(
                            id = doc.id,
                            idCategory = categoryId,
                            idUser = userId,
                            date = doc.getString("date") ?: return@forEach,
                            typeExpenseOrIncome = if (type == "income") 
                                FinancialRecord.TYPE_INCOME else FinancialRecord.TYPE_EXPENSE,
                            money = amount,
                            icon = category.icon,
                            noteExpenseIncome = doc.getString("note") ?: "",
                            titleCategory = category.title
                        )
                        
                        categoryTransactions.getOrPut(categoryId) { mutableListOf() }.add(record)
                    }
                } catch (e: Exception) {
                    // Skip invalid transactions
                }
            }

            // Create and sort CategoryOverView list
            val categoryOverviews = categories
                .filter { categoryTotals[it.idCategory] != null }
                .map { category ->
                    CategoryOverView(
                        total = categoryTotals[category.idCategory] ?: 0,
                        category = category,
                        listRecord = categoryTransactions[category.idCategory]
                    )
                }
                .sortedByDescending { it.total }
                .take(5)

            _topCategories.postValue(categoryOverviews)
        } catch (e: Exception) {
            _topCategories.postValue(emptyList())
        }
    }

    private suspend fun getRecentTransactions() {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val currentMonth = YearMonth.now()

            // Get categories for mapping
            val categoryDocs = FirebaseFirestore.getInstance()
                .collection("categories")
                .get()
                .await()

            val categories = categoryDocs.mapNotNull { doc ->
                try {
                    Category(
                        idCategory = doc.id,
                        title = doc.getString("title") ?: return@mapNotNull null,
                        icon = doc.getString("icon") ?: return@mapNotNull null
                    )
                } catch (e: Exception) {
                    null
                }
            }.associateBy { it.idCategory ?: "unknown" }

            // Get recent transactions
            val transactionDocs = FirebaseFirestore.getInstance()
                .collection("transactions")
                .document(userId)
                .collection("records")
                .whereEqualTo("year", currentMonth.year)
                .whereEqualTo("month", currentMonth.monthValue)
                .get()
                .await()

            val transactions = transactionDocs.mapNotNull { doc ->
                try {
                    val categoryId = doc.getString("categoryId") ?: return@mapNotNull null
                    val category = categories[categoryId] ?: return@mapNotNull null
                    
                    FinancialRecord(
                        id = doc.id,
                        idCategory = categoryId,
                        idUser = userId,
                        date = doc.getString("date") ?: return@mapNotNull null,
                        typeExpenseOrIncome = if (doc.getString("type") == "income") 
                            FinancialRecord.TYPE_INCOME else FinancialRecord.TYPE_EXPENSE,
                        money = doc.getLong("amount") ?: 0,
                        icon = category.icon,
                        noteExpenseIncome = doc.getString("note") ?: "",
                        titleCategory = category.title
                    )
                } catch (e: Exception) {
                    null
                }
            }
            .sortedByDescending { it.date }
            .take(5)

            _recentTransactions.postValue(transactions)
        } catch (e: Exception) {
            _recentTransactions.postValue(emptyList())
        }
    }

    private suspend fun getMonthlyBudget() {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val currentMonth = YearMonth.now()
            
            val document = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("budgets")
                .document("${currentMonth.year}_${currentMonth.monthValue}")
                .get()
                .await()

            val budget = document.getLong("amount") ?: 0L
            _monthlyBudget.postValue(budget)

            // Calculate remaining budget and progress
            val spent = _monthlyExpense.value ?: 0L
            _remainingBudget.postValue(if (budget > spent) budget - spent else 0)
            
            if (budget > 0) {
                val progress = ((spent.toDouble() / budget.toDouble()) * 100).toInt()
                _budgetProgress.postValue(progress.coerceIn(0, 100))
            } else {
                _budgetProgress.postValue(0)
            }
        } catch (e: Exception) {
            _monthlyBudget.postValue(0)
            _remainingBudget.postValue(0)
            _budgetProgress.postValue(0)
        }
    }

    fun retryLoading() {
        loadInitialData()
    }

    companion object {
        const val TAB_INCOME = 0
        const val TAB_EXPENSE = 1
    }
}

data class MonthlyData(
    val month: Int,
    val year: Int,
    val income: Long,
    val expense: Long
) 