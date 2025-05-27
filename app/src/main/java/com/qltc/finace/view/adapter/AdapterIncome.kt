package com.qltc.finace.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qltc.finace.R
import com.qltc.finace.data.entity.Category
import com.qltc.finace.databinding.ItemCategoryBinding

class AdapterIncome(private var onClickListener: AdapterIncome.OnClickListener) :
    ListAdapter<Category, AdapterIncome.CategoryViewHolder>(Callback()) {
    class CategoryViewHolder(var view: ItemCategoryBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(item: Category) {
            view.data = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewBinding = DataBindingUtil.inflate<ItemCategoryBinding>(
            inflater, R.layout.item_category, parent, false
        )
        return CategoryViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener {
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
        fun onClick(position: Int, listCategory: MutableList<Category>)
    }
}