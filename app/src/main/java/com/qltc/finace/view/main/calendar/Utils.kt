package com.qltc.finace.view.main.calendar

import java.time.DayOfWeek
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

fun YearMonth.displayText(short: Boolean = false): String {
    return "${this.month.displayText(short = short)} ${this.year}"
}

fun Month.displayText(short: Boolean = true): String {
    val style = if (short) TextStyle.SHORT else TextStyle.FULL
    return getDisplayName(style, Locale("vi", "VN")).replaceFirstChar {
        if (it.isLowerCase())
            it.titlecase(Locale("vi", "VN"))
        else
            it.toString()
    }
}

fun DayOfWeek.displayText(uppercase: Boolean = false): String {
    return getDisplayName(TextStyle.SHORT, Locale("vi", "VN")).let { value ->
        if (uppercase) value.uppercase(Locale("vi", "VN")) else value
    }
}

