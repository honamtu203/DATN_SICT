package com.qltc.finace.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qltc.finace.R
import com.qltc.finace.data.entity.CategoryOverView
import com.qltc.finace.databinding.ItemTotalCategoryBinding

class AdapterTopCategory(private var onClickListener: OnClickListener) :
    ListAdapter<CategoryOverView, AdapterTopCategory.CategoryViewHolder>(DiffCallback()) {

    interface OnClickListener {
        fun onClickCategory(item: CategoryOverView)
    }

    inner class CategoryViewHolder(var viewBinding: ItemTotalCategoryBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(item: CategoryOverView) {
            viewBinding.item = item
            viewBinding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewBinding = DataBindingUtil.inflate<ItemTotalCategoryBinding>(
            inflater, R.layout.item_total_category, parent, false
        )
        return CategoryViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener {
            onClickListener.onClickCategory(getItem(holder.absoluteAdapterPosition))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CategoryOverView>() {
        override fun areItemsTheSame(oldItem: CategoryOverView, newItem: CategoryOverView): Boolean {
            return oldItem.category.idCategory == newItem.category.idCategory
        }

        override fun areContentsTheSame(oldItem: CategoryOverView, newItem: CategoryOverView): Boolean {
            return oldItem == newItem
        }
    }
} 