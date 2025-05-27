package com.qltc.finace.view.main.home.category

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.qltc.finace.view.adapter.AdapterCategoryDetail

class OnSwipeAdapterCategoryDetail(
    dragDirs: Int, swipeDirs: Int,
    val listener: OnSwipeItemCategoryDetail
) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,target: RecyclerView.ViewHolder) = true
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        listener.onSwipe(viewHolder as AdapterCategoryDetail.CategoryDetailViewHolder)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        val foregroundView = (viewHolder as AdapterCategoryDetail.CategoryDetailViewHolder)
            .viewBinding.foreground
        getDefaultUIUtil().onDraw(c,recyclerView,foregroundView,dX,dY,actionState,isCurrentlyActive)
    }

    override fun onChildDrawOver(c: Canvas, recyclerView: RecyclerView, viewHolder: ViewHolder?,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        val foregroundView = (viewHolder as AdapterCategoryDetail.CategoryDetailViewHolder)
            .viewBinding.foreground
        getDefaultUIUtil().onDrawOver(c, recyclerView,foregroundView,dX,dY,actionState,isCurrentlyActive)
    }

    override fun onSelectedChanged(viewHolder: ViewHolder?, actionState: Int) {
        if (viewHolder != null) {
            val foregroundView = (viewHolder as AdapterCategoryDetail.CategoryDetailViewHolder)
                .viewBinding.foreground
            getDefaultUIUtil().onSelected(foregroundView)
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        val foregroundView = (viewHolder as AdapterCategoryDetail.CategoryDetailViewHolder)
            .viewBinding.foreground
        getDefaultUIUtil().clearView(foregroundView)
    }

}
interface OnSwipeItemCategoryDetail {
    fun onSwipe(viewHolder: AdapterCategoryDetail.CategoryDetailViewHolder)
}
