package com.qltc.finace.view.main.calendar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.qltc.finace.base.BaseViewModel
import com.qltc.finace.base.SingleLiveData
import com.qltc.finace.data.Fb
import com.qltc.finace.data.entity.Category
import com.qltc.finace.data.repository.local.expense.ExpenseRepository
import com.qltc.finace.data.repository.local.income.InComeRepository
import com.qltc.finace.data.entity.Expense
import com.qltc.finace.data.entity.Income
import com.qltc.finace.data.repository.local.category.CategoryRepository
import com.qltc.finace.extension.toMonthYearString
import com.qltc.finace.extension.toLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: InComeRepository,
    private val categoryRepository: CategoryRepository
): BaseViewModel() {
    var date = LocalDate.now();
    var selectedDate: LocalDate? = null
    var incomeTotal = MutableLiveData(0L);
    var expenseTotal = MutableLiveData(0L);
    var listExpense = mutableListOf<Expense>()
    var listIncome = mutableListOf<Income>()
    var listCategory = mutableListOf<Category>()
    var isGetDataByMonth = SingleLiveData(false)

    var mapGroupExpenseToShowDayView  : Map<LocalDate, List<Expense>> = mapOf()
    var mapGroupIncomeToShowDayView : Map<LocalDate,List<Income>> = mapOf()

    var mapCategory : Map<String, Category> = mutableMapOf()
    // list này là tổng hợp các khoản thu, chi của 1 ngày hoặc 1 tháng,
    var listSyntheticByDate = MutableLiveData(mutableListOf<FinancialRecord>())
    fun getDataByDate() {
        viewModelScope.launch(Dispatchers.IO) {
            val lExpense = expenseRepository.getAllExpense()
            val lIncome = incomeRepository.getAllIncome()
            val lCategory = categoryRepository.getAll()
            withContext(Dispatchers.Main) {
                resetData()
                listExpense.addAll(lExpense)
                listIncome.addAll(lIncome)
                listCategory.addAll(lCategory)
                mapCategory = lCategory.associateBy { it.idCategory ?: "otherCategory" }
                mapGroupExpenseToShowDayView = listExpense.groupBy { it.date.toLocalDate() }
                mapGroupIncomeToShowDayView = listIncome.groupBy { it.date.toLocalDate() }
                isGetDataByMonth.postValue(true)
                filterListSyntheticByMonth(YearMonth.now())
                //calculator()
            }
        }
    }
    fun filterListSyntheticByDate(dateSelecting : LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            val list: MutableList<FinancialRecord> = mutableListOf()
            for (item in listExpense) {
                if (dateSelecting.toString() == item.date) {
                    list.add(
                        FinancialRecord(
                            idCategory = item.idCategory,
                            id = item.idExpense,
                            idUser = item.idUser,
                            noteExpenseIncome = item.note,
                            date = item.date,
                            money = item.expense,
                            typeExpenseOrIncome = FinancialRecord.TYPE_EXPENSE,
                            titleCategory = mapCategory[item.idCategory]?.title,
                            icon = mapCategory[item.idCategory]?.icon
                        )
                    )
                }
            }
            for (item in listIncome) {
                if (dateSelecting.toString() == item.date) {
                    list.add(
                        FinancialRecord(
                            idCategory = item.idCategory,
                            id = item.idIncome,
                            idUser = item.idUser,
                            noteExpenseIncome = item.note,
                            date = item.date,
                            money = item.income,
                            typeExpenseOrIncome = FinancialRecord.TYPE_INCOME,
                            titleCategory = mapCategory[item.idCategory]?.title,
                            icon = mapCategory[item.idCategory]?.icon
                        )
                    )
                }
            }
            withContext(Dispatchers.Main) {
                listSyntheticByDate.postValue(list)
            }
        }
    }
    fun filterListSyntheticByMonth(monthSelecting : YearMonth) {
     //   clearDataTotal()
        var incomeTotalByDate = 0L
        var expenseTotalByDate = 0L
        viewModelScope.launch(Dispatchers.IO) {
            val list: MutableList<FinancialRecord> = mutableListOf()
            for (item in listExpense) {
                if (monthSelecting.toString() == item.date.toLocalDate().toMonthYearString()) {
                    list.add(
                        FinancialRecord(
                            idCategory = item.idCategory,
                            id = item.idExpense,
                            idUser = item.idUser,
                            noteExpenseIncome = item.note,
                            date = item.date,
                            money = item.expense,
                            typeExpenseOrIncome = FinancialRecord.TYPE_EXPENSE,
                            titleCategory = mapCategory[item.idCategory]?.title,
                            icon = mapCategory[item.idCategory]?.icon
                        )
                    )
                    item.expense?.let { expenseTotalByDate += it }
                }
            }
            for (item in listIncome) {
                if (monthSelecting.toString() == item.date.toLocalDate().toMonthYearString()) {
                    list.add(
                        FinancialRecord(
                            idCategory = item.idCategory,
                            id = item.idIncome,
                            idUser = item.idUser,
                            noteExpenseIncome = item.note,
                            date = item.date,
                            money = item.income,
                            typeExpenseOrIncome = FinancialRecord.TYPE_INCOME,
                            titleCategory = mapCategory[item.idCategory]?.title,
                            icon = mapCategory[item.idCategory]?.icon
                        )
                    )
                    item.income?.let { incomeTotalByDate += it }
                }
            }
            withContext(Dispatchers.Main) {
                listSyntheticByDate.postValue(list)
                incomeTotal.postValue(incomeTotalByDate)
                expenseTotal.postValue(expenseTotalByDate)
            }
        }
    }
    fun filterListCategory(type : Int) : MutableList<Category>{
        if (type == FinancialRecord.TYPE_EXPENSE) {
            return listCategory.filter{ it.type == Fb.CategoryExpense }.toMutableList()
        }
        else {
            return listCategory.filter{ it.type == Fb.CategoryIncome }.toMutableList()
        }
    }
//    private fun calculator() {
//        for (income in listIncome) {
//            income.income?.let { incomeTotal += it }
//        }
//        for (expense in listExpense)
//            expense.expense?.let { expenseTotal += it }
//        total =  expenseTotal +  incomeTotal
//    }
    fun resetData() {
        incomeTotal.postValue(0L)
        expenseTotal.postValue(0L)
        listExpense.clear()
        listIncome.clear()
        listCategory.clear()
    }
    private fun clearDataTotal () {
        incomeTotal.postValue(0L)
        expenseTotal.postValue(0L)
    }
}