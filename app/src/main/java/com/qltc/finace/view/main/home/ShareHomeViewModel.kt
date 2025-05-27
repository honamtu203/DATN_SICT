package com.qltc.finace.view.main.home

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
class ShareHomeViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: InComeRepository
) : BaseHomeViewModel() {

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
        val expense = Expense(
            idCategory = categoryExpenseSelected?.idCategory,
            date = this.dateExpense.toString(),
            note = this.noteExpense.trim(),
            expense = this.moneyExpense.toLong()
        )
        viewModelScope.launch(Dispatchers.IO) {
            val it = expenseRepository.insertExpense(expense)
            withContext(Dispatchers.Main) {
                isAddExpense.value = true
            }
        }
    }
    fun submitIncome() {
        val income = Income(
            idCategory = categoryIncomeSelected?.idCategory,
            date = this.dateIncome.toString(),
            note = this.noteIncome.trim(),
            income = this.moneyIncome.toLong()
        )
        viewModelScope.launch(Dispatchers.IO) {
            val it = incomeRepository.insertIncome(income)
            withContext(Dispatchers.Main) {
                isAddIncome.value = true
            }
        }
    }
}