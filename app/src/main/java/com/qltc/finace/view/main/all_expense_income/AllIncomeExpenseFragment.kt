package com.qltc.finace.view.main.all_expense_income

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.databinding.FragmentAllDataIncomeExpenseBinding
import com.qltc.finace.view.adapter.AdapterExpenseIncomeReport

import com.qltc.finace.view.main.calendar.FinancialRecord
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllIncomeExpenseFragment : BaseFragment<FragmentAllDataIncomeExpenseBinding,AllIncomeExpenseViewModel>(),AllIncomeExpenseListener
, AdapterExpenseIncomeReport.OnClickListener{
    override val viewModel: AllIncomeExpenseViewModel by activityViewModels()
    override val layoutID: Int = R.layout.fragment_all_data_income_expense
    private val adapter by lazy { AdapterExpenseIncomeReport(this) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewBinding.apply {
            listener = this@AllIncomeExpenseFragment
            rcv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            rcv.adapter = this@AllIncomeExpenseFragment.adapter
        }
        
        // Quan sát dữ liệu từ ViewModel
        viewModel.dataRcv.observe(viewLifecycleOwner) { transactions ->
            adapter.submitList(null) // Xóa danh sách cũ
            adapter.submitList(transactions) // Cập nhật danh sách mới
        }
        
        // Tải dữ liệu mới
        viewModel.getAll()
    }

    override fun onClickItemEI(item: FinancialRecord) {

    }

    override fun onClickBack() {
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