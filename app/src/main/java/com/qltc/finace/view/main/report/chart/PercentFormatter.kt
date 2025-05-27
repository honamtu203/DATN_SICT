package com.qltc.finace.view.main.report.chart

import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat
import java.util.Locale

class PercentFormatter() : ValueFormatter() {
    private val format = DecimalFormat("###,##0.0")
    private var pieChart: PieChart? = null
    
    init {
        format.setDecimalFormatSymbols(DecimalFormat().decimalFormatSymbols.apply {
            decimalSeparator = ','
            groupingSeparator = '.'
        })
    }
    
    constructor(pieChart: PieChart) : this() {
        this.pieChart = pieChart
    }
    
    override fun getFormattedValue(value: Float): String {
        return if (pieChart != null && pieChart!!.data != null) {
            // Calculate percentage properly if we have the chart
            val total = pieChart!!.data.yValueSum
            val percent = if (total > 0) value / total * 100f else 0f
            format.format(percent) + "%"
        } else {
            // Use value directly if it's already a percentage
            format.format(value) + "%"
        }
    }
    
    override fun getPieLabel(value: Float, pieEntry: PieEntry): String {
        // For pie chart labels, always calculate percentage
        return if (pieChart != null && pieChart!!.data != null) {
            val total = pieChart!!.data.yValueSum
            val percent = if (total > 0) value / total * 100f else 0f
            format.format(percent) + "%"
        } else {
            format.format(value) + "%"
        }
    }
}