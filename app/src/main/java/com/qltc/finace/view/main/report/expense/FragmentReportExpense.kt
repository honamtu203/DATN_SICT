//package com.qltc.finace.view.main.report.expense
//
//import android.graphics.Color
//import android.graphics.Typeface
//import android.os.Bundle
//import android.util.Log
//import android.view.View
//import androidx.fragment.app.activityViewModels
//import com.github.mikephil.charting.data.Entry
//import com.github.mikephil.charting.data.PieData
//import com.github.mikephil.charting.data.PieDataSet
//import com.github.mikephil.charting.data.PieEntry
//import com.github.mikephil.charting.highlight.Highlight
//import com.github.mikephil.charting.listener.OnChartValueSelectedListener
//import com.qltc.finace.R
//import com.qltc.finace.base.BaseFragment
//import com.qltc.finace.base.TAG
//import com.qltc.finace.databinding.FragmentReportExpenseBinding
//import com.qltc.finace.extension.setPieChartDefault
//import com.qltc.finace.view.adapter.AdapterExpenseIncomeReport
//import com.qltc.finace.view.main.calendar.FinancialRecord
//import com.qltc.finace.view.main.report.chart.PercentFormatter
//import com.qltc.finace.view.main.report.ReportViewModel
//import com.qltc.finace.view.main.report.income.ReportExpenseListener
//import dagger.hilt.android.AndroidEntryPoint
//import java.time.YearMonth
//
//
//@AndroidEntryPoint
//class FragmentReportExpense : BaseFragment<FragmentReportExpenseBinding, ReportViewModel>(),
//    ReportExpenseListener, OnChartValueSelectedListener,AdapterExpenseIncomeReport.OnClickListener {
//    override val layoutID: Int = R.layout.fragment_report_expense
//    override val viewModel : ReportViewModel by activityViewModels()
//    private val valueFormatter by lazy { PercentFormatter() }
//    private val adapterRcv by lazy { AdapterExpenseIncomeReport(this) }
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        viewBinding.lifecycleOwner = this
//        viewBinding.apply {
//
//        }
//        addDataSet()
//        viewModel.dataPieChar.observe(viewLifecycleOwner) {
//            val pieDataSet = PieDataSet(it, "Khoản thu")
//            pieDataSet.apply {
//                sliceSpace = 2f
//                valueTextSize = 12f
//                colors = listOf(
//                    context?.getColor(R.color.orange) ?: Color.GRAY,
//                    context?.getColor(R.color.green_700) ?: Color.GREEN,
//                    Color.RED,
//                    context?.getColor(R.color.color_FF8126) ?: Color.MAGENTA,
//                    Color.YELLOW,
//                    Color.CYAN,
//                    Color.BLACK
//                )
//            }
//            // set đuườn line mô tả biểu đồ
//            pieDataSet.apply {
//                xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
//                yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
//                valueLinePart1Length = 0.8f
//                valueLinePart2Length = 0.8f
//                valueLinePart1OffsetPercentage = 70f
//                valueLineColor = Color.BLACK
//                valueTextSize = 9f
//
//            }
//            pieDataSet.sliceSpace = 0f
//            viewBinding.mChart.minAngleForSlices = 5f
//            val pieData = PieData(pieDataSet)
//
//            pieData.setValueFormatter(valueFormatter)
//            viewBinding.mChart.apply {
//                data = pieData
//                notifyDataSetChanged()
//                refreshDrawableState()
//                setEntryLabelColor(context.getColor(R.color.black60))
//                setEntryLabelTextSize(10f)
//                setEntryLabelTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
//                invalidate()
//            }
//        }
//        viewModel.prepareDataPieChartExpense(YearMonth.from(viewModel.date))
//    }
//
//    override fun onValueSelected(p0: Entry?, p1: Highlight?) {
//        if (p0 != null && p0 is PieEntry) {
//            // Lấy tọa độ x và y của điểm được click
//            val x = p1?.x ?: 0f
//            val y = p1?.y ?: 0f
//            // Hiển thị popup tại điểm được click
//         //   displayPopupWindow(x, y)
//        }
//    }
//
//    override fun onNothingSelected() {
//
//    }
//    private fun addDataSet() {
//        viewBinding.mChart.apply {
//            setPieChartDefault()
//            setOnChartValueSelectedListener(this@FragmentReportExpense)
//        }
//    }
//    override fun onResume() {
//        super.onResume()
//        viewModel.prepareRecyclerViewExpense(YearMonth.from(viewModel.date))
//    }
//    override fun onClickItemEI(item: FinancialRecord) {
//        Log.d(TAG, "onClickItemEI: ${item.noteExpenseIncome}")
//    }
//}