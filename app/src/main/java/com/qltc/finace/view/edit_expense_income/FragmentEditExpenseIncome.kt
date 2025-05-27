package com.qltc.finace.view.edit_expense_income

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.qltc.finace.AppBindingAdapter.setTimeFormatter
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.base.Constant
import com.qltc.finace.data.entity.Category
import com.qltc.finace.databinding.FragmentEditBinding
import com.qltc.finace.extension.formatDateTime
import com.qltc.finace.extension.toLocalDate
import com.qltc.finace.view.adapter.AdapterCategory
import com.qltc.finace.view.main.calendar.FinancialRecord
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class FragmentEditExpenseIncome : BaseFragment<FragmentEditBinding, EditExpenseIncomeViewModel>(), EditExpenseIncomeListener, AdapterCategory.OnClickListener{
    override val viewModel: EditExpenseIncomeViewModel by viewModels()
    override val layoutID: Int = R.layout.fragment_edit
    private val adapter by lazy { AdapterCategory(this)}
    private var x : List<Category>? = null
    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // Kiểm tra nếu có thay đổi và chưa lưu
            if (viewModel.isEnableButtonAdd.value == true) {
                showSaveConfirmationDialog()
            } else {
                // Không có thay đổi, thoát bình thường
                this.remove()
                findNavController().popBackStack()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            viewModel.itemData = arguments?.getParcelable(Constant.KEY_ITEM_IE)
            viewModel.listCategory = arguments?.getParcelableArrayList(Constant.KEY_LIST_CATEGORY)
        }
        catch (e : Exception) {
            e.message?.let { Log.e("FragmentEditExpenseIncome", "argument error $it") }
        }
        
        // Đăng ký callback xử lý nút back
        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.apply {
            listener = this@FragmentEditExpenseIncome
            viewModel = this@FragmentEditExpenseIncome.viewModel
        }
        if (viewModel.itemData == null || viewModel.listCategory == null) {
            onClickBack()
        }
        viewModel.apply {
            note = itemData?.noteExpenseIncome ?: ""
            date = itemData?.date.toLocalDate()
            money = itemData?.money?.toString() ?: ""
            /*
            check xem khooản thu (chi) thuộc category nào, sau đó gán  vị trí [i] category đó vào itemCategorySelected
             */
            if (listCategory != null) {
                for (item in listCategory!!) {
                    if(item.idCategory == itemData?.idCategory) {
                        itemCategorySelected = listCategory!!.indexOf(item)
                        categorySelected = item
                        break
                    }
                }
            }
        }
        setData()
        
        // Ẩn imageButton cũ vì đã thêm TextView và button mới
        viewBinding.imageButton.visibility = View.GONE
    }
    
    private fun showSaveConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Lưu thay đổi")
            .setMessage("Bạn có muốn lưu các thay đổi không?")
            .setPositiveButton("Lưu") { dialog, _ ->
                dialog.dismiss()
                onClickUpdate()
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
                backPressedCallback.remove()
                findNavController().popBackStack()
            }
            .create()
            .show()
    }
    
   private fun showToastIsUpdate(message : String) {
       Toast.makeText(
           requireContext(),
           message,
           Toast.LENGTH_SHORT
       ).show()
   }

    override fun onClickBack() {
        if (viewModel.isEnableButtonAdd.value == true) {
            showSaveConfirmationDialog()
        } else {
            findNavController().popBackStack()
        }
    }

    override fun onClickUpdate() {
        viewModel.itemData?.let {
            viewModel.updateItemData(it.typeExpenseOrIncome) {message ->
                showToastIsUpdate(message)
                findNavController().popBackStack()
            }
        }
    }

    override fun openDayPicker() {
        val picker = DatePickerDialog(
            requireContext(),
            { view, year, month, dayOfMonth ->
                viewModel.apply {
                    date = LocalDate.of(year, month+1, dayOfMonth)
                }
                viewBinding.pickTime.setTimeFormatter(viewModel.date)
            },
            viewModel.date.year,
            viewModel.date.monthValue -1,
            viewModel.date.dayOfMonth
        )
        picker.show()
    }
    
    private fun setData() {
        val time = viewModel.date.formatDateTime()
        viewBinding.pickTime.text = time

        if (viewModel.itemData?.typeExpenseOrIncome == FinancialRecord.TYPE_EXPENSE) {
            viewBinding.typeUpdate.text = getString(R.string.update_expense)
        }
        else {
            viewBinding.typeUpdate.text = getString(R.string.update_income)
        }
        adapter.submitList(viewModel.listCategory)
        viewBinding.rcv.adapter = this.adapter
       viewBinding.rcv.post { viewBinding.rcv
           .findViewHolderForAdapterPosition(viewModel.itemCategorySelected)!!
           .itemView.isSelected = true }
    }

    override fun onClickItemCategory(position: Int, listCategory: MutableList<Category>) {
        if (viewModel.itemCategorySelected != -1) {
            viewBinding.rcv
                .findViewHolderForAdapterPosition(viewModel.itemCategorySelected)!!
                .itemView.isSelected = false
        }
        viewModel.itemCategorySelected = position
        viewBinding.rcv
            .findViewHolderForAdapterPosition(viewModel.itemCategorySelected)!!
            .itemView.isSelected = true
        viewModel.categorySelected = listCategory[position]
    }
    
    override fun onDestroy() {
        super.onDestroy()
        backPressedCallback.remove()
    }
}