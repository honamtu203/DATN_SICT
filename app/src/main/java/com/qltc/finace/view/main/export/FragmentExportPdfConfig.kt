package com.qltc.finace.view.main.export

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.databinding.FragmentExportPdfConfigBinding
import com.qltc.finace.utils.PdfExportHelper
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import android.util.Log
import android.widget.NumberPicker
import java.util.Calendar
import android.graphics.drawable.ColorDrawable
import android.widget.EditText
import androidx.core.content.ContextCompat
import java.time.LocalDateTime
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.qltc.finace.data.entity.CategoryExpenseDetail
import com.qltc.finace.data.entity.CategoryIncomeDetail
import com.qltc.finace.extension.getListColorDefault
import com.qltc.finace.extension.setPieChartDefault
import com.qltc.finace.view.main.report.chart.PercentFormatter
import android.graphics.Color
import android.view.ViewGroup
import android.widget.FrameLayout

@AndroidEntryPoint
class FragmentExportPdfConfig : BaseFragment<FragmentExportPdfConfigBinding, ExportPdfViewModel>(), ExportPdfListener {
    
    companion object {
        private const val TAG = "FragmentExportPdfConfig"
        // Giá trị tối thiểu và tối đa cho năm
        private const val MIN_YEAR = 2015
        private val MAX_YEAR = Calendar.getInstance().get(Calendar.YEAR) + 1
    }
    
    override val viewModel: ExportPdfViewModel by viewModels()
    override val layoutID: Int = R.layout.fragment_export_pdf_config
    
    // Duy trì biến để lưu tháng/năm đã chọn
    private var selectedMonth: YearMonth = YearMonth.now()
    
    // Lưu thời điểm hiện tại khi mở fragment
    private lateinit var currentTime: LocalDateTime
    
    // Tên tháng để hiển thị trong NumberPicker
    private val monthNames = arrayOf(
        "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", 
        "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8",
        "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Lưu thời điểm hiện tại
        currentTime = LocalDateTime.now()
        
        viewBinding.apply {
            listener = this@FragmentExportPdfConfig
            viewModel = this@FragmentExportPdfConfig.viewModel
            lifecycleOwner = viewLifecycleOwner
        }
        
        setupInlineMonthYearPickers()
        setupObservers()
        
        // Đặt tên file mặc định
        val defaultFileName = generateDefaultFileName(selectedMonth.atDay(1), currentTime)
        viewBinding.edtFileName.setText(defaultFileName.removeSuffix(".pdf"))
    }
    
    /**
     * Tạo tên file mặc định cho báo cáo PDF
     * 
     * @param startDate Ngày bắt đầu khoảng thời gian báo cáo
     * @param currentTime Thời điểm hiện tại
     * @return Tên file theo định dạng "Báo cáo thu chi_MMYYYY_ddMMyyyyHHmmss.pdf"
     */
    private fun generateDefaultFileName(startDate: LocalDate, currentTime: LocalDateTime): String {
        val datePrefix = startDate.format(DateTimeFormatter.ofPattern("MMyyyy"))
        val timestamp = currentTime.format(DateTimeFormatter.ofPattern("ddMMyyyyHHmmss"))
        
        return "Report_Finance_${datePrefix}_${timestamp}.pdf"
    }
    
    /**
     * Cập nhật tên file mặc định khi thay đổi tháng
     */
    private fun updateDefaultFileName() {
        // Kiểm tra nếu ô tên file đang trống hoặc người dùng đang sử dụng tên file tự động trước đó
        val currentText = viewBinding.edtFileName.text.toString().trim()
        if (currentText.isEmpty() || currentText.startsWith("Report_Finance") || currentText.startsWith("Report_Finance_")) {
            val defaultFileName = generateDefaultFileName(selectedMonth.atDay(1), currentTime)
            viewBinding.edtFileName.setText(defaultFileName.removeSuffix(".pdf"))
        }
    }
    
    private fun setupInlineMonthYearPickers() {
        try {
            // Thiết lập NumberPicker cho tháng
            viewBinding.monthPicker.apply {
                minValue = 0
                maxValue = 11
                displayedValues = monthNames
                value = selectedMonth.monthValue - 1  // Month is 0-indexed in Picker, 1-indexed in YearMonth
                wrapSelectorWheel = false
                
                // Áp dụng kiểu mẫu cho NumberPicker với API level check
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // API 29+ có các phương thức mới
                        setSelectionDividerHeight(2)
                        textColor = ContextCompat.getColor(requireContext(), R.color.black)
                        textSize = resources.getDimensionPixelSize(R.dimen.text_size_normal).toFloat()
                    } else {
                        // Fallback cho API thấp hơn - sử dụng reflection
                setDividerColor(ContextCompat.getColor(requireContext(), R.color.orange))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                setTextSize(resources.getDimensionPixelSize(R.dimen.text_size_normal).toFloat())
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Không thể áp dụng style cho NumberPicker: ${e.message}", e)
                }
            }
            
            // Thiết lập NumberPicker cho năm
            viewBinding.yearPicker.apply {
                minValue = MIN_YEAR
                maxValue = MAX_YEAR
                value = selectedMonth.year
                wrapSelectorWheel = false
                
                // Áp dụng kiểu mẫu cho NumberPicker với API level check
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // API 29+ có các phương thức mới
                        setSelectionDividerHeight(2)
                        textColor = ContextCompat.getColor(requireContext(), R.color.black)
                        textSize = resources.getDimensionPixelSize(R.dimen.text_size_normal).toFloat()
                    } else {
                        // Fallback cho API thấp hơn - sử dụng reflection
                setDividerColor(ContextCompat.getColor(requireContext(), R.color.orange))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                setTextSize(resources.getDimensionPixelSize(R.dimen.text_size_normal).toFloat())
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Không thể áp dụng style cho NumberPicker: ${e.message}", e)
                }
            }
            
            // Đặt listener cho nút xác nhận
            viewBinding.btnSelectDate.setOnClickListener {
                // Lấy giá trị đã chọn từ pickers
                val month = viewBinding.monthPicker.value + 1  // Convert to 1-indexed month
                val year = viewBinding.yearPicker.value
                
                // Cập nhật tháng đã chọn
                selectedMonth = YearMonth.of(year, month)
                Log.d(TAG, "Đã chọn tháng: $selectedMonth")
                
                // Cập nhật tên file mặc định khi thay đổi tháng
                updateDefaultFileName()
                
                // Thông báo cho người dùng
                Toast.makeText(
                    requireContext(),
                    "Đã chọn: Tháng ${month}/${year}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi khi thiết lập bộ chọn tháng/năm: ${e.message}", e)
            Toast.makeText(
                requireContext(), 
                "Không thể khởi tạo bộ chọn tháng. Sử dụng tháng hiện tại.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    // Thêm phương thức extension cho NumberPicker để áp dụng kiểu mẫu dễ dàng hơn
    private fun NumberPicker.setDividerColor(color: Int) {
        try {
            val field = NumberPicker::class.java.getDeclaredField("mSelectionDivider")
            field.isAccessible = true
            val colorDrawable = ColorDrawable(color)
            field.set(this, colorDrawable)
        } catch (e: Exception) {
            Log.e(TAG, "Không thể đặt màu divider: ${e.message}", e)
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
            Log.e(TAG, "Không thể đặt màu chữ: ${e.message}", e)
        }
    }
    
    private fun NumberPicker.setTextSize(size: Float) {
        try {
            val count = childCount
            for (i in 0 until count) {
                val child = getChildAt(i)
                if (child is EditText) {
                    child.textSize = size / resources.displayMetrics.scaledDensity
                    return
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Không thể đặt kích thước chữ: ${e.message}", e)
        }
    }
    
    private fun setupObservers() {
        // Sử dụng viewLifecycleOwner nhất quán
        viewModel.pdfGenerationResult.observe(viewLifecycleOwner) { uri ->
            uri?.let {
                Toast.makeText(
                    requireContext(),
                    "PDF đã được tạo thành công",
                    Toast.LENGTH_SHORT
                ).show()
                
                // Mở và chia sẻ file PDF
                openAndSharePdf(uri)
            }
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            viewBinding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            viewBinding.btnExportPdf.isEnabled = !isLoading
        }
        
        // Observe data readiness
        viewModel.isDataReady.observe(viewLifecycleOwner) { isReady ->
            viewBinding.btnExportPdf.isEnabled = isReady && !viewModel.isLoading.value!!
            if (!isReady) {
                Toast.makeText(requireContext(), "Đang tải dữ liệu...", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun openAndSharePdf(uri: android.net.Uri) {
        // Mở file PDF
        val pdfHelper = PdfExportHelper(requireContext())
        pdfHelper.openPdfFile(uri)
        
        // Thêm tùy chọn chia sẻ
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ báo cáo PDF"))
    }

    override fun onExportPdfClicked() {
        // Validate tên file trước
        if (!validateFileName()) {
            return
        }
        
        // Kiểm tra quyền dựa theo phiên bản Android
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Android 9 trở xuống cần yêu cầu quyền truy cập bộ nhớ
            Dexter.withContext(requireContext())
                .withPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report?.areAllPermissionsGranted() == true) {
                            generatePDF()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Cần cấp quyền để xuất file PDF",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()
                    }
                })
                .check()
        } else {
            // Android 10+ không cần xin quyền
            generatePDF()
        }
    }
    
    private fun validateFileName(): Boolean {
        var fileName = viewBinding.edtFileName.text.toString().trim()
        
        if (fileName.isEmpty()) {
            // Thay vì báo lỗi, sử dụng tên mặc định
            fileName = generateDefaultFileName(selectedMonth.atDay(1), LocalDateTime.now()).removeSuffix(".pdf")
            viewBinding.edtFileName.setText(fileName)
            return true
        }
        
        // Kiểm tra ký tự đặc biệt không hợp lệ thay vì chỉ định danh sách ký tự hợp lệ
        // Danh sách ký tự không hợp lệ điển hình: \/:|<>*?"
        if (fileName.contains(Regex("[\\\\/:*?\"<>|]"))) {
            viewBinding.edtFileName.error = "Tên file không được chứa các ký tự đặc biệt: \\ / : * ? \" < > |"
            viewBinding.edtFileName.requestFocus()
            return false
        }
        
        return true
    }
    
    private fun generatePDF() {
        Log.d(TAG, "🚀 Starting PDF generation...")
        
        // Get configuration options
        val reportType = when (viewBinding.radioGroupDataType.checkedRadioButtonId) {
            R.id.rbExpense -> PdfExportHelper.TYPE_EXPENSE
            R.id.rbIncome -> PdfExportHelper.TYPE_INCOME
            else -> PdfExportHelper.TYPE_BOTH
        }
        
        Log.d(TAG, "📊 Report type: $reportType")
        
        var displayOptions = 0
        if (viewBinding.cbShowCharts.isChecked) {
            displayOptions = displayOptions or PdfExportHelper.OPTION_SHOW_CHARTS
        }
        if (viewBinding.cbShowDetails.isChecked) {
            displayOptions = displayOptions or PdfExportHelper.OPTION_SHOW_DETAILS
        }
        if (viewBinding.cbShowStats.isChecked) {
            displayOptions = displayOptions or PdfExportHelper.OPTION_SHOW_STATISTICS
        }
        
        Log.d(TAG, "📋 Display options: Charts=${viewBinding.cbShowCharts.isChecked}, Details=${viewBinding.cbShowDetails.isChecked}, Stats=${viewBinding.cbShowStats.isChecked}")
        
        // Kiểm tra có ít nhất một option được chọn
        if (displayOptions == 0) {
            Toast.makeText(
                requireContext(),
                "Vui lòng chọn ít nhất một tùy chọn hiển thị (Biểu đồ, Thống kê, hoặc Chi tiết)",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        
        // Lấy tên file từ EditText và đảm bảo có phần mở rộng .pdf
        var fileName = viewBinding.edtFileName.text.toString().trim()
        
        // Nếu tên file trống, sử dụng tên mặc định
        if (fileName.isEmpty()) {
            fileName = generateDefaultFileName(selectedMonth.atDay(1), LocalDateTime.now()).removeSuffix(".pdf")
        }
        
        // Đảm bảo tên file không có phần mở rộng .pdf (sẽ được thêm bởi PdfExportHelper)
        if (fileName.endsWith(".pdf", ignoreCase = true)) {
            fileName = fileName.substring(0, fileName.length - 4)
        }
        
        // Luôn sử dụng giá trị hiện tại từ các pickers (thay vì selectedMonth) để đảm bảo lấy giá trị mới nhất
        val month = viewBinding.monthPicker.value + 1  // chuyển từ 0-index sang 1-index
        val year = viewBinding.yearPicker.value
        val monthYearToExport = YearMonth.of(year, month)
        
        // Tạo biểu đồ theo loại báo cáo nếu hiển thị biểu đồ được chọn
        var expensePieChart: PieChart? = null
        var incomePieChart: PieChart? = null
        
        if (viewBinding.cbShowCharts.isChecked) {
            Log.d(TAG, "📈 Creating charts for month: $monthYearToExport")
            when (reportType) {
                PdfExportHelper.TYPE_EXPENSE -> {
                    Log.d(TAG, "📈 Creating expense chart only...")
                    expensePieChart = createExpensePieChart(monthYearToExport)
                    Log.d(TAG, "📈 Expense chart result: ${if (expensePieChart != null) "Success" else "Null (no data)"}")
                }
                PdfExportHelper.TYPE_INCOME -> {
                    Log.d(TAG, "📈 Creating income chart only...")
                    incomePieChart = createIncomePieChart(monthYearToExport)
                    Log.d(TAG, "📈 Income chart result: ${if (incomePieChart != null) "Success" else "Null (no data)"}")
                }
                PdfExportHelper.TYPE_BOTH -> {
                    Log.d(TAG, "📈 Creating both expense and income charts...")
                    expensePieChart = createExpensePieChart(monthYearToExport)
                    incomePieChart = createIncomePieChart(monthYearToExport)
                    Log.d(TAG, "📈 Charts result - Expense: ${if (expensePieChart != null) "Success" else "Null (no data)"}, Income: ${if (incomePieChart != null) "Success" else "Null (no data)"}")
                }
            }
        } else {
            Log.d(TAG, "📈 Charts not requested, skipping chart creation")
        }
        
        // Generate PDF using YearMonth instead of start/end date
        viewModel.generatePdf(
            context = requireContext(),
            fileName = fileName,
            month = monthYearToExport,
            reportType = reportType,
            displayOptions = displayOptions,
            expensePieChart = expensePieChart,
            incomePieChart = incomePieChart
        )
    }
    
    /**
     * Tạo biểu đồ chi tiêu cho tháng được chọn
     */
    private fun createExpensePieChart(month: YearMonth): PieChart? {
        try {
            val expenseData = viewModel.getExpenseDataForMonth(month)
            
            if (expenseData.isNullOrEmpty()) {
                Log.d(TAG, "No expense data available for chart")
                return null
            }
            
            // Tạo danh sách entries cho biểu đồ
            val pieEntries = mutableListOf<PieEntry>()
            var hasData = false
            
            expenseData.forEach { categoryData ->
                if (categoryData.totalAmount > 0) {
                    pieEntries.add(PieEntry(
                        categoryData.totalAmount.toFloat(),
                        categoryData.category?.title ?: "Không tên"
                    ))
                    hasData = true
                    Log.d(TAG, "Added expense entry: ${categoryData.category?.title} - ${categoryData.totalAmount}")
                }
            }
            
            if (!hasData || pieEntries.isEmpty()) {
                return null
            }
            
            return createFormattedPieChart(pieEntries, "Chi tiêu tháng ${month.monthValue}/${month.year}")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating expense pie chart: ${e.message}", e)
            return null
        }
    }
    
    /**
     * Tạo biểu đồ thu nhập cho tháng được chọn
     */
    private fun createIncomePieChart(month: YearMonth): PieChart? {
        try {
            val incomeData = viewModel.getIncomeDataForMonth(month)
            
            if (incomeData.isNullOrEmpty()) {
                Log.d(TAG, "No income data available for chart")
                return null
            }
            
            // Tạo danh sách entries cho biểu đồ
            val pieEntries = mutableListOf<PieEntry>()
            var hasData = false
            
            incomeData.forEach { categoryData ->
                if (categoryData.totalAmount > 0) {
                    pieEntries.add(PieEntry(
                        categoryData.totalAmount.toFloat(),
                        categoryData.category?.title ?: "Không tên"
                    ))
                    hasData = true
                    Log.d(TAG, "Added income entry: ${categoryData.category?.title} - ${categoryData.totalAmount}")
                }
            }
            
            if (!hasData || pieEntries.isEmpty()) {
                return null
            }
            
            return createFormattedPieChart(pieEntries, "Thu nhập tháng ${month.monthValue}/${month.year}")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating income pie chart: ${e.message}", e)
            return null
        }
    }
    
    /**
     * Tạo và định dạng biểu đồ PieChart với entries và tiêu đề đã cho
     */
    private fun createFormattedPieChart(pieEntries: List<PieEntry>, centerText: String): PieChart {
        Log.d(TAG, "📊 Creating formatted pie chart: '$centerText'")
        Log.d(TAG, "📈 Pie entries count: ${pieEntries.size}")
        
        // Validate entries
        if (pieEntries.isEmpty()) {
            throw IllegalArgumentException("Pie entries cannot be empty")
        }
        
        val totalValue = pieEntries.sumOf { it.value.toDouble() }.toFloat()
        Log.d(TAG, "📊 Total value: $totalValue")
        
        if (totalValue <= 0) {
            throw IllegalArgumentException("Total value must be greater than 0: $totalValue")
        }
        
        // Log each entry for debugging
        pieEntries.forEachIndexed { index, entry ->
            Log.d(TAG, "📊 Entry $index: ${entry.label} = ${entry.value}")
        }
        
        // Kích thước tối ưu cho biểu đồ PDF
        val CHART_WIDTH = 1200
        val CHART_HEIGHT = 1200
        
        Log.d(TAG, "📐 Creating chart with dimensions: ${CHART_WIDTH}x${CHART_HEIGHT}")
        
        // Tạo biểu đồ và container
        val pieChart = PieChart(requireContext())
        val container = FrameLayout(requireContext())
        container.layoutParams = FrameLayout.LayoutParams(CHART_WIDTH, CHART_HEIGHT)
        container.addView(pieChart, FrameLayout.LayoutParams(CHART_WIDTH, CHART_HEIGHT))
        
        // Cấu hình biểu đồ với các thiết lập tối ưu cho PDF
        pieChart.apply {
            layoutParams = FrameLayout.LayoutParams(CHART_WIDTH, CHART_HEIGHT)
            minimumWidth = CHART_WIDTH
            minimumHeight = CHART_HEIGHT
            
            // Cấu hình cơ bản
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 50f  // Giảm để có nhiều không gian hơn cho labels
            transparentCircleRadius = 55f
            
            // Cấu hình center text
            setDrawCenterText(true)
            this.centerText = centerText
            setCenterTextSize(16f)  // Giảm size để tránh bị cắt
            setCenterTextColor(Color.BLACK)
            setCenterTextTypeface(android.graphics.Typeface.DEFAULT_BOLD)
            
            // Cấu hình rotation và interaction
            rotationAngle = 0f
            isRotationEnabled = false  // Tắt rotation để ổn định khi render
            isHighlightPerTapEnabled = false  // Tắt highlight để tránh lỗi render
            
            // Cấu hình entry labels (labels trên từng slice)
            setDrawEntryLabels(false)  // Tắt entry labels để tránh overlap, dùng legend thay thế
            
            // Cấu hình legend với thiết lập tối ưu
            legend.apply {
                isEnabled = true
                textSize = 12f
                formSize = 10f
                typeface = android.graphics.Typeface.DEFAULT
                textColor = Color.BLACK
                
                // Cấu hình vị trí và layout của legend
                verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                
                // Cấu hình word wrap và spacing
                isWordWrapEnabled = true
                maxSizePercent = 0.95f  // Sử dụng tối đa 95% chiều rộng
                xEntrySpace = 8f
                yEntrySpace = 4f
                formToTextSpace = 4f
            }
            
            // Cấu hình margins để đảm bảo legend hiển thị đầy đủ
            setExtraOffsets(20f, 20f, 20f, 60f)  // left, top, right, bottom - tăng bottom cho legend
            setMinOffset(20f)
            
            // Hiển thị phần trăm
            setUsePercentValues(true)
        }
        
        // Tính tổng giá trị để tính phần trăm chính xác
        val total = pieEntries.sumOf { it.value.toDouble() }.toFloat()
        Log.d(TAG, "Total value for chart '$centerText': $total")
        
        // Cấu hình dataset với màu sắc tối ưu cho PDF
        val dataSet = PieDataSet(pieEntries, "")
        dataSet.apply {
            sliceSpace = 2f  // Giảm khoảng cách giữa các slice
            selectionShift = 0f  // Tắt selection shift để tránh lỗi render
            
            // Màu sắc tối ưu cho PDF (tương phản cao, dễ phân biệt)
            colors = listOf(
                Color.rgb(255, 152, 0),   // Orange
                Color.rgb(76, 175, 80),   // Green
                Color.rgb(33, 150, 243),  // Blue
                Color.rgb(244, 67, 54),   // Red
                Color.rgb(156, 39, 176),  // Purple
                Color.rgb(0, 188, 212),   // Cyan
                Color.rgb(255, 235, 59),  // Yellow
                Color.rgb(121, 85, 72),   // Brown
                Color.rgb(96, 125, 139),  // Blue Grey
                Color.rgb(158, 158, 158)  // Grey
            )
            
            // Cấu hình value labels (hiển thị trên biểu đồ)
            valueTextSize = 14f
            valueTextColor = Color.BLACK
            valueTypeface = android.graphics.Typeface.DEFAULT_BOLD
            
            // Tắt value lines để tránh overlap và lỗi render
            setDrawValues(true)  // Hiển thị values
            setValueLinePart1OffsetPercentage(80f)
            valueLinePart1Length = 0.3f
            valueLinePart2Length = 0.3f
            valueLineColor = Color.BLACK
            valueLineWidth = 1f
            
            // Đặt vị trí value bên trong slice để tránh cắt
            yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
            xValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
        }
        
        // Tạo formatter cho việc hiển thị phần trăm
        val percentFormatter = PercentFormatter()
        
        // Đặt data và formatter cho biểu đồ
        val pieData = PieData(dataSet).apply {
            setValueFormatter(percentFormatter)
            setValueTextSize(12f)  // Giảm size để fit trong slice
            setValueTextColor(Color.WHITE)  // Màu trắng để nổi bật trên nền màu
        }
        
        // Thiết lập data cho biểu đồ
        Log.d(TAG, "📊 Setting chart data...")
        pieChart.data = pieData
        
        // Kiểm tra data đã được set thành công
        if (pieChart.data == null) {
            throw IllegalStateException("Failed to set chart data")
        }
        
        Log.d(TAG, "✅ Chart data set successfully. Entry count: ${pieChart.data.entryCount}")
        
        // Đảm bảo tính toán và render chính xác
        Log.d(TAG, "🔄 Calculating chart offsets and preparing for render...")
        pieChart.calculateOffsets()
        pieChart.notifyDataSetChanged()
        pieChart.invalidate()
        
        // Đo và layout biểu đồ với container
        Log.d(TAG, "📏 Measuring and laying out container...")
        container.measure(
            View.MeasureSpec.makeMeasureSpec(CHART_WIDTH, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(CHART_HEIGHT, View.MeasureSpec.EXACTLY)
        )
        container.layout(0, 0, CHART_WIDTH, CHART_HEIGHT)
        
        // Đo và layout riêng cho pieChart
        Log.d(TAG, "📏 Measuring and laying out pie chart...")
        pieChart.measure(
            View.MeasureSpec.makeMeasureSpec(CHART_WIDTH, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(CHART_HEIGHT, View.MeasureSpec.EXACTLY)
        )
        pieChart.layout(0, 0, CHART_WIDTH, CHART_HEIGHT)
        
        Log.d(TAG, "📏 Final chart dimensions: ${pieChart.width}x${pieChart.height}")
        
        // Kiểm tra chart có kích thước hợp lệ
        if (pieChart.width <= 0 || pieChart.height <= 0) {
            throw IllegalStateException("Chart has invalid dimensions after layout: ${pieChart.width}x${pieChart.height}")
        }
        
        // Đảm bảo đủ thời gian để biểu đồ được render hoàn toàn
        Log.d(TAG, "⏳ Waiting for chart to render completely...")
        try {
            Thread.sleep(600) // Tăng thời gian để đảm bảo render hoàn thiện
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            Log.w(TAG, "⚠️ Chart render wait interrupted")
        }
        
        // Kiểm tra cuối cùng
        if (pieChart.data == null || pieChart.data.entryCount <= 0) {
            throw IllegalStateException("Chart data is invalid after setup")
        }
        
        Log.d(TAG, "✅ Formatted pie chart created successfully with total value: $totalValue")
        Log.d(TAG, "✅ Chart ready for bitmap conversion")
        return pieChart
    }
    
    override fun onBackClicked() {
        requireActivity().onBackPressed()
    }
} 