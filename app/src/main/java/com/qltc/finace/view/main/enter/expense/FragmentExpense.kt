package com.qltc.finace.view.main.enter.expense

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
import com.qltc.finace.databinding.FragmentExpenseBinding
import com.qltc.finace.extension.formatDateTime
import com.qltc.finace.view.adapter.AdapterExpense
import com.qltc.finace.view.main.enter.ShareEnterViewModel
import com.qltc.finace.view.main.enter.category.FragmentCategoryDetail
import com.qltc.finace.view.main.enter.expense.ExpenseListener
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class FragmentExpense : BaseFragment<FragmentExpenseBinding,ShareEnterViewModel>(), ExpenseListener,AdapterExpense.OnClickListener {
    override val viewModel: ShareEnterViewModel by activityViewModels()
    override val layoutID: Int = R.layout.fragment_expense
    val adapter by lazy {   AdapterExpense(this) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getCategoryExpense()
        viewBinding.apply {
            listener = this@FragmentExpense
            viewModel = this@FragmentExpense.viewModel
            viewBinding.rcvExpense.adapter = this@FragmentExpense.adapter
        }

        viewModel.listCategoryExpense.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
        setTimeDefault()

        viewModel.isAddExpense.observe(viewLifecycleOwner) {
            if (it) {
                clearDataInput()
                Toast.makeText(this@FragmentExpense.requireContext(), "Đã thêm khoản chi", Toast.LENGTH_SHORT).show()
                viewModel.isAddExpense.postValue(false)
            }
        }
    }

    override fun openDayPicker() {
        val picker = DatePickerDialog(
            requireContext(),
            { view, year, month, dayOfMonth ->
                viewModel.apply {
                    dateExpense = LocalDate.of(year, month+1, dayOfMonth)
                }
                viewBinding.pickTime.setTimeFormatter(viewModel.dateExpense)
            },
            viewModel.dateExpense.year,
            viewModel.dateExpense.monthValue -1,
            viewModel.dateExpense.dayOfMonth
        )
        picker.show()

    }

    override fun submitExpense() {
        viewModel.submitExpense()
    }

    private fun setTimeDefault() {
        val time = viewModel.dateExpense.formatDateTime();
        viewBinding.pickTime.text = time
    }

    override fun onClick(position: Int, listCategory: MutableList<Category>) {
        if(listCategory[position].idCategory == Fb.ItemAddedCategory) {
            findNavController().navigate(
                R.id.frg_category_detail,
                Bundle().apply {
                    putString(
                        FragmentCategoryDetail.KEY_CATEGORY,
                        Fb.CategoryExpense
                    )
                }
            )
        }
        else {
            if (viewModel.itemCategoryExpenseSelected != -1) {
                viewBinding.rcvExpense
                    .findViewHolderForAdapterPosition(viewModel.itemCategoryExpenseSelected)!!
                    .itemView.isSelected = false
            }
            viewModel.itemCategoryExpenseSelected = position
            viewBinding.rcvExpense
                .findViewHolderForAdapterPosition(viewModel.itemCategoryExpenseSelected)!!
                .itemView.isSelected = true
            viewModel.categoryExpenseSelected = listCategory[position]
        }
    }
    private fun clearDataInput() {
        viewBinding.edtNoteExpense.setText("")
        viewBinding.inputMoneyExpense.setText("")
    }

}