package com.qltc.finace.view.main.enter.income

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.qltc.finace.AppBindingAdapter.setTimeFormatter
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.data.Fb
import com.qltc.finace.data.entity.Category
import com.qltc.finace.databinding.FragmentIncomeBinding
import com.qltc.finace.extension.formatDateTime

import com.qltc.finace.view.adapter.AdapterIncome
import com.qltc.finace.view.main.enter.ShareEnterViewModel
import com.qltc.finace.view.main.enter.category.FragmentCategoryDetail
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class IncomeFragment : BaseFragment<FragmentIncomeBinding,ShareEnterViewModel>(),IncomeListener,AdapterIncome.OnClickListener {
    override val viewModel: ShareEnterViewModel by activityViewModels()
    override val layoutID: Int = R.layout.fragment_income

    val adapter by lazy {  AdapterIncome(this) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getCategoryIncome()
        viewBinding.apply {
            listener = this@IncomeFragment
            viewModel = this@IncomeFragment.viewModel
            viewBinding.rcvIncome.adapter = this@IncomeFragment.adapter
        }

        viewModel.listCategoryIncome.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
        viewModel.isAddIncome.observe(viewLifecycleOwner) {
            if(it) {
                Toast.makeText(this@IncomeFragment.requireContext(),"Đã thêm khoản thu", Toast.LENGTH_SHORT).show()
                clearInputData()
                viewModel.isAddIncome.value = false
            }
        }
        setTimeDefault()

    }
    override fun openDayPicker() {
            val picker = DatePickerDialog(
                requireContext(),
                { view, year, month, dayOfMonth ->
                    viewModel.apply {
                        dateIncome = LocalDate.of(year, month+1, dayOfMonth)
                    }
                    viewBinding.pickTime.setTimeFormatter(viewModel.dateIncome)
                },
                viewModel.dateIncome.year,
                viewModel.dateIncome.monthValue -1,
                viewModel.dateIncome.dayOfMonth
            )
            picker.show()
    }

    override fun submitIncome() {
        viewModel.submitIncome()
    }

    override fun onClick(position: Int, listCategory: MutableList<Category>) {
        if(listCategory[position].idCategory == Fb.ItemAddedCategory) {
            findNavController().navigate(
                R.id.frg_category_detail,
                Bundle().apply {
                    putString(
                        FragmentCategoryDetail.KEY_CATEGORY,
                        Fb.CategoryIncome
                    )
                }
            )
        }
        else {
            if (viewModel.itemCategoryIncomeSelected != -1) {
                viewBinding.rcvIncome
                    .findViewHolderForAdapterPosition(viewModel.itemCategoryIncomeSelected)!!
                    .itemView.isSelected = false
            }
            viewModel.itemCategoryIncomeSelected = position
            viewBinding.rcvIncome
                .findViewHolderForAdapterPosition(viewModel.itemCategoryIncomeSelected)!!
                .itemView.isSelected = true
            viewModel.categoryIncomeSelected = listCategory[position]
        }
    }
    private fun setTimeDefault() {
        val time = viewModel.dateIncome.formatDateTime()
        viewBinding.pickTime.text = time
    }
    private fun clearInputData() {
        viewBinding.edtNote.setText("")
        viewBinding.inputMoney.setText("")
    }
}