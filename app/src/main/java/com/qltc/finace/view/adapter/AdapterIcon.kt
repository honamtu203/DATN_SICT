package com.qltc.finace.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qltc.finace.R
import com.qltc.finace.databinding.ItemIconBinding

class AdapterIcon(private var onClickListener: IconOnClickListener) :
    ListAdapter<String, AdapterIcon.IconViewHolder>(Callback()) {
    var itemSelect = -1;
    class IconViewHolder(val view: ItemIconBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(item: String) {
            view.data = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewBinding = DataBindingUtil.inflate<ItemIconBinding>(
            inflater,
            R.layout.item_icon,
            parent,
            false
        )
        return IconViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener{
            onClickListener.onClick(position, listIcon =  currentList)
        }

    }

    class Callback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

    }

    interface IconOnClickListener {
        fun onClick(position: Int, listIcon : MutableList<String>)
    }

}