package com.qltc.finace.base


import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import androidx.lifecycle.LiveData
import java.lang.ref.WeakReference

@SuppressLint("StaticFieldLeak")
class KeyboardManager private constructor() : LiveData<KeyboardManager.KeyboardStatus>() {

    companion object {
        private var instance: KeyboardManager? = null
        fun init(activity: Activity): KeyboardManager {
            if (instance == null)
                instance = KeyboardManager(activity)
            return instance!!
        }
    }

    private constructor(activity: Activity) : this() {
        activityWeakReference?.get()?.let {
            return
        }
        if (activityWeakReference==null)
            activityWeakReference = WeakReference(activity)
    }


    private var activityWeakReference: WeakReference<Activity>? = null
    private var rootView: View? = null
    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    fun status(): KeyboardManager? {
        if (instance == null)
            throw IllegalAccessException("Call init with activity reference before accessing status")
        return instance
    }


    private fun addOnGlobalLayoutListener() {
        activityWeakReference?.get()?.let {
            rootView = rootView ?: it.findViewById(android.R.id.content)
            globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {

                val rect = Rect().apply { rootView!!.getWindowVisibleDisplayFrame(this) }

                val screenHeight = rootView!!.height

                // rect.bottom is the position above soft keypad or device button.
                // if keypad is shown, the rect.bottom is smaller than that before.
                val keypadHeight = screenHeight - rect.bottom

                // 0.15 ratio is perhaps enough to determine keypad height.
                if (Math.abs(keypadHeight) > screenHeight * 0.05) {
                    postValue(KeyboardStatus.OPEN)
                } else {
                    postValue(KeyboardStatus.CLOSED)
                }
            }
            rootView!!.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        }
    }

    private fun removeOnGlobalLayoutListener() {
        rootView?.viewTreeObserver?.removeOnGlobalLayoutListener(globalLayoutListener)
        rootView = null
        globalLayoutListener = null
    }

    override fun onActive() {
        super.onActive()
        addOnGlobalLayoutListener()
    }

    override fun onInactive() {
        super.onInactive()
        removeOnGlobalLayoutListener()
    }


    enum class KeyboardStatus {
        OPEN, CLOSED
    }

}