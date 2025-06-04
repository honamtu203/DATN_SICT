package com.qltc.finace.data

sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String?, val throwable: Throwable? = null) : Resource<Nothing>()
} 