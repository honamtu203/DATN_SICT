package com.qltc.finace.data.entity

import com.github.mikephil.charting.data.PieEntry

data class CategoryIncomeDetail(
    var category: Category? = null,
    var totalAmount: Long = 0L,
    var listIncome: List<Income>? = null
)

fun CategoryIncomeDetail.toPieEntry() = PieEntry(totalAmount.toFloat(), category?.title, this) 