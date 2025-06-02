package com.qltc.finace.view.main.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.databinding.FragmentChangeInforBinding

class FragmentChangeInfor : BaseFragment<FragmentChangeInforBinding, ChangeInforViewModel>(), ChangeInforListener {
    override val layoutID: Int = R.layout.fragment_change_infor
    override val viewModel: ChangeInforViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.lifecycleOwner = this
        viewBinding.listener = this
        viewBinding.viewModel = viewModel

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Set current user info as hints
        val currentUsername = viewModel.getCurrentUsername()
        val currentPhone = viewModel.getCurrentPhone()
        val currentEmail = viewModel.getCurrentEmail()

        // Set hints with current data or default hints
        viewBinding.etUsername.hint = if (currentUsername.isNotEmpty()) currentUsername else "Tên Người Dùng"
        viewBinding.etPhone.hint = if (currentPhone.isNotEmpty()) currentPhone else "Số Điện Thoại"
        viewBinding.etEmail.hint = if (currentEmail.isNotEmpty()) currentEmail else "Email"

        // Disable email field if user is authenticated with Google
        if (viewModel.isGoogleAuthUser()) {
            viewBinding.etEmail.isEnabled = false
            viewBinding.tilEmail.isEnabled = false
            viewBinding.tilEmail.helperText = "Email Google không thể chỉnh sửa"
            viewBinding.etEmail.alpha = 0.5f
        }

        // Set click listener for save button (in case data binding doesn't work)
        viewBinding.btnSaveChanges.setOnClickListener {
            onSaveChangesClick()
        }
    }

    private fun observeViewModel() {
        // Observe update result
        viewModel.updateResult.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            
            // Navigate back if update was successful
            if (message == "Cập nhật thành công!") {
                findNavController().navigateUp()
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            viewBinding.btnSaveChanges.isEnabled = !isLoading
            // You can add progress bar here if needed
        }

        // Observe current user name for display
        viewModel.currentUserName.observe(viewLifecycleOwner) { name ->
            // This will update the display name automatically through data binding
        }

        // Observe OTP dialog trigger
        viewModel.showOtpDialog.observe(viewLifecycleOwner) { phoneNumber ->
            if (phoneNumber.isNotEmpty()) {
                showOtpDialog(phoneNumber)
            }
        }
        
        // Observe Email dialog trigger
        viewModel.showEmailDialog.observe(viewLifecycleOwner) { email ->
            if (email.isNotEmpty()) {
                showEmailDialog(email)
            }
        }
    }

    private fun showOtpDialog(phoneNumber: String) {
        val username = viewBinding.etUsername.text.toString().trim()
        val email = viewBinding.etEmail.text.toString().trim()
        
        val otpDialog = OtpVerificationDialog(
            context = requireActivity(),
            phoneNumber = phoneNumber,
            onOtpConfirmed = { credential ->
                // OTP verified successfully, update phone along with other fields
                viewModel.updateWithPhoneCredential(credential, username, email)
            },
            onOtpCancelled = {
                // OTP cancelled, but still update username and email if valid
                Toast.makeText(requireContext(), "Xác thực số điện thoại bị hủy", Toast.LENGTH_SHORT).show()
                viewModel.updateWithoutPhone(username, email)
            }
        )
        
        otpDialog.show()
    }

    private fun showEmailDialog(newEmail: String) {
        val username = viewBinding.etUsername.text.toString().trim()
        
        val emailDialog = EmailVerificationDialog(
            context = requireActivity(),
            newEmail = newEmail,
            onEmailConfirmed = { confirmedEmail ->
                // Email verified successfully, update email along with other fields
                viewModel.updateWithEmailCredential(confirmedEmail, username)
            },
            onEmailCancelled = {
                // Email verification cancelled, but still update username if valid
                Toast.makeText(requireContext(), "Xác thực email bị hủy", Toast.LENGTH_SHORT).show()
                val hasUsernameChange = username.isNotEmpty() && username != viewModel.getCurrentUsername()
                if (hasUsernameChange) {
                    viewModel.updateWithoutPhoneAndEmail(username, hasUsernameChange)
                }
            }
        )
        
        emailDialog.show()
    }

    override fun onBackClick() {
        findNavController().navigate(R.id.fag_profile)
    }

    override fun onEditAvatarClick() {
        // TODO: Implement avatar editing functionality
        Toast.makeText(requireContext(), "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
    }

    override fun onSaveChangesClick() {
        // Get data from EditTexts
        val username = viewBinding.etUsername.text.toString().trim()
        val phone = viewBinding.etPhone.text.toString().trim()
        val email = viewBinding.etEmail.text.toString().trim()

        // Get current values for comparison
        val currentUsername = viewModel.getCurrentUsername()
        val currentPhone = viewModel.getCurrentPhone()
        val currentEmail = viewModel.getCurrentEmail()

        // Check if there are any changes
        val hasUsernameChange = username.isNotEmpty() && username != currentUsername
        val hasPhoneChange = phone.isNotEmpty() && phone != currentPhone
        val hasEmailChange = email.isNotEmpty() && email != currentEmail && !viewModel.isGoogleAuthUser()

        if (!hasUsernameChange && !hasPhoneChange && !hasEmailChange) {
            Toast.makeText(requireContext(), "Không có thay đổi nào để lưu", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate phone number format if changed
        if (hasPhoneChange && !phone.startsWith("+84")) {
            // Auto-format phone number if it doesn't have country code
            val formattedPhone = if (phone.startsWith("0")) {
                "+84${phone.substring(1)}"
            } else {
                "+84$phone"
            }
            viewBinding.etPhone.setText(formattedPhone)
            // Use current values if fields are empty (user didn't want to change them)
            val finalUsername = if (username.isNotEmpty()) username else currentUsername
            val finalEmail = if (email.isNotEmpty() && !viewModel.isGoogleAuthUser()) email else currentEmail
            
            // Call ViewModel to update user info (will trigger OTP if phone changed)
            viewModel.updateUserInfo(finalUsername, formattedPhone, finalEmail)
        } else {
            // Use current values if fields are empty (user didn't want to change them)
            val finalUsername = if (username.isNotEmpty()) username else currentUsername
            val finalPhone = if (phone.isNotEmpty()) phone else currentPhone
            val finalEmail = if (email.isNotEmpty() && !viewModel.isGoogleAuthUser()) email else currentEmail
            
            // Call ViewModel to update user info (will trigger OTP if phone changed)
            viewModel.updateUserInfo(finalUsername, finalPhone, finalEmail)
        }
    }

    override fun onDeleteAccountClick() {
        // TODO: Implement delete account logic
        Toast.makeText(requireContext(), "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
    }
} 