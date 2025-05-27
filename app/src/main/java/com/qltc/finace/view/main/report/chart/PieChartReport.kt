package com.qltc.finace.view.main.report.chart

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.qltc.finace.R
import com.qltc.finace.extension.getListColorDefault
import com.qltc.finace.extension.setPieChartDefault

class PieChartReport @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PieChart(context,attrs,defStyleAttr) {
    val pieDataSet: PieDataSet = PieDataSet(null,"")
    private val valueFormatter by lazy { PercentFormatter() }
    fun setPieChartReportDefault(centerTextLabel: String = "Khoản chi") {
        setPieChartDefault(centerTextLabel)
        setLinePart()
        formatText()
        sliceSpace()
    }
    fun submitList(list: MutableList<PieEntry>) {
        data = createPieData(list)
        refreshDrawableState()
        notifyDataSetChanged()
        invalidate()
    }
    private fun formatText() {
        pieDataSet.apply {
            sliceSpace = 2f
            valueTextSize = 12f
            colors = getListColorDefault()
        }

        setEntryLabelColor(context.getColor(R.color.black60))
        setEntryLabelTextSize(10f)
        setEntryLabelTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
    }
    private fun sliceSpace() {
        pieDataSet.sliceSpace = 0f
    }
    private fun setLinePart() {
        pieDataSet.sliceSpace = 0f
        pieDataSet.apply {
            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            valueLinePart1Length = 0.8f
            valueLinePart2Length = 0.8f
            valueLinePart1OffsetPercentage = 70f
            valueLineColor = Color.BLACK
            valueTextSize = 9f
        }
    }
    private fun createPieData(list: MutableList<PieEntry>) : PieData{
        val pieData = PieData()
        pieDataSet.values = list
        pieData.dataSet = pieDataSet
        pieData.setValueFormatter(valueFormatter)
        return pieData
    }
}
