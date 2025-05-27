package com.qltc.finace.view.main.calendar

import android.os.Parcelable
import com.qltc.finace.data.entity.Category
import com.qltc.finace.data.entity.Expense
import com.qltc.finace.data.entity.Income
import kotlinx.parcelize.Parcelize

@Parcelize
data class FinancialRecord (
    val id : String?,
    val idCategory : String? = null,
    val idUser : String? = null,
    val date : String?,
    val typeExpenseOrIncome: Int,
    val money : Long? = null,
    val icon : String? = null,
    val noteExpenseIncome : String? = null,
    val titleCategory : String? = null
) :Parcelable {
    companion object {
        const val TYPE_EXPENSE = 0
        const val TYPE_INCOME = 1
    }
}

fun Expense.toFinancialRecord(category: Category?) : FinancialRecord {
    return FinancialRecord(
        id = id,
        idCategory = idCategory,
        idUser = idUser,
        date = date,
        typeExpenseOrIncome = FinancialRecord.TYPE_EXPENSE,
        money = money,
        icon = category?.icon ,
        noteExpenseIncome = this.note,
        titleCategory = category?.title,
    )
}
fun Income.toFinancialRecord(category: Category?) : FinancialRecord {
    return FinancialRecord(
        id = id,
        idCategory = idCategory,
        idUser = idUser,
        date = date,
        typeExpenseOrIncome = FinancialRecord.TYPE_INCOME,
        money = money,
        icon = category?.icon ,
        noteExpenseIncome = this.note,
        titleCategory = category?.title
    )
}

