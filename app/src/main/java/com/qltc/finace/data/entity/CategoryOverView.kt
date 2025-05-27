package com.qltc.finace.data.entity

import com.qltc.finace.view.main.calendar.FinancialRecord

data class CategoryOverView(
    var total : Long? = null,
    var category: Category,
    var listRecord: List<FinancialRecord>? = null
)