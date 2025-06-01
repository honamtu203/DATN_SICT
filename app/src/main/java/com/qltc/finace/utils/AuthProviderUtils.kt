package com.qltc.finace.utils

import com.google.firebase.auth.FirebaseUser

object AuthProviderUtils {
    
    /**
     * Different types of authentication providers
     */
    enum class AuthProviderType {
        EMAIL_PASSWORD,
        EMAIL_PASSWORD_WITH_PHONE,
        GOOGLE,
        PHONE_ONLY,
        FACEBOOK,
        UNKNOWN
    }
    
    /**
     * Capabilities for each provider type
     */
    data class AuthProviderCapabilities(
        val canChangeEmail: Boolean,
        val requiresPasswordForEmailChange: Boolean,
        val requiresOTPForEmailChange: Boolean,
        val canChangePassword: Boolean,
        val displayName: String
    )
    
    /**
     * Get the primary authentication provider for a user
     * Priority: Email/Password > Google > Facebook > Phone-only
     */
    fun getPrimaryAuthProvider(user: FirebaseUser?): AuthProviderType {
        if (user == null) return AuthProviderType.UNKNOWN
        
        val providers = user.providerData.map { it.providerId }
        
        return when {
            // Check for social providers first (these are exclusive)
            providers.contains("google.com") -> AuthProviderType.GOOGLE
            providers.contains("facebook.com") -> AuthProviderType.FACEBOOK
            
            // Check for email/password combinations
            providers.contains("password") && providers.contains("phone") -> {
                // User signed up with email/password and later linked phone
                AuthProviderType.EMAIL_PASSWORD_WITH_PHONE
            }
            providers.contains("password") -> {
                // Pure email/password user
                AuthProviderType.EMAIL_PASSWORD
            }
            providers.contains("phone") -> {
                // Phone-only user
                AuthProviderType.PHONE_ONLY
            }
            
            else -> AuthProviderType.UNKNOWN
        }
    }
    
    /**
     * Get capabilities for a specific provider type
     */
    fun getProviderCapabilities(providerType: AuthProviderType): AuthProviderCapabilities {
        return when (providerType) {
            AuthProviderType.EMAIL_PASSWORD -> AuthProviderCapabilities(
                canChangeEmail = true,
                requiresPasswordForEmailChange = true,
                requiresOTPForEmailChange = false,
                canChangePassword = true,
                displayName = "Email/Mật khẩu"
            )
            AuthProviderType.EMAIL_PASSWORD_WITH_PHONE -> AuthProviderCapabilities(
                canChangeEmail = true,
                requiresPasswordForEmailChange = true,
                requiresOTPForEmailChange = false,
                canChangePassword = true,
                displayName = "Email/Mật khẩu (đã liên kết SĐT)"
            )
            AuthProviderType.PHONE_ONLY -> AuthProviderCapabilities(
                canChangeEmail = true,
                requiresPasswordForEmailChange = false,
                requiresOTPForEmailChange = true,
                canChangePassword = false,
                displayName = "Số điện thoại"
            )
            AuthProviderType.GOOGLE -> AuthProviderCapabilities(
                canChangeEmail = false,
                requiresPasswordForEmailChange = false,
                requiresOTPForEmailChange = false,
                canChangePassword = false,
                displayName = "Google"
            )
            AuthProviderType.FACEBOOK -> AuthProviderCapabilities(
                canChangeEmail = false,
                requiresPasswordForEmailChange = false,
                requiresOTPForEmailChange = false,
                canChangePassword = false,
                displayName = "Facebook"
            )
            AuthProviderType.UNKNOWN -> AuthProviderCapabilities(
                canChangeEmail = false,
                requiresPasswordForEmailChange = false,
                requiresOTPForEmailChange = false,
                canChangePassword = false,
                displayName = "Không xác định"
            )
        }
    }
    
    /**
     * Get user-friendly description for email change restrictions
     */
    fun getEmailChangeDescription(providerType: AuthProviderType): String {
        val capabilities = getProviderCapabilities(providerType)
        
        return when {
            !capabilities.canChangeEmail -> 
                "Tài khoản ${capabilities.displayName} không thể thay đổi email. Vui lòng liên hệ hỗ trợ hoặc tạo tài khoản mới."
            capabilities.requiresPasswordForEmailChange -> 
                "Để thay đổi email, vui lòng nhập mật khẩu hiện tại để xác thực."
            capabilities.requiresOTPForEmailChange -> 
                "Để thay đổi email, vui lòng xác thực bằng mã OTP gửi tới số điện thoại của bạn."
            else -> 
                "Người dùng đăng nhập bằng ${capabilities.displayName} có thể thay đổi email."
        }
    }
    
    /**
     * Check if user can change email
     */
    fun canUserChangeEmail(user: FirebaseUser?): Boolean {
        val providerType = getPrimaryAuthProvider(user)
        return getProviderCapabilities(providerType).canChangeEmail
    }
    
    /**
     * Check if user needs password for email change
     */
    fun requiresPasswordForEmailChange(user: FirebaseUser?): Boolean {
        val providerType = getPrimaryAuthProvider(user)
        return getProviderCapabilities(providerType).requiresPasswordForEmailChange
    }
    
    /**
     * Check if user needs OTP for email change
     */
    fun requiresOTPForEmailChange(user: FirebaseUser?): Boolean {
        val providerType = getPrimaryAuthProvider(user)
        return getProviderCapabilities(providerType).requiresOTPForEmailChange
    }
    
    /**
     * Get all linked providers for debugging
     */
    fun getLinkedProviders(user: FirebaseUser?): List<String> {
        return user?.providerData?.map { it.providerId } ?: emptyList()
    }
} 