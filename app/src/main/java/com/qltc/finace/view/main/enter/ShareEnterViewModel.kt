package com.qltc.finace.view.main.enter

import androidx.lifecycle.viewModelScope
import com.qltc.finace.base.SingleLiveData
import com.qltc.finace.data.Fb
import com.qltc.finace.data.entity.Category
import com.qltc.finace.data.entity.Expense
import com.qltc.finace.data.entity.Income
import com.qltc.finace.data.repository.local.category.CategoryRepository
import com.qltc.finace.data.repository.local.expense.ExpenseRepository
import com.qltc.finace.data.repository.local.income.InComeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ShareEnterViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: InComeRepository
) : BaseEnterViewModel() {

    var isAddExpense = SingleLiveData(false)
    var isAddIncome = SingleLiveData(false)
    fun getCategoryExpense() {
        viewModelScope.launch(Dispatchers.IO) {
            val it = categoryRepository.getAllCategoryByType(Fb.CategoryExpense)
            withContext(Dispatchers.Main) {
                it.add(Category.categoryAdded())
                listCategoryExpense.postValue(it)
            }
        }
    }

    fun getCategoryIncome() {
        viewModelScope.launch(Dispatchers.IO) {
            val it = categoryRepository.getAllCategoryByType(Fb.CategoryIncome)
            withContext(Dispatchers.Main) {
                it.add(Category.categoryAdded())
                listCategoryIncome.postValue(it)
            }
        }
    }

    fun submitExpense() {
        if (isEnableButtonAddExpense.value == true) {
            viewModelScope.launch(Dispatchers.IO) {
                val expense = Expense(
                    idCategory = categoryExpenseSelected?.idCategory,
                    expense = moneyExpense.toLong(),
                    date = dateExpense.toString(),
                    note = noteExpense
                )
                expenseRepository.insertExpense(expense)
                withContext(Dispatchers.Main) {
                    isAddExpense.postValue(true)
                }
            }
        }
    }

    fun submitIncome() {
        if (isEnableButtonAddIncome.value == true) {
            viewModelScope.launch(Dispatchers.IO) {
                val income = Income(
                    idCategory = categoryIncomeSelected?.idCategory,
                    income = moneyIncome.toLong(),
                    date = dateIncome.toString(),
                    note = noteIncome
                )
                incomeRepository.insertIncome(income)
                withContext(Dispatchers.Main) {
                    isAddIncome.postValue(true)
                }
            }
        }
    }
} 