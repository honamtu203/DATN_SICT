package com.qltc.finace.data.entity

import com.github.mikephil.charting.data.PieEntry

data class CategoryExpenseDetail (
    var category: Category? = null,
    var totalAmount: Long = 0L,
    var listExpense: List<Expense>? = null
)

fun CategoryExpenseDetail.toPieEntry() = PieEntry(totalAmount.toFloat(), category?.title, this)
fun CategoryExpenseDetail.getExpenseOfMonth() ={

}