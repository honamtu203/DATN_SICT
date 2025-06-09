package com.qltc.finace.view.authentication.forgot_password

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.databinding.FragmentForgotPasswordBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentForgotPassword : BaseFragment<FragmentForgotPasswordBinding, ForgotPasswordViewModel>(), ForgotPasswordListener {
    
    override val viewModel: ForgotPasswordViewModel by viewModels()
    override val layoutID: Int = R.layout.fragment_forgot_password

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.apply {
            listener = this@FragmentForgotPassword
            viewModel = this@FragmentForgotPassword.viewModel
        }
    }

    override fun onBackClick() {
        try {
            // Thoát hoàn toàn khỏi WebView và quay trở về FragmentHome ngay lập tức
            findNavController().popBackStack(R.id.fag_sign_in, false)
        } catch (e: Exception) {
            // Nếu có lỗi, thử phương thức thay thế
            try {
                findNavController().navigate(R.id.fag_sign_in)
            } catch (e2: Exception) {
                // Nếu cả hai phương thức đều thất bại, thử cách cuối cùng
                activity?.supportFragmentManager?.popBackStack()
            }
        }
    }

    override fun onResetPasswordClick() {
        viewModel.sendPasswordResetEmail(
            onSuccess = {
                Toast.makeText(
                    requireContext(),
                    "Đã gửi email đặt lại mật khẩu. Vui lòng kiểm tra hộp thư của bạn.",
                    Toast.LENGTH_LONG
                ).show()
                findNavController().navigateUp()
            },
            onError = { errorMessage ->
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }
        )
    }
} 