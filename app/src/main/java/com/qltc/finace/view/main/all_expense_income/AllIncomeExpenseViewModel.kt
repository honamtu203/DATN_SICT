package com.qltc.finace.view.main.all_expense_income

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.qltc.finace.base.BaseViewModel
import com.qltc.finace.data.entity.Category
import com.qltc.finace.data.entity.Expense
import com.qltc.finace.data.entity.Income
import com.qltc.finace.data.repository.local.category.CategoryRepository
import com.qltc.finace.data.repository.local.expense.ExpenseRepository
import com.qltc.finace.data.repository.local.income.InComeRepository
import com.qltc.finace.view.main.calendar.FinancialRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AllIncomeExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: InComeRepository,
    private val categoryRepository: CategoryRepository
) : BaseViewModel() {
    private var listExpense = mutableListOf<Expense>()
    private var listIncome = mutableListOf<Income>()
    var listCategory = mutableListOf<Category>()
    private var mapCategory : Map<String, Category> = mutableMapOf()

    var dataRcv = MutableLiveData(mutableListOf<FinancialRecord>())
    fun getAll() {
        viewModelScope.launch(Dispatchers.IO) {
            val lExpense = expenseRepository.getAllExpense()
            val lIncome = incomeRepository.getAllIncome()
            val lCategory = categoryRepository.getAll()
            withContext(Dispatchers.Main) {
                listExpense.addAll(lExpense)
                listIncome.addAll(lIncome)
                listCategory.addAll(lCategory)
                mapCategory = lCategory.associateBy { it.idCategory ?: "otherCategory" }
                prepareData()
            }
        }
    }
    fun prepareData( ){
        viewModelScope.launch(Dispatchers.IO) {
            val list: MutableList<FinancialRecord> = mutableListOf()
            for (item in listExpense) {
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
            for (item in listIncome) {
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

            withContext(Dispatchers.Main) {
                dataRcv.postValue(list)
            }
        }
    }
}