/*
 * Copyright (C)  2015 - 2019 Lukoh Nam, goForer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goforer.base.presentation.view.helper

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.goforer.base.presentation.view.holder.ItemHolderBinder
import com.goforer.grabph.R
import javax.inject.Inject

class RecyclerItemTouchHelperCallback @Inject
constructor(private val context: Context, private val helperListener: ItemTouchHelperListener,
            private val bgColor: Int) : ItemTouchHelper.Callback() {

    private var background: Drawable? = null
    private var deleteIcon: Drawable? = null

    private var deleteIconMargin: Int = 0

    private var initiated: Boolean = false

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        // Set movement flags based on the layout manager
        return if (recyclerView.layoutManager is androidx.recyclerview.widget.GridLayoutManager) {
            val dragFlags = (ItemTouchHelper.UP or ItemTouchHelper.DOWN
                    or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
            val swipeFlags = 0

            makeMovementFlags(dragFlags, swipeFlags)
        } else {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = ItemTouchHelper.START

            makeMovementFlags(dragFlags, swipeFlags)
        }
    }

    override fun onMove(recyclerView: RecyclerView, source: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder): Boolean {
        if (source.itemViewType != target.itemViewType) {
            return false
        }

        // Notify the mainAdapter of the move
        helperListener.onItemMove(source.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Notify the mainAdapter of the dismissal
        helperListener.onItemDismiss(viewHolder.adapterPosition)
    }

    override fun onChildDraw(canvas: Canvas, recyclerView: RecyclerView,
                             viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                             actionState: Int, isCurrentlyActive: Boolean) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val itemView = viewHolder.itemView

            // not sure why, but this method get's called for viewholder that are already swiped away
            if (viewHolder.adapterPosition == -1) {
                // not interested in those
                return
            }

            if (!initiated) {
                init()
            }

            // draw red background
            background?.setBounds(itemView.right + dX.toInt(), itemView.top,
                    itemView.right, itemView.bottom)
            background?.draw(canvas)

            // draw delete-icon
            val itemHeight = itemView.bottom - itemView.top
            val intrinsicWidth = deleteIcon?.intrinsicWidth!!
            val intrinsicHeight = deleteIcon?.intrinsicWidth!!

            val xMarkLeft = itemView.right - deleteIconMargin - intrinsicWidth
            val xMarkRight = itemView.right - deleteIconMargin
            val xMarkTop = itemView.top + (itemHeight - intrinsicHeight) / 2
            val xMarkBottom = xMarkTop + intrinsicHeight

            deleteIcon!!.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom)
            deleteIcon!!.draw(canvas)


            // Fade out the view as it is swiped out of the parent's bounds
            val alpha = ALPHA_FULL - Math.abs(dX) / viewHolder.itemView.width.toFloat()
            viewHolder.itemView.alpha = alpha
            viewHolder.itemView.translationX = dX

        } else {
            super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        // We only want the active item to change
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder is ItemHolderBinder<*>) {
                // Let the view holder know that this item is being moved or dragged
                val itemViewHolder = viewHolder as ItemHolderBinder<*>?
                itemViewHolder!!.onItemSelected()
            }
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG || actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            helperListener.onItemDrag(actionState)
        }

        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView,
                           viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        viewHolder.itemView.alpha = ALPHA_FULL

        if (viewHolder is ItemHolderBinder<*>) {
            // Tell the view holder it's time to restore the idle state
            val itemViewHolder = viewHolder as ItemHolderBinder<*>
            itemViewHolder.onItemClear()
        }
    }

    private fun init() {
        background = ColorDrawable(bgColor)
        deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_item)
        deleteIconMargin = context.resources.getDimension(R.dimen.margin_32).toInt()
        initiated = true
    }

    companion object {
        private const val ALPHA_FULL = 1.0f
    }
}
