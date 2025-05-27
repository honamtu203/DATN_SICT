package com.qltc.finace.base

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * LiveData triển khai chỉ gửi cập nhật một lần (không lặp lại).
 * Đảm bảo mỗi sự kiện chỉ được xử lý một lần, ngay cả khi Fragment/Activity được tạo lại.
 */
class SingleLiveData<T> : MutableLiveData<T> {

    private val pending = AtomicBoolean(false)
    
    // Constructor mặc định không tham số
    constructor() : super()
    
    // Constructor có tham số
    constructor(value: T?) : super() {
        this.value = value
    }

    @MainThread
    override fun setValue(value: T?) {
        pending.set(true)
        super.setValue(value)
    }

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (hasActiveObservers()) {
            Log.w(
                SingleLiveData::class.java.simpleName,
                "Multiple observers registered but only one will be notified of changes."
            )
        }
        
        super.observe(owner) { value ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(value)
            }
        }
    }
    
    /**
     * Tiện ích để gửi sự kiện null
     */
    fun call() {
        value = null
    }
}
