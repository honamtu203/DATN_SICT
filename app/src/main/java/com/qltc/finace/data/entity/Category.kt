package com.qltc.finace.data.entity

import android.os.Parcelable
import com.qltc.finace.data.Fb
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category (
    var idCategory: String? = null,
    var icon: String? = null,
    var title: String? = null,
    var type : String? = null
)  : Parcelable{
    companion object {
        fun categoryAdded() = Category(icon = Icon.ic_30, title = "Thêm", idCategory = Fb.ItemAddedCategory)
    }

}

fun categoryExpense() = arrayListOf(
    Category(title = "Ăn uống", type = Fb.CategoryExpense, icon = Icon.ic_26),
    Category(title = "Quần áo", type = Fb.CategoryExpense, icon = Icon.ic_13),
    Category(title = "Mỹ Phẩm", type = Fb.CategoryExpense, icon = Icon.ic_4),
    Category(title = "Tiêu hàng ngày", type = Fb.CategoryExpense, icon = Icon.ic_23),
    Category(title = "Phí giao lưu", type = Fb.CategoryExpense, icon = Icon.ic_15),
    Category(title = "Y tế", type = Fb.CategoryExpense, icon = Icon.ic_12),
    Category(title = "Giáo dục", type = Fb.CategoryExpense, icon = Icon.ic_7),
    Category(title = "Tiền nhà", type = Fb.CategoryExpense, icon = Icon.ic_27),
    Category(title = "Tiền xe", type = Fb.CategoryExpense, icon = Icon.ic_17),
)
fun categoryIncome() = arrayListOf(
    Category(title = "Tiền lương", type = Fb.CategoryIncome, icon = Icon.ic_1),
    Category(title = "Tiền thưởng", type = Fb.CategoryIncome, icon = Icon.ic_2),
    Category(title = "Tiền phụ cấp", type = Fb.CategoryIncome, icon = Icon.ic_25),
    Category(title = "Tiền Đầu tư", type = Fb.CategoryIncome, icon = Icon.ic_6),
    Category(title = "Thu nhập khác", type = Fb.CategoryIncome, icon = Icon.ic_32),
)

