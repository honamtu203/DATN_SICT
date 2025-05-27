package com.qltc.finace.data.repository.local.income

import com.qltc.finace.data.entity.Income

interface InComeRepository {
    suspend fun getAllIncome() : MutableList<Income>
    suspend fun insertIncome(income: Income) : Boolean
    suspend fun getIncomeByDate(date : String): List<Income>
    suspend fun getIncomeByMonth(month : String) : List<Income>
    suspend fun deleteIncome(income: Income) : Boolean
    suspend fun updateIncome(income: Income) : Boolean
}