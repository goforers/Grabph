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

package com.goforer.base.presentation.view.behavior

import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat.SCROLL_AXIS_VERTICAL
import kotlin.math.max
import kotlin.math.min

class BottomStickyBehavior(private val anchorId: Int, private val notStickyMargin: Int): CoordinatorLayout.Behavior<View>() {
    override fun onStartNestedScroll(@NonNull coordinatorLayout: CoordinatorLayout, @NonNull child: View,
                                     @NonNull directTargetChild: View, @NonNull target: View, axes: Int, type: Int): Boolean {
        return axes == SCROLL_AXIS_VERTICAL
    }

    override fun onNestedPreScroll(@NonNull coordinatorLayout: CoordinatorLayout, @NonNull child: View,
                                   @NonNull target: View, dx: Int, dy: Int, @NonNull consumed: IntArray, type: Int) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)

        val anchor = coordinatorLayout.findViewById<View>(anchorId)
        val anchorLocation = IntArray(2)

        anchor.getLocationInWindow(anchorLocation)

        val bottom = coordinatorLayout.bottom

        //vertical position, cannot scroll over the bottom of the coordinator layout
        child.y = min(anchorLocation[1].toFloat(), bottom - child.height.toFloat())

        //Margins depend on the distance to the bottom
        val diff = max(bottom - anchorLocation[1] - child.height, 0)
        val layoutParams = child.layoutParams as ViewGroup.MarginLayoutParams

        layoutParams.leftMargin = min(diff, notStickyMargin)
        layoutParams.rightMargin = min(diff, notStickyMargin)
        child.layoutParams = layoutParams
    }
}