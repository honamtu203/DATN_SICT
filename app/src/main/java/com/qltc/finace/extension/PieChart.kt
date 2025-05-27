package com.qltc.finace.extension

import android.graphics.Color
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.qltc.finace.R
import com.qltc.finace.view.main.report.chart.PieChartCustomRendederer

fun PieChart.descriptionDefault(): Description {
    val d = Description()
    d.text = ""
    return d
}

fun PieChart.setPieChartDefault(centerTextLabel: String = "Khoản chi") {
    isRotationEnabled = true  // cho phep xoay biểu đồ
    // thiet lap mo tả cho biểu do
    holeRadius = 40f              // bán kính của lỗ trống ở giữa bieiể đồ
    setTransparentCircleAlpha(50)  // đặt độ trong suouốt cho vòng tròn bên ngoài
    centerText = centerTextLabel   // vaăn bản ở giữa biểu đồ
    setCenterTextSize(10f)
    setEntryLabelColor(R.color.black80)
    setDrawEntryLabels(true) // hiển thị nhãn của các muục dữ liệu
    setUsePercentValues(true)
    isRotationEnabled = false
    description = this.descriptionDefault()
    setExtraOffsets(15f, 15f, 15f, 15f)
    renderer = PieChartCustomRendederer(
        this,
        this.animator,
        this.viewPortHandler
    )
    legend.isEnabled = false
}

fun PieChart.getListColorDefault() = listOf(
    context?.getColor(R.color.orange) ?: Color.GRAY,
    context?.getColor(R.color.green_700) ?: Color.GREEN,
    Color.RED,
    context?.getColor(R.color.color_FF8126) ?: Color.MAGENTA,
    Color.YELLOW,
    Color.CYAN,
    Color.BLACK
)