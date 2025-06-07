package com.qltc.finace.view.main.enter.add_category

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.qltc.finace.base.BaseViewModel
import com.qltc.finace.data.entity.Category
import com.qltc.finace.data.entity.Icon
import com.qltc.finace.data.repository.local.category.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddCategoryViewModel @Inject constructor(
 private val categoryRepository: CategoryRepository
) : BaseViewModel(){
    var idItemRcvIconSelect = -1
    var icon : String? = null
        set(value) {
            field = value
            checkValidData()
        }
    fun getListIcon() = Icon.getListIcon()
    var title : String = ""
        set(value) {
            field = value
            checkValidData()
        }
    var isEnableButtonAdd = MutableLiveData(false)

    fun checkValidData() {
        isEnableButtonAdd.value = (title.isNotEmpty() && idItemRcvIconSelect != -1)
    }
    fun addNewCategory(typeCategory : String, callback : () -> Unit) {
        if (icon == null) {return}
        val newCategory = Category (icon = icon,
            type = typeCategory,
            title = this.title
        )
        viewModelScope.launch(Dispatchers.IO) {
            categoryRepository.addCategory(
                category = newCategory,
                typeCategory = typeCategory
            )
            withContext(Dispatchers.Main){
                title = ""
                callback()
            }
        }
    }
}