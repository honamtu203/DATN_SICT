package com.qltc.finace.data.repository.local.income

import android.util.Log
import com.qltc.finace.data.Fb
import com.qltc.finace.data.entity.Income
import com.qltc.finace.extension.toMonthYearString
import com.qltc.finace.extension.toLocalDate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class InComeRepositoryImp @Inject constructor(

) : InComeRepository{
    private val db: FirebaseFirestore = Firebase.firestore
    private val user by lazy {  FirebaseAuth.getInstance().currentUser}
    override suspend fun getAllIncome():MutableList<Income>{
        if(user == null) {return mutableListOf() }
        val listIncome = mutableListOf<Income>()
        db.collection(Fb.Income)
            .whereEqualTo(Fb.CategoryField.idUser, user!!.uid)
            .get()
            .addOnSuccessListener {querySnapshot ->
                querySnapshot.documents.forEach {document ->
                    document.toObject(Income::class.java)?.let {
                        it.idIncome = document.id
                        listIncome.add(it)
                    }
                }
            }
            .addOnFailureListener {}
            .await()
        return listIncome
    }

    override suspend fun insertIncome(income: Income): Boolean {
        if(user == null) {return false }
        var result = false
        income.idUser = user!!.uid
        db.collection(Fb.Income)
            .add(income)
            .addOnCompleteListener{
                result = true
            }
            .addOnFailureListener {
                result = false
            }
            .await()
        return result
    }

    override suspend fun getIncomeByDate(date: String): List<Income> {
        return listOf()
    }

    override suspend fun getIncomeByMonth(month: String): List<Income> {
        if (user == null) {
            return mutableListOf()
        }
        val listIncome = mutableListOf<Income>()
        db.collection(Fb.Income)
            .whereEqualTo(Fb.CategoryField.idUser, user!!.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (doc in querySnapshot.documents) {
                    val item = doc.toObject(Income::class.java)
                    item?.idIncome = doc.id
                    if (item != null && item.date?.toLocalDate()?.toMonthYearString() == month) {
                        listIncome.add(item)
                    }
                }
            }
            .addOnFailureListener {}
            .await()
        return listIncome
    }

    override suspend fun deleteIncome(income: Income): Boolean {
        if (user == null) { return false}
        var result = false
        income.idIncome?.let {
            db.collection(Fb.Income)
                .document(it)
                .delete()
                .addOnCompleteListener{
                    result = true
                }.addOnFailureListener {result = false}
                .await()
        }
        return result

    }

    override suspend fun updateIncome(income: Income): Boolean {
        if (user == null) { return false}
        var result = false
        income.idIncome?.let {
            db.collection(Fb.Income)
                .document(it)
                .update(mapOf(
                    "idUser" to income.idUser,
                    "idCategory" to income.idCategory,
                    "note" to income.note,
                    "date" to income.date,
                    "income" to income.income
                ))
                .addOnCompleteListener {
                    result = true
                }
                .addOnFailureListener {e ->
                    Log.e("error", "updateExpense: ${e.message}")
                    result = false
                }
                .await()
        }
        return result
    }
}