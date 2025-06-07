package com.qltc.finace.view.main.enter.category

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.data.Fb
import com.qltc.finace.data.entity.Category
import com.qltc.finace.databinding.FragmentCategoryDetailBinding
import com.qltc.finace.view.adapter.AdapterCategoryDetail
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentCategoryDetail :
    BaseFragment<FragmentCategoryDetailBinding, CategoryDetailViewModel>(),
    CategoryDetailListener, AdapterCategoryDetail.OnClickListener, OnSwipeItemCategoryDetail {
    override val viewModel: CategoryDetailViewModel by viewModels()
    override val layoutID: Int = R.layout.fragment_category_detail

    val adapter by lazy { AdapterCategoryDetail(this) }
    private var typeCategory: String = Fb.CategoryIncome

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.apply {
            listener = this@FragmentCategoryDetail
            viewModel = this@FragmentCategoryDetail.viewModel
        }

        typeCategory = arguments?.getString(KEY_CATEGORY) ?: Fb.CategoryIncome
        if (typeCategory == Fb.CategoryIncome) {
            viewModel.nameCategory = this.getString(R.string.income)
        } else
            viewModel.nameCategory = this.getString(R.string.expense)

        viewModel.getCategory(typeCategory = typeCategory)
        viewBinding.rcv.adapter = this.adapter

        val itemDeclaration = DividerItemDecoration(this.context,DividerItemDecoration.VERTICAL)
        viewBinding.rcv.addItemDecoration(itemDeclaration)

        val itemTouchHelperSimpleCallback = OnSwipeAdapterCategoryDetail(0,ItemTouchHelper.LEFT, this)
        ItemTouchHelper(itemTouchHelperSimpleCallback).attachToRecyclerView(viewBinding.rcv)
        viewModel.listCategory.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

    }


    override fun onClick(position: Int, listCategory: MutableList<Category>) {
    }

    override fun backPress() {
        findNavController().popBackStack()
    }
    override fun addCategory() {
        findNavController().navigate(R.id.frg_add_category,
            Bundle().apply {
                putString(KEY_CATEGORY, typeCategory)
            })
    }

    companion object {
        const val KEY_CATEGORY = "KEY_CATEGORY"
    }

    override fun onSwipe(viewHolder: AdapterCategoryDetail.CategoryDetailViewHolder) {
        val category = adapter.currentList[viewHolder.absoluteAdapterPosition]
        if(category != null) {
            viewModel.removeCategory(typeCategory = this.typeCategory, category = category) {
                adapter.notifyItemRemoved(viewHolder.absoluteAdapterPosition)
                Toast.makeText(this.context, "Bạn đã xóa danh mục ${category.title}" , Toast.LENGTH_SHORT).show()
            }
        }
    }
}