package com.qltc.finace.data.repository.local.expense

import android.util.Log
import com.qltc.finace.data.Fb
import com.qltc.finace.data.mapperExpense
import com.qltc.finace.data.entity.Expense
import com.qltc.finace.extension.toMonthYearString
import com.qltc.finace.extension.toLocalDate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.math.log

class ExpenseRepositoryImp @Inject constructor(

): ExpenseRepository {
    private val db: FirebaseFirestore = Firebase.firestore
    private val user by lazy {  FirebaseAuth.getInstance().currentUser}
    override suspend fun getAllExpense(): MutableList<Expense> {
        if (user == null) {
            return mutableListOf()
        }
        val listExpense = mutableListOf<Expense>()
        db.collection(Fb.Expense)
            .whereEqualTo(Fb.CategoryField.idUser, user!!.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                querySnapshot.documents.forEach { document ->
                    document.toObject(Expense::class.java)?.let { expense ->
                        expense.idExpense = document.id
                        listExpense.add(expense)
                    }
                }
            }
            .addOnFailureListener {

            }
            .await()
        return listExpense
    }

    override suspend fun insertExpense(expense: Expense): Boolean {
        if(user == null) {return false }
        var result = false
        expense.idUser = user!!.uid
        db.collection(Fb.Expense)
            .add(expense)
            .addOnCompleteListener{
                result = true
            }
            .addOnFailureListener {
                result = false
            }
            .await()
        return result
    }

    override suspend fun getExpenseByDay(date: String): List<Expense> {
        if (user == null) {
            return mutableListOf()
        }
        val listExpense = mutableListOf<Expense>()
        db.collection(Fb.Expense)
            .whereEqualTo(Fb.CategoryField.idUser, user!!.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (item in querySnapshot.documents) {
                    if (item["date"] == date) {
                        listExpense.add(item.mapperExpense())
                    }
                }
            }
            .addOnFailureListener {}
            .await()
        return listOf()
    }

    override suspend fun getExpenseByWeek(week: String): List<Expense> {
//        if (user == null ) {return listOf() }
//        val listExpense = mutableListOf<Expense>()
//        db.collection(Fb.Expense)
//            .whereEqualTo(Fb.CategoryField.idUser, user!!.uid)
//            .get()
//            .addOnSuccessListener { querySnapshot ->
//                for (item in querySnapshot.documents) {
//                    if (item["week"] == week) {
//                        listExpense.add(item.mapperExpense())
//                    }
//                }
//            }
//            .addOnFailureListener {}
//            .await()
//
//        return  listExpense
        return listOf()
    }

    override suspend fun getExpenseByMonth(month: String): List<Expense> {
        if (user == null) {
            return mutableListOf()
        }
        val listExpense = mutableListOf<Expense>()
        db.collection(Fb.Expense)
            .whereEqualTo(Fb.CategoryField.idUser, user!!.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (doc in querySnapshot.documents) {
                    val item = doc.toObject(Expense::class.java)
                    item?.idExpense = doc.id
                    if (item != null && item.date?.toLocalDate()?.toMonthYearString() == month) {
                        listExpense.add(item)
                    }
                }
            }
            .addOnFailureListener {}
            .await()
        return listExpense
    }

    override suspend fun deleteExpense(expense: Expense): Boolean {
        if (user == null) {return false}
        var result = false
        expense.idExpense?.let {
            db.collection(Fb.Expense)
                .document(it)
                .delete()
                .addOnSuccessListener {
                    result = true
                }
                .addOnFailureListener {
                    result = false
                }
                .await()
                }
        return result
        }

    override suspend fun updateExpense(expense: Expense): Boolean {
        if (user == null) {return false}
        var result = false
        expense.idExpense?.let {
            db.collection(Fb.Expense)
                .document(it)
                .update(
                    mapOf(
                        "idUser" to expense.idUser,
                        "expense" to expense.expense,
                        "date" to expense.date,
                        "idCategory" to expense.idCategory,
                        "note" to expense.note,
                    )
                )
                .addOnSuccessListener {
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