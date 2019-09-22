/*
 * Copyright 2019 Lukoh Nam, goForer
 *    
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, 
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * You should have received a copy of the GNU General Public License along with this program.  
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.goforer.base.presentation.view.customs.layout

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout
import android.view.MotionEvent
import android.view.ViewConfiguration
import kotlin.math.abs

class SwipeRefreshLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet ? = null): SwipyRefreshLayout(context, attrs) {
    private var touchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop
    private var prevX: Float = 0.toFloat()

    @SuppressLint("Recycle")
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> prevX = MotionEvent.obtain(event).x

            MotionEvent.ACTION_MOVE -> {
                val eventX = event.x
                val xDiff = abs(eventX - prevX)

                if (xDiff > touchSlop) {
                    return false
                }
            }
        }

        return super.onInterceptTouchEvent(event)
    }
}