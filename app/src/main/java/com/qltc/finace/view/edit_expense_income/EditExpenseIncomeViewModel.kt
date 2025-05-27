package com.qltc.finace.view.edit_expense_income

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.qltc.finace.R
import com.qltc.finace.base.BaseViewModel
import com.qltc.finace.data.entity.Category
import com.qltc.finace.data.entity.Expense
import com.qltc.finace.data.entity.Income
import com.qltc.finace.data.repository.local.expense.ExpenseRepository
import com.qltc.finace.data.repository.local.income.InComeRepository
import com.qltc.finace.extension.isNotNullAndNotEmpty
import com.qltc.finace.view.main.calendar.FinancialRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class EditExpenseIncomeViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: InComeRepository
) : BaseViewModel() {
    var itemData : FinancialRecord? = null
    var isEnableButtonAdd = MutableLiveData(false)
    var listCategory : MutableList<Category>? = null
    var itemCategorySelected = -1
    var money = ""
        set(value) {
            field = value
            validData()
            val a = intArrayOf()
        }
    var date = LocalDate.now()
        set(value) {
            field = value
            validData()
        }
    var note = ""
        set(value) {
            field = value
            validData()
        }
    var categorySelected : Category? = null
        set(value) {
            field = value
            validData()
        }

    private fun validData() {
        try {
            val numberIncome = money.toLong()
            if (categorySelected != null
                && money.isNotNullAndNotEmpty()
                && numberIncome > 0
                && (itemData?.noteExpenseIncome != note
                        || itemData?.date.toString() != date.toString()
                        || itemData?.money != money.toLong()
                        || itemData?.idCategory != categorySelected?.idCategory)
            ) {
                isEnableButtonAdd.postValue(true)
                return
            }
        }
        catch (_ : Exception) {}
        isEnableButtonAdd.postValue(false)
    }
    fun updateItemData(typeUpdate: Int, callBack : (String) -> Unit) {
        if (typeUpdate == FinancialRecord.TYPE_EXPENSE)
            updateExpense(callBack)
        else
            updateIncome(callBack)
    }
    private fun updateExpense(callBackIsUpdate : (String) -> Unit) {
        val item = Expense(
            idExpense = this.itemData?.id,
            idUser = this.itemData?.idUser,
            expense = money.toLong(),
            date = date.toString(),
            note = note,
            idCategory = categorySelected?.idCategory
        )
        viewModelScope.launch(Dispatchers.IO) {
            val isU = expenseRepository.updateExpense(item)
            withContext(Dispatchers.Main) {
                if (isU) {
                    callBackIsUpdate.invoke(context.getString(R.string.message_update_expense))
                }
                else {
                    callBackIsUpdate.invoke(context.getString(R.string.err_update_income))
                }
            }
        }
    }
    private fun updateIncome(callBackIsUpdate: (String) -> Unit) {
        val item = Income(
            idIncome = this.itemData?.id,
            idUser = this.itemData?.idUser,
            income = money.toLong(),
            date = date.toString(),
            note = note,
            idCategory = categorySelected?.idCategory
        )
        viewModelScope.launch(Dispatchers.IO) {
            val isU = incomeRepository.updateIncome(item)
            withContext(Dispatchers.Main) {
                if (isU) {
                    callBackIsUpdate.invoke(context.getString(R.string.message_update_income))
                }
                else {
                    callBackIsUpdate.invoke(context.getString(R.string.err_update_income))
                }
            }
        }
    }
}