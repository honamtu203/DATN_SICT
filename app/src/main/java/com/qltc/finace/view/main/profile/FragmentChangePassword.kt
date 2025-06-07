package com.qltc.finace.view.main.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.databinding.FragmentChangePasswordBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentChangePassword : BaseFragment<FragmentChangePasswordBinding, ChangePasswordViewModel>(), ChangePasswordListener {
    override val layoutID: Int = R.layout.fragment_change_password
    override val viewModel: ChangePasswordViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.apply {
            lifecycleOwner = this@FragmentChangePassword
            listener = this@FragmentChangePassword
            viewModel = this@FragmentChangePassword.viewModel
        }
        
        setupObservers()
    }
    
    private fun setupObservers() {
        // Observe password change permission để enable/disable UI
        viewModel.isPasswordChangeAllowed.observe(viewLifecycleOwner) { isAllowed ->
            setupUIBasedOnPermission(isAllowed)
            
            // Hiển thị thông báo yêu cầu mật khẩu chỉ khi được phép
            if (isAllowed) {
                showPasswordRequirements()
            }
        }
        
        // Observe validation state để enable/disable nút xác nhận
        viewModel.isValidToSubmit.observe(viewLifecycleOwner) { isValid ->
            // Chỉ enable button nếu được phép đổi mật khẩu VÀ validation hợp lệ
            val canSubmit = isValid && (viewModel.isPasswordChangeAllowed.value == true)
            viewBinding.btnConfirm.isEnabled = canSubmit
            viewBinding.btnConfirm.alpha = if (canSubmit) 1.0f else 0.5f
        }
        
        // Observe loading state để disable nút khi đang xử lý
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            val canSubmit = !isLoading && 
                           (viewModel.isValidToSubmit.value == true) && 
                           (viewModel.isPasswordChangeAllowed.value == true)
            
            viewBinding.btnConfirm.isEnabled = canSubmit
            viewBinding.btnConfirm.text = if (isLoading) "Đang xử lý..." else "Xác Nhận"
            
            // Disable các input field khi đang loading (chỉ nếu được phép)
            if (viewModel.isPasswordChangeAllowed.value == true) {
                viewBinding.etOldPassword.isEnabled = !isLoading
                viewBinding.etNewPassword.isEnabled = !isLoading
                viewBinding.etConfirmPassword.isEnabled = !isLoading
            }
        }
    }
    
    private fun setupUIBasedOnPermission(isAllowed: Boolean) {
        if (isAllowed) {
            // Được phép thay đổi mật khẩu - enable UI
            enablePasswordChangeUI()
            hideErrorMessage()
        } else {
            // Không được phép - disable UI và hiển thị lỗi
            disablePasswordChangeUI()
            showAuthProviderError()
        }
    }
    
    private fun enablePasswordChangeUI() {
        viewBinding.apply {
            // Enable input fields
            etOldPassword.isEnabled = true
            etNewPassword.isEnabled = true
            etConfirmPassword.isEnabled = true
            
            // Enable text input layouts
            tilOldPassword.isEnabled = true
            tilNewPassword.isEnabled = true
            tilConfirmPassword.isEnabled = true
            
            // Reset alpha
            etOldPassword.alpha = 1.0f
            etNewPassword.alpha = 1.0f
            etConfirmPassword.alpha = 1.0f
            
            // Show password requirements
            tvPasswordHint.visibility = View.VISIBLE
        }
    }
    
    private fun disablePasswordChangeUI() {
        viewBinding.apply {
            // Disable input fields
            etOldPassword.isEnabled = false
            etNewPassword.isEnabled = false
            etConfirmPassword.isEnabled = false
            
            // Disable text input layouts
            tilOldPassword.isEnabled = false
            tilNewPassword.isEnabled = false
            tilConfirmPassword.isEnabled = false
            
            // Set alpha để hiển thị trạng thái disabled
            etOldPassword.alpha = 0.5f
            etNewPassword.alpha = 0.5f
            etConfirmPassword.alpha = 0.5f
            
            // Disable button
            btnConfirm.isEnabled = false
            btnConfirm.alpha = 0.5f
            
            // Hide password requirements
            tvPasswordHint.visibility = View.GONE
        }
    }
    
    private fun showAuthProviderError() {
        val errorMessage = viewModel.getAuthProviderErrorMessage()
        viewBinding.apply {
            tvAuthError.text = errorMessage
            tvAuthError.visibility = View.VISIBLE
        }
    }
    
    private fun hideErrorMessage() {
        viewBinding.tvAuthError.visibility = View.GONE
    }
    
    private fun showPasswordRequirements() {
        Toast.makeText(
            context, 
            viewModel.getPasswordRequirements(), 
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onBackClick() {
        try {
            findNavController().popBackStack()
        } catch (e: Exception) {
            // Fallback nếu popBackStack() gặp lỗi
            try {
                findNavController().popBackStack(R.id.frag_profile, false)
            } catch (e2: Exception) {
                try {
                    // Phương pháp thay thế cuối cùng
                    findNavController().navigate(R.id.frag_profile)
                } catch (e3: Exception) {
                    // Ghi log lỗi
                    e3.printStackTrace()
                }
            }
        }
    }

    override fun onConfirmClick() {
        // Kiểm tra lại lần cuối trước khi thực hiện
        if (viewModel.isPasswordChangeAllowed.value != true) {
            Toast.makeText(context, "Không thể thay đổi mật khẩu với phương thức đăng nhập hiện tại", Toast.LENGTH_SHORT).show()
            return
        }
        
        val oldPass = viewBinding.etOldPassword.text?.toString()?.trim() ?: ""
        val newPass = viewBinding.etNewPassword.text?.toString()?.trim() ?: ""
        
        viewModel.changePassword(
            oldPass = oldPass,
            newPass = newPass,
            onSuccess = {
                Toast.makeText(context, "Thay đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            },
            onError = { errorMessage ->
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                
                // Clear mật khẩu cũ nếu sai
                if (errorMessage.contains("không chính xác")) {
                    viewBinding.etOldPassword.text?.clear()
                    viewBinding.etOldPassword.requestFocus()
                }
            }
        )
    }
} 