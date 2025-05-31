package com.qltc.finace.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qltc.finace.R
import com.qltc.finace.data.entity.FAQItem
import com.qltc.finace.databinding.ItemFaqBinding

class FAQAdapter : ListAdapter<FAQItem, FAQAdapter.FAQViewHolder>(Callback()) {

    class FAQViewHolder(val view: ItemFaqBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(item: FAQItem, adapter: FAQAdapter, position: Int) {
            view.data = item
            view.adapter = adapter
            view.position = position
            view.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewBinding = DataBindingUtil.inflate<ItemFaqBinding>(
            inflater,
            R.layout.item_faq,
            parent,
            false
        )
        return FAQViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: FAQViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, this, position)
    }

    fun toggleExpansion(position: Int) {
        val newList = currentList.toMutableList()
        newList[position] = newList[position].copy(isExpanded = !newList[position].isExpanded)
        submitList(newList)
    }

    class Callback : DiffUtil.ItemCallback<FAQItem>() {
        override fun areItemsTheSame(oldItem: FAQItem, newItem: FAQItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FAQItem, newItem: FAQItem): Boolean {
            return oldItem == newItem
        }
    }
} 