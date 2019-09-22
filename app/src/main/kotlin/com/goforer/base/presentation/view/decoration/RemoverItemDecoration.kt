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

package com.goforer.base.presentation.view.decoration

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.goforer.grabph.R

class RemoverItemDecoration(private val bgColor: Int): RecyclerView.ItemDecoration() {
    // we want to cache this and not allocate anything repeatedly in the onDraw method
    private var background: Drawable? = null

    private var isInitiated: Boolean = false

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                state: RecyclerView.State) {
        outRect.left = view.resources.getDimensionPixelSize(R.dimen.margin_2)
        outRect.right = view.resources.getDimensionPixelSize(R.dimen.margin_2)
        outRect.bottom = view.resources.getDimensionPixelSize(R.dimen.margin_2)

        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = view.resources.getDimensionPixelSize(R.dimen.margin_2)
        } else {
            outRect.top = view.resources.getDimensionPixelSize(R.dimen.margin_6)
        }
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {

        if (!isInitiated) {
            init()
        }

        // only if animation is in progress
        if (parent.itemAnimator!!.isRunning) {

            // some items might be animating down and some items might be animating up to close the gap left by the removed item
            // this is not exclusive, both movement can be happening at the same time
            // to reproduce this leave just enough items so the first one and the last one would be just a little off screen
            // then remove one from the middle

            // find first child with translationY > 0
            // and last one with translationY < 0
            // we're after a rect that is not covered in recycler-view views at this point in time
            var lastViewComingDown: View? = null
            var firstViewComingUp: View? = null

            // this is fixed
            val left = 0
            val right = parent.width

            // this we need to find out
            var top = 0
            var bottom = 0

            // find relevant translating views
            val childCount = parent.layoutManager!!.childCount
            for (i in 0 until childCount) {
                val child = parent.layoutManager!!.getChildAt(i)
                if (child!!.translationY < 0) {
                    // view is coming down
                    lastViewComingDown = child
                } else if (child.translationY > 0) {
                    // view is coming up
                    if (firstViewComingUp == null) {
                        firstViewComingUp = child
                    }
                }
            }

            if (lastViewComingDown != null && firstViewComingUp != null) {
                // views are coming down AND going up to fill the void
                top = lastViewComingDown.bottom + lastViewComingDown.translationY.toInt()
                bottom = firstViewComingUp.top + firstViewComingUp.translationY.toInt()
            } else if (lastViewComingDown != null) {
                // views are going down to fill the void
                top = lastViewComingDown.bottom + lastViewComingDown.translationY.toInt()
                bottom = lastViewComingDown.bottom
            } else if (firstViewComingUp != null) {
                // views are coming up to fill the void
                top = firstViewComingUp.top
                bottom = firstViewComingUp.top + firstViewComingUp.translationY.toInt()
            }

            background!!.setBounds(left, top, right, bottom)
            background!!.draw(canvas)

        }

        super.onDraw(canvas, parent, state)
    }

    private fun init() {
        background = ColorDrawable(bgColor)
        isInitiated = true
    }
}