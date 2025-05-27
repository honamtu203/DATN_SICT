package com.qltc.finace.view.main.report

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.qltc.finace.R
import java.time.YearMonth
import java.util.Calendar

class MonthYearPickerDialog(
    context: Context,
    private var selectedMonth: YearMonth,
    private val onMonthYearSelected: (yearMonth: YearMonth) -> Unit
) : Dialog(context) {

    companion object {
        private const val MIN_YEAR = 2015
        private val MAX_YEAR = Calendar.getInstance().get(Calendar.YEAR) + 1
    }

    private lateinit var monthPicker: NumberPicker
    private lateinit var yearPicker: NumberPicker
    private lateinit var btnCancel: Button
    private lateinit var btnConfirm: Button
    private lateinit var tvTitle: TextView

    // Tên tháng để hiển thị trong NumberPicker
    private val monthNames = arrayOf(
        "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", 
        "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8",
        "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_month_year_picker)

        // Initialize views
        monthPicker = findViewById(R.id.monthPicker)
        yearPicker = findViewById(R.id.yearPicker)
        btnCancel = findViewById(R.id.btnCancel)
        btnConfirm = findViewById(R.id.btnConfirm)
        tvTitle = findViewById(R.id.tvTitle)

        setupPickers()
        setupButtons()
    }

    private fun setupPickers() {
        // Thiết lập NumberPicker cho tháng
        monthPicker.apply {
            minValue = 0
            maxValue = 11
            displayedValues = monthNames
            value = selectedMonth.monthValue - 1  // Month is 0-indexed in Picker, 1-indexed in YearMonth
            wrapSelectorWheel = false
            
            // Áp dụng kiểu mẫu
            setDividerColor(ContextCompat.getColor(context, R.color.orange))
            setTextColor(ContextCompat.getColor(context, R.color.black))
        }
        
        // Thiết lập NumberPicker cho năm
        yearPicker.apply {
            minValue = MIN_YEAR
            maxValue = MAX_YEAR
            value = selectedMonth.year
            wrapSelectorWheel = false
            
            // Áp dụng kiểu mẫu
            setDividerColor(ContextCompat.getColor(context, R.color.orange))
            setTextColor(ContextCompat.getColor(context, R.color.black))
        }
    }

    private fun setupButtons() {
        btnCancel.setOnClickListener {
            dismiss()
        }

        btnConfirm.setOnClickListener {
            val month = monthPicker.value + 1  // Convert to 1-indexed month
            val year = yearPicker.value
            val newYearMonth = YearMonth.of(year, month)
            
            onMonthYearSelected(newYearMonth)
            dismiss()
        }
    }

    // Extension functions for NumberPicker styling
    private fun NumberPicker.setDividerColor(color: Int) {
        try {
            val field = NumberPicker::class.java.getDeclaredField("mSelectionDivider")
            field.isAccessible = true
            val colorDrawable = ColorDrawable(color)
            field.set(this, colorDrawable)
        } catch (e: Exception) {
            // Ignore styling errors
        }
    }

    private fun NumberPicker.setTextColor(color: Int) {
        try {
            val count = childCount
            for (i in 0 until count) {
                val child = getChildAt(i)
                if (child is EditText) {
                    child.setTextColor(color)
                    return
                }
            }
        } catch (e: Exception) {
            // Ignore styling errors
        }
    }
} 