package com.qltc.finace.utils

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.tabs.TabLayout
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.math.abs

@BindingAdapter("setMoneyBalance")
fun setMoneyBalance(textView: TextView, amount: Long?) {
    amount?.let {
        val formatter = DecimalFormat("#,###")
        textView.text = "${formatter.format(it)} đ"
    }
}

@BindingAdapter("setBalanceVisibility")
fun setBalanceVisibility(textView: TextView, isVisible: Boolean?) {
    if (isVisible == false) {
        textView.text = "••••••"
    }
}

@BindingAdapter("setMoneyIncome")
fun setMoneyIncome(textView: TextView, amount: Long?) {
    amount?.let {
        val formatter = DecimalFormat("#,###")
        textView.text = "${formatter.format(it)} đ"
    }
}

@BindingAdapter("setMoneyExpense")
fun setMoneyExpense(textView: TextView, amount: Long?) {
    amount?.let {
        val formatter = DecimalFormat("#,###")
        textView.text = "${formatter.format(it)} đ"
    }
}

@BindingAdapter("setBalanceChange")
fun setBalanceChange(textView: TextView, change: Double?) {
    change?.let {
        val prefix = if (it >= 0) "+" else "-"
        textView.text = "$prefix${String.format("%.1f", abs(it))}% so với tháng trước"
    }
} 