package com.qltc.finace.view.authentication.otp

import androidx.lifecycle.viewModelScope
import com.qltc.finace.base.BaseViewModel
import com.qltc.finace.data.repository.local.category.CategoryRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel cho màn hình OTP, không sử dụng Hilt mà dùng Factory thủ công để khởi tạo
 */
class OtpViewModel constructor(
    private val categoryRepository: CategoryRepository
) : BaseViewModel() {
    
    /**
     * Thêm danh mục mặc định cho người dùng mới
     * @param isNewUser Có phải người dùng mới đăng ký không
     * @param success Callback khi thêm thành công
     */
    fun insertDefaultCategory(isNewUser: Boolean, success: () -> Unit) {
        if (!isNewUser) {
            success()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            categoryRepository.importCategoryDefault()
            withContext(Dispatchers.Main) {
                success()
            }
        }
    }
}