package com.qltc.finace.data.repository.local.expense

import com.qltc.finace.data.entity.Category
import com.qltc.finace.data.entity.Expense

interface ExpenseRepository {
    suspend fun getAllExpense() : MutableList<Expense>
    suspend fun insertExpense(expense: Expense) : Boolean
    suspend fun getExpenseByDay(date: String) : List<Expense>
    suspend fun getExpenseByWeek(week: String) : List<Expense>
    suspend fun getExpenseByMonth(month: String) : List<Expense>
    suspend fun deleteExpense(expense: Expense) : Boolean
    suspend fun updateExpense(expense: Expense) : Boolean
}