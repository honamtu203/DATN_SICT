package com.qltc.finace.data.entity

import android.os.Parcelable
import com.qltc.finace.extension.toLocalDate
import com.qltc.finace.extension.toMonthYearString
import kotlinx.parcelize.Parcelize

@Parcelize
data class Income(
    var idIncome: String? = null,
    var idCategory: String? = null,
    var idUser: String? = null,
    var date: String? = null,
    var income: Long? = null,
    var note: String? = null
) : BaseDataEI(
    id = idIncome,
    idCate = idCategory,
    uuidUser = idUser,
    dateEI = date,
    money = income,
    noteEI = note
), Parcelable {
    fun getYearMonth() = date.toLocalDate().toMonthYearString()
    fun getObjectCategory(list: List<Category>) = list.find { it.idCategory == idCategory }
}
