package com.qltc.finace.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qltc.finace.R
import com.qltc.finace.data.entity.Category
import com.qltc.finace.databinding.ItemCategoryDetailBinding

class AdapterCategoryDetail(private var onClickListener: OnClickListener) :
    ListAdapter<Category, AdapterCategoryDetail.CategoryDetailViewHolder>(Callback()) {
    var itemSelect = -1;
    class CategoryDetailViewHolder(val viewBinding: ItemCategoryDetailBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(item: Category) {
            viewBinding.data = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryDetailViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewBinding = DataBindingUtil.inflate<ItemCategoryDetailBinding>(
            inflater,
            R.layout.item_category_detail,
            parent,
            false
        )
        return CategoryDetailViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: CategoryDetailViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener{
           onClickListener.onClick(holder.absoluteAdapterPosition, currentList)
        }

    }

    class Callback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.idCategory == newItem.idCategory
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }

    }

    interface OnClickListener {
        fun onClick(position: Int, listCategory : MutableList<Category>)
    }

}