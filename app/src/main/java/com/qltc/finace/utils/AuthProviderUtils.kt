package com.qltc.finace.utils

import com.google.firebase.auth.FirebaseUser

object AuthProviderUtils {
    
    /**
     * Different types of authentication providers
     */
    enum class AuthProviderType {
        EMAIL_PASSWORD,
        GOOGLE,
        PHONE,
        FACEBOOK,
        UNKNOWN
    }
    
    /**
     * Capabilities for each provider type
     */
    data class AuthProviderCapabilities(
        val canChangeEmail: Boolean,
        val requiresPasswordForEmailChange: Boolean,
        val canChangePassword: Boolean,
        val displayName: String
    )
    
    /**
     * Get the primary authentication provider for a user
     */
    fun getPrimaryAuthProvider(user: FirebaseUser?): AuthProviderType {
        if (user == null) return AuthProviderType.UNKNOWN
        
        val providers = user.providerData.map { it.providerId }
        
        return when {
            providers.contains("google.com") -> AuthProviderType.GOOGLE
            providers.contains("facebook.com") -> AuthProviderType.FACEBOOK
            providers.contains("phone") -> AuthProviderType.PHONE
            providers.contains("password") -> AuthProviderType.EMAIL_PASSWORD
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
                canChangePassword = true,
                displayName = "Email/Mật khẩu"
            )
            AuthProviderType.PHONE -> AuthProviderCapabilities(
                canChangeEmail = true,
                requiresPasswordForEmailChange = false,
                canChangePassword = false,
                displayName = "Số điện thoại"
            )
            AuthProviderType.GOOGLE -> AuthProviderCapabilities(
                canChangeEmail = false,
                requiresPasswordForEmailChange = false,
                canChangePassword = false,
                displayName = "Google"
            )
            AuthProviderType.FACEBOOK -> AuthProviderCapabilities(
                canChangeEmail = false,
                requiresPasswordForEmailChange = false,
                canChangePassword = false,
                displayName = "Facebook"
            )
            AuthProviderType.UNKNOWN -> AuthProviderCapabilities(
                canChangeEmail = false,
                requiresPasswordForEmailChange = false,
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
            else -> 
                "Người dùng đăng nhập bằng ${capabilities.displayName} có thể thay đổi email mà không cần mật khẩu."
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
} 