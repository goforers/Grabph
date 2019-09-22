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

@file:Suppress("NAME_SHADOWING", "DEPRECATION")

package com.goforer.base.presentation.view.decoration

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View

@SuppressLint("DuplicateDivider")
class DividerItemDecoration(context: Context, orientation: Int, drawable: Drawable?):
                                                                    RecyclerView.ItemDecoration() {
    private val divider: Drawable

    private var orientation: Int = 0

    constructor(context: Context, orientation: Int) : this(context, orientation, null)

    init {
        var drawable = drawable
        if (drawable == null) {
            val array = context.obtainStyledAttributes(ATTRS)
            drawable = array.getDrawable(0)
            array.recycle()
        }

        divider = drawable!!
        setOrientation(orientation)
    }

    /**
     * Should the layout be a column or a row.
     * Use "horizontal" for a row, "vertical" for a column. The default is horizontal.
     *
     * @param orientation Pass HORIZONTAL or VERTICAL.
     */
    private fun setOrientation(orientation: Int) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw IllegalArgumentException("invalid orientation")
        }

        this.orientation = orientation
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (orientation == VERTICAL_LIST) {
            drawVertical(canvas, parent)
        } else {
            drawHorizontal(canvas, parent)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                state: RecyclerView.State) {
        if (orientation == VERTICAL_LIST) {
            outRect.set(0, 0, 0, divider.intrinsicHeight)
        } else {
            outRect.set(0, 0, divider.intrinsicWidth, 0)
        }
    }

    private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child
                    .layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + divider.intrinsicHeight
            divider.setBounds(left, top, right, bottom)
            divider.draw(canvas)
        }
    }

    private fun drawHorizontal(canvas: Canvas, parent: RecyclerView) {
        val top = parent.paddingTop
        val bottom = parent.height - parent.paddingBottom

        val childCount = parent.childCount
        
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child
                    .layoutParams as RecyclerView.LayoutParams
            val left = child.right + params.rightMargin
            val right = left + divider.intrinsicHeight
            
            divider.setBounds(left, top, right, bottom)
            divider.draw(canvas)
        }
    }

    companion object {
        private val ATTRS = intArrayOf(android.R.attr.listDivider)

        private const val HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL

        const val VERTICAL_LIST = LinearLayoutManager.VERTICAL
    }
}