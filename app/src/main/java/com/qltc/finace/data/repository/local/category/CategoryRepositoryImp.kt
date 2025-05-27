package com.qltc.finace.data.repository.local.category

import android.util.Log
import com.qltc.finace.base.TAG
import com.qltc.finace.data.Fb
import com.qltc.finace.data.entity.Category
import com.qltc.finace.data.entity.categoryExpense
import com.qltc.finace.data.entity.categoryIncome
import com.qltc.finace.data.mapperCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CategoryRepositoryImp @Inject constructor(

) : CategoryRepository {

    private val db: FirebaseFirestore = Firebase.firestore
    private val user by lazy {  FirebaseAuth.getInstance().currentUser}
    override suspend fun getAllCategoryByType(typeCategory: String): MutableList<Category> {
        if (user == null) {return  mutableListOf() }

        val listCate = mutableListOf<Category>()
        db.collection(Fb.User)
            .document(user!!.uid)
            .collection(typeCategory)
            .get()
            .addOnSuccessListener { querySnapShot ->
                querySnapShot?.documents?.forEach {
                    listCate.add(it.mapperCategory(typeCategory))
                }
            }
            .addOnFailureListener { ex -> Log.e(TAG, "getAllCategory: $ex") }
            .await()
        return listCate
    }

    override suspend fun getAll(): MutableList<Category> {
        if (user == null) {return  mutableListOf() }

        val listCate = mutableListOf<Category>()
        db.collection(Fb.User)
            .document(user!!.uid)
            .collection(Fb.CategoryIncome)
            .get()
            .addOnSuccessListener { querySnapShot ->
                querySnapShot?.documents?.forEach {
                    listCate.add(it.mapperCategory(Fb.CategoryIncome))
                }
            }
            .addOnFailureListener { ex -> Log.e(TAG, "getAllCategory: $ex") }
            .await()
        db.collection(Fb.User)
            .document(user!!.uid)
            .collection(Fb.CategoryExpense)
            .get()
            .addOnSuccessListener { querySnapShot ->
                querySnapShot?.documents?.forEach {
                    listCate.add(it.mapperCategory(Fb.CategoryExpense))
                }
            }
            .addOnFailureListener { ex -> Log.e(TAG, "getAllCategory: $ex") }
            .await()

        return listCate
    }
//    suspend fun getALlData() : MutableList<Category> {
//        coroutineScope {
//
//        }.await()
//        return runBlocking {
//            var list : MutableList<Category> = mutableListOf()
//            val a = getAllCategoryByType(Fb.CategoryIncome)
//            val b = getAllCategoryByType(Fb.CategoryExpense)
//            list.addAll(a)
//            list.addAll(b)
//            list
//        }
//    }

    override suspend fun addCategory(category: Category, typeCategory: String) {
        if (user == null) return
        db.collection(Fb.User)
            .document(user!!.uid)
            .collection(typeCategory)
            .add(category)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
            }.addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }.await()
    }

    override suspend fun importCategoryDefault() {
        if (user == null) {return }
        val batch = db.batch()
        categoryExpense().forEach{
            val docRef = db.collection(Fb.User)
                            .document(user!!.uid)
                            .collection(Fb.CategoryExpense)
                            .document()
            batch.set(docRef, it);
        }
        categoryIncome().forEach{
            val docRef = db.collection(Fb.User)
                .document(user!!.uid)
                .collection(Fb.CategoryIncome)
                .document()
            batch.set(docRef, it);
        }
        batch.commit().addOnCompleteListener{
            Log.d(TAG, "addOnCompleteListener: ${it.result}")
        }.addOnFailureListener {
            Log.w(TAG, "addOnFailureListener : $it")
        }.await()
    }

    override suspend fun removeCategory(category: Category, typeCategory: String): Boolean {
        if (user == null) return false
        var deferredResult = false
        category.idCategory?.let {
            db.collection(Fb.User).document(user!!.uid)
                .collection(typeCategory)
                .document(it)
                .delete()
                .addOnSuccessListener {
                    Log.d(TAG, "removeCategory: Success ${category.idCategory}")
                    deferredResult = true
                }
                .addOnFailureListener {
                    Log.d(TAG, "removeCategory: Fail ${category.idCategory}")
                    deferredResult = false
                }.await()
        }
        return deferredResult
    }

}
