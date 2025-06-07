package com.qltc.finace.view.main.helper

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.data.entity.FAQItem
import com.qltc.finace.databinding.FragmentFaqBinding
import com.qltc.finace.view.adapter.FAQAdapter

class FragmentFAQ : BaseFragment<FragmentFaqBinding, FAQViewModel>(), FAQListener {
    override val layoutID: Int = R.layout.fragment_faq
    override val viewModel: FAQViewModel by viewModels()
    
    private lateinit var faqAdapter: FAQAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.lifecycleOwner = this
        viewBinding.listener = this
        
        setupRecyclerView()
        loadFAQData()
    }

    private fun setupRecyclerView() {
        faqAdapter = FAQAdapter()
        viewBinding.rvFaq.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = faqAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadFAQData() {
        val faqList = getFAQData()
        faqAdapter.submitList(faqList)
    }

    private fun getFAQData(): List<FAQItem> {
        return listOf(
            FAQItem(
                id = 0,
                question = "Làm thế nào để thêm một khoản thu mới?",
                answer = "Bước 1️⃣: Trên giao diện chính, chuyển sang Tab \"Khoản thu\".\nBước 2️⃣: Nhấn vào nút \"Thêm mới\" (thường là biểu tượng dấu \"+\").\nBước 3️⃣: Nhập số tiền thu, chọn danh mục thu, ngày nhận tiền, và ghi chú.\nBước 4️⃣: Nhấn \"Lưu\" để hoàn tất."
            ),
            FAQItem(
                id = 1,
                question = "Làm thế nào để thêm một khoản chi mới?",
                answer = "Bước 1️⃣: Trên giao diện chính, chuyển sang Tab \"Khoản chi\".\nBước 2️⃣: Nhấn vào nút \"Thêm mới\" (thường là biểu tượng dấu \"+\").\nBước 3️⃣: Nhập số tiền chi, chọn danh mục chi, ngày chi tiền, và ghi chú.\nBước 4️⃣: Nhấn \"Lưu\" để hoàn tất."
            ),
            FAQItem(
                id = 2,
                question = "Tôi có thể sửa hoặc xóa giao dịch đã nhập không?",
                answer = "Có, bạn hoàn toàn có thể.\n\n✏️ Để sửa: Mở Tab \"Khoản thu\" hoặc \"Khoản chi\", tìm giao dịch cần sửa, nhấn vào giao dịch đó và chọn \"Sửa\".\n\n🗑️ Để xóa: Tương tự, tìm giao dịch và chọn \"Xóa\". Lưu ý: Giao dịch đã xóa không thể khôi phục."
            ),
            FAQItem(
                id = 3,
                question = "Làm thế nào để xem báo cáo thu chi theo tháng/năm?",
                answer = "Bước 1️⃣: Từ menu chính, chọn mục \"Báo cáo\".\nBước 2️⃣: Chọn \"Tháng\" hoặc \"Năm\" bạn muốn xem.\nBước 3️⃣: Sử dụng bộ lọc để chọn tháng/năm cụ thể.\n\nUng dụng sẽ hiển thị tổng thu, tổng chi, và số dư của kỳ đã chọn."
            ),
            FAQItem(
                id = 4,
                question = "Biểu đồ tròn và biểu đồ cột có ý nghĩa gì?",
                answer = "📊 Biểu đồ tròn: Thể hiện tỷ trọng chi tiêu cho từng danh mục (ví dụ: bao nhiêu % cho Ăn uống, Đi lại...).\n\n📈 Biểu đồ cột: So sánh số liệu giữa các khoảng thời gian hoặc so sánh thu-chi qua các tháng."
            ),
            FAQItem(
                id = 5,
                question = "Làm thế nào để xuất báo cáo ra file PDF?",
                answer = "Bước 1️⃣: Đi đến mục \"Báo cáo\".\nBước 2️⃣: Chọn tháng hoặc năm bạn muốn xuất.\nBước 3️⃣: Tìm nút \"Xuất file\" hoặc \"Tải xuống\".\nBước 4️⃣: Chọn định dạng \"PDF\".\n\n⚠️ Lưu ý: Cần cấp quyền truy cập bộ nhớ để lưu file."
            ),
            FAQItem(
                id = 6,
                question = "Làm thế nào để xuất dữ liệu ra file CSV?",
                answer = "Bước 1️⃣: Đi đến mục \"Báo cáo\".\nBước 2️⃣: Chọn khoảng thời gian dữ liệu muốn xuất.\nBước 3️⃣: Tìm nút \"Xuất file\".\nBước 4️⃣: Chọn định dạng \"CSV\".\n\nFile CSV có thể mở bằng Excel hoặc Google Sheets để phân tích chi tiết."
            ),
            FAQItem(
                id = 7,
                question = "Làm thế nào để thay đổi thông tin hồ sơ?",
                answer = "Bước 1️⃣: Từ menu chính, chọn \"Tài khoản\" hoặc \"Hồ sơ\".\nBước 2️⃣: Nhấn \"Chỉnh sửa hồ sơ\".\nBước 3️⃣: Thay đổi tên hiển thị, email (nếu được phép), ảnh đại diện.\nBước 4️⃣: Nhấn \"Lưu\" để cập nhật."
            ),
            FAQItem(
                id = 8,
                question = "Tôi muốn đổi mật khẩu, phải làm sao?",
                answer = "Bước 1️⃣: Vào \"Tài khoản\" hoặc \"Cài đặt\".\nBước 2️⃣: Tìm \"Đổi mật khẩu\" hoặc \"Bảo mật\".\nBước 3️⃣: Nhập mật khẩu cũ, sau đó nhập mật khẩu mới và xác nhận.\nBước 4️⃣: Nhấn \"Lưu\" hoặc \"Xác nhận\"."
            ),
            FAQItem(
                id = 9,
                question = "Làm thế nào để đăng xuất khỏi tài khoản?",
                answer = "Bước 1️⃣: Vào \"Tài khoản\" hoặc \"Cài đặt\".\nBước 2️⃣: Tìm và chọn \"Đăng xuất\".\nBước 3️⃣: Xác nhận yêu cầu đăng xuất."
            ),
            FAQItem(
                id = 10,
                question = "Tôi quên mật khẩu, làm sao để lấy lại?",
                answer = "Bước 1️⃣: Tại màn hình đăng nhập, nhấn \"Quên mật khẩu?\".\nBước 2️⃣: Nhập email đã đăng ký tài khoản.\nBước 3️⃣: Kiểm tra hộp thư (kể cả thư mục spam) và làm theo hướng dẫn để đặt lại mật khẩu mới."
            ),
            FAQItem(
                id = 11,
                question = "Tôi có thể liên hệ hỗ trợ qua những kênh nào?",
                answer = "Ứng dụng QLTC hỗ trợ qua nhiều kênh:\n\n📘 Facebook: Fanpage chính thức\n📧 Gmail: Email hỗ trợ khách hàng\n💬 Zalo: Chat trực tiếp\n📞 Điện thoại: Hotline hỗ trợ\n\nBạn có thể tìm thông tin chi tiết trong mục \"Trợ giúp\" của ứng dụng."
            ),
            FAQItem(
                id = 12,
                question = "Ứng dụng có hỗ trợ tạo danh mục tùy chỉnh không?",
                answer = "Có! Để quản lý tài chính hiệu quả hơn:\n\nBước 1️⃣: Vào \"Cài đặt\".\nBước 2️⃣: Chọn \"Quản lý Danh mục\".\nBước 3️⃣: Bạn có thể \"Thêm mới\", \"Sửa tên\" hoặc \"Xóa\" danh mục cho cả khoản thu và khoản chi."
            ),
            FAQItem(
                id = 13,
                question = "Tại sao xuất file cần quyền truy cập bộ nhớ?",
                answer = "Ứng dụng cần quyền truy cập bộ nhớ để lưu file báo cáo (PDF hoặc CSV) vào thư mục trên điện thoại của bạn.\n\nNếu không có quyền này, ứng dụng không thể hoàn thành việc lưu file. Bạn có thể cấp quyền trong Cài đặt > Ứng dụng > QLTC > Quyền."
            ),
            FAQItem(
                id = 14,
                question = "Làm thế nào để lọc báo cáo theo danh mục cụ thể?",
                answer = "Bước 1️⃣: Trong mục \"Báo cáo\", chọn khoảng thời gian.\nBước 2️⃣: Tìm tùy chọn \"Lọc theo danh mục\" (biểu tượng phễu).\nBước 3️⃣: Chọn danh mục chi tiêu hoặc thu nhập cụ thể.\n\nBáo cáo sẽ cập nhật để chỉ hiển thị dữ liệu của danh mục đã chọn."
            )
        )
    }

    override fun onOpenNotebookLMClick() {
        TODO("Not yet implemented")
    }

    override fun onBackClick() {
        try {
            findNavController().popBackStack()
        } catch (e: Exception) {
            // Fallback nếu popBackStack() gặp lỗi
            try {
                findNavController().popBackStack(R.id.frag_home, false)
            } catch (e2: Exception) {
                try {
                    // Phương pháp thay thế cuối cùng
                    findNavController().navigate(R.id.frag_home)
                } catch (e3: Exception) {
                    // Ghi log lỗi
                    e3.printStackTrace()
                }
            }
        }
    }
} 