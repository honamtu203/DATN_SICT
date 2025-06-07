package com.qltc.finace.view.main.enter.add_category

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.data.Fb
import com.qltc.finace.databinding.AddCategoryBinding
import com.qltc.finace.view.adapter.AdapterIcon
import com.qltc.finace.view.main.enter.category.FragmentCategoryDetail
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAddCategory(
) : BaseFragment<AddCategoryBinding, AddCategoryViewModel> (), AddCategoryListener, AdapterIcon.IconOnClickListener {
    override val viewModel: AddCategoryViewModel by viewModels()
    override val layoutID: Int = R.layout.add_category
    private  val adapter by lazy { AdapterIcon(this) }

    private  var typeCategory : String = Fb.CategoryIncome
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.apply {
            viewModel = this@FragmentAddCategory.viewModel
            rcv.adapter = this@FragmentAddCategory.adapter
            listener = this@FragmentAddCategory
        }
        typeCategory = arguments?.getString(FragmentCategoryDetail.KEY_CATEGORY) ?: Fb.CategoryIncome

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter.submitList(viewModel.getListIcon())
    }

    override fun onClick(position: Int, listIcon: MutableList<String>) {
        if (viewModel.idItemRcvIconSelect != -1 && viewBinding.rcv.findViewHolderForAdapterPosition(viewModel.idItemRcvIconSelect) == null) {
            return
        }
        if (viewModel.idItemRcvIconSelect != -1) {
            viewBinding.rcv
                .findViewHolderForAdapterPosition(viewModel.idItemRcvIconSelect)!!
                .itemView.isSelected = false
        }
        viewModel.idItemRcvIconSelect = position
        viewBinding.rcv
            .findViewHolderForAdapterPosition(viewModel.idItemRcvIconSelect)!!
            .itemView.isSelected = true
        viewModel.icon = listIcon[position]

    }

    override fun onClickAddNewCategory() {
        viewModel.addNewCategory(typeCategory = this.typeCategory,
            callback = {
                viewBinding.inputTitle.setText("")
                showToast()
            }
        )
    }
    private fun showToast() {
        Toast.makeText(this.activity, this.activity?.getString(R.string.da_them_danh_muc),Toast.LENGTH_SHORT).show()
    }
    override fun onClickBack() {
        findNavController().popBackStack()
    }
}