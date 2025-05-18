package com.qltc.finace.view.main.export

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.NumberPicker
import com.qltc.finace.R
import java.util.Calendar

/**
 * Dialog for selecting month and year
 */
class MonthYearPickerDialog(
    context: Context,
    private val initialYear: Int,
    private val initialMonth: Int, // 0-indexed (0-11)
    private val onDateSetListener: (year: Int, month: Int) -> Unit // month is 0-indexed
) : AlertDialog(context) {

    private lateinit var monthPicker: NumberPicker
    private lateinit var yearPicker: NumberPicker
    
    // Define min and max years
    private val minYear = 2015
    private val maxYear = Calendar.getInstance().get(Calendar.YEAR) + 1
    
    // Create array of month names
    private val monthNames = arrayOf(
        "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", 
        "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8",
        "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_month_year_picker, null)
        setView(dialogView)
        
        // Initialize pickers
        monthPicker = dialogView.findViewById(R.id.monthPicker)
        yearPicker = dialogView.findViewById(R.id.yearPicker)
        
        // Setup month picker
        monthPicker.minValue = 0
        monthPicker.maxValue = 11
        monthPicker.displayedValues = monthNames
        monthPicker.value = initialMonth
        monthPicker.wrapSelectorWheel = false
        
        // Setup year picker
        yearPicker.minValue = minYear
        yearPicker.maxValue = maxYear
        yearPicker.value = initialYear
        yearPicker.wrapSelectorWheel = false
        
        // Add buttons
        setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok)) { _, _ ->
            onDateSetListener.invoke(yearPicker.value, monthPicker.value)
        }
        
        setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }
        
        setTitle("Chọn tháng")
    }
} 