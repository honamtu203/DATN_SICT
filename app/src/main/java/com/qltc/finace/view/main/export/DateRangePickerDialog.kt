package com.qltc.finace.view.main.export

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import com.qltc.finace.R
import java.time.LocalDate

class DateRangePickerDialog(
    context: Context,
    private var startDate: LocalDate,
    private var endDate: LocalDate,
    private val onDateRangeSelected: (startDate: LocalDate, endDate: LocalDate) -> Unit
) : Dialog(context) {

    private lateinit var btnCancel: Button
    private lateinit var btnApply: Button
    private lateinit var startDatePicker: DatePicker
    private lateinit var endDatePicker: DatePicker
    private lateinit var tvError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_date_range_picker)

        // Initialize views
        startDatePicker = findViewById(R.id.datePickerStart)
        endDatePicker = findViewById(R.id.datePickerEnd)
        btnCancel = findViewById(R.id.btnCancel)
        btnApply = findViewById(R.id.btnApply)
        tvError = findViewById(R.id.tvDateError)

        // Set initial dates
        startDatePicker.init(
            startDate.year,
            startDate.monthValue - 1,
            startDate.dayOfMonth
        ) { _, year, month, day ->
            val newStartDate = LocalDate.of(year, month + 1, day)
            if (newStartDate.isAfter(getEndDateFromPicker())) {
                tvError.text = context.getString(R.string.date_range_error)
                tvError.visibility = android.view.View.VISIBLE
                btnApply.isEnabled = false
            } else {
                tvError.visibility = android.view.View.GONE
                btnApply.isEnabled = true
            }
        }

        endDatePicker.init(
            endDate.year,
            endDate.monthValue - 1,
            endDate.dayOfMonth
        ) { _, year, month, day ->
            val newEndDate = LocalDate.of(year, month + 1, day)
            if (getStartDateFromPicker().isAfter(newEndDate)) {
                tvError.text = context.getString(R.string.date_range_error)
                tvError.visibility = android.view.View.VISIBLE
                btnApply.isEnabled = false
            } else {
                tvError.visibility = android.view.View.GONE
                btnApply.isEnabled = true
            }
        }

        // Set button click listeners
        btnCancel.setOnClickListener {
            dismiss()
        }

        btnApply.setOnClickListener {
            val newStartDate = getStartDateFromPicker()
            val newEndDate = getEndDateFromPicker()
            
            if (!newStartDate.isAfter(newEndDate)) {
                onDateRangeSelected(newStartDate, newEndDate)
                dismiss()
            } else {
                tvError.text = context.getString(R.string.date_range_error)
                tvError.visibility = android.view.View.VISIBLE
            }
        }
    }

    private fun getStartDateFromPicker(): LocalDate {
        return LocalDate.of(
            startDatePicker.year,
            startDatePicker.month + 1,
            startDatePicker.dayOfMonth
        )
    }

    private fun getEndDateFromPicker(): LocalDate {
        return LocalDate.of(
            endDatePicker.year,
            endDatePicker.month + 1,
            endDatePicker.dayOfMonth
        )
    }
} 