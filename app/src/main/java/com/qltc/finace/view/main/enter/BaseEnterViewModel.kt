package com.qltc.finace.view.main.enter

import androidx.lifecycle.MutableLiveData
import com.qltc.finace.base.BaseViewModel
import com.qltc.finace.base.SingleLiveData
import com.qltc.finace.data.entity.Category
import com.qltc.finace.extension.isNotNullAndNotEmpty
import java.time.LocalDate

abstract class BaseEnterViewModel : BaseViewModel() {
    var typeCurrentFragment : Int = FragmentEnter.FRAGMENT_EXPENSE
        set(value) {
            field = value
            if (field == FragmentEnter.FRAGMENT_EXPENSE) {
                checkValidDataExpense()
            }
            else {
                checkValidDataIncome()
            }
        }
    var isEnableButtonAddAtToolbar = MutableLiveData(false)

    var isEnableButtonAddExpense = MutableLiveData(false)
    var listCategoryExpense = SingleLiveData<MutableList<Category>>(mutableListOf())
    var itemCategoryExpenseSelected = -1
    var dateExpense: LocalDate = LocalDate.now()
    var categoryExpenseSelected : Category? = null
        set(value) {
            field = value
            checkValidDataExpense()
        }
    var noteExpense : String = ""
        set(value) {
            field = value
            checkValidDataExpense()
        }
    var moneyExpense : String = ""
        set(value) {
            field = value
            checkValidDataExpense()
        }
    fun checkValidDataExpense() {
        try {
            val numberExpense = moneyExpense.toLong()
            if (categoryExpenseSelected != null && noteExpense.isNotNullAndNotEmpty()  && numberExpense > 0) {
                isEnableButtonAddExpense.postValue(true)
                isEnableButtonAddAtToolbar.postValue(true)
                return
            }
        }
        catch (_ : Exception) {}
        isEnableButtonAddExpense.postValue(false)
        isEnableButtonAddAtToolbar.postValue(false)
    }

    var isEnableButtonAddIncome = MutableLiveData(false)
    var listCategoryIncome = SingleLiveData<MutableList<Category>>(mutableListOf())
    var itemCategoryIncomeSelected = -1
    var dateIncome: LocalDate = LocalDate.now()
    var categoryIncomeSelected : Category? = null
        set(value) {
            field = value
            checkValidDataIncome()
        }
    var noteIncome : String = ""
        set(value) {
            field = value
            checkValidDataIncome()
        }
    var moneyIncome : String = ""
        set(value) {
            field = value
            checkValidDataIncome()
        }
    fun checkValidDataIncome() {
        try {
            val numberIncome = moneyIncome.toLong()
            if (categoryIncomeSelected != null && noteIncome.isNotNullAndNotEmpty()  && numberIncome > 0) {
                isEnableButtonAddIncome.postValue(true)
                isEnableButtonAddAtToolbar.postValue(true)
                return
            }
        }
        catch (_ : Exception) {}
        isEnableButtonAddIncome.postValue(false)
        isEnableButtonAddAtToolbar.postValue(false)
    }
} 