package com.qltc.finace.data

import com.qltc.finace.data.entity.Category
import com.qltc.finace.data.entity.Expense
import com.qltc.finace.data.entity.Income
import com.google.firebase.firestore.DocumentSnapshot

fun DocumentSnapshot.mapperCategory(typeCategory: String) : Category {
    return Category(
        idCategory = this.id,
        icon = this["icon"] as? String?,
        title = this["title"] as? String,
        type = this["type"] as? String ?: typeCategory
    )
}
fun DocumentSnapshot.mapperExpense() : Expense {
    return Expense(
        idExpense = this.id,
        idCategory = this["idCategory"] as? String?,
        idUser = this["idUser"] as? String?,
        expense = this["expense"] as? Long?,
        note = this["note"] as? String?,
        date = this["date"] as? String
    )
}
fun DocumentSnapshot.mapperIncome() : Income {
    return Income(
        idIncome = this.id,
        idCategory = this["idCategory"] as? String?,
        idUser = this["idUser"] as? String?,
        income = this["income"] as? Long?,
        date = this["date"] as? String?,
        note = this["note"] as? String?
    )
}