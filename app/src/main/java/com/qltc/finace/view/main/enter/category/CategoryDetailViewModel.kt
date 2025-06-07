package com.qltc.finace.view.main.enter.category

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.qltc.finace.base.BaseViewModel
import com.qltc.finace.base.TAG
import com.qltc.finace.data.Fb
import com.qltc.finace.data.entity.Category
import com.qltc.finace.data.repository.local.category.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : BaseViewModel() {
    var listCategory : MutableLiveData<MutableList<Category>> = MutableLiveData()

    var nameCategory : String? = null
    fun getCategory(typeCategory: String = Fb.CategoryExpense) {
        viewModelScope.launch(Dispatchers.IO) {
            val it = categoryRepository.getAllCategoryByType(typeCategory = typeCategory)
            withContext(Dispatchers.Main) {
                listCategory.postValue(it)
            }
        }

    }
    fun removeCategory(typeCategory: String,category: Category, callback : () -> Unit) {
        viewModelScope.launch(Dispatchers.IO){
            val it = categoryRepository.removeCategory(category = category, typeCategory = typeCategory)
            withContext(Dispatchers.Main){
                Log.d(TAG, "removeCategory____________________________: $it")
                if(it) {
                    val newList : MutableList<Category>? = listCategory.value
                    newList?.removeIf{it.idCategory == category.idCategory}
                    listCategory.postValue(newList ?: mutableListOf())
                    callback()
                }
            }
        }
    }
}