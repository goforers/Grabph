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

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.github.clans.fab.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlin.math.max
import kotlin.math.min

class BottomFloatingButtonBehavior(context: Context, attrs: AttributeSet): CoordinatorLayout.Behavior<FloatingActionButton>(context, attrs) {
    @ViewCompat.NestedScrollType
    private var lastStartedType: Int = 0

    private var offsetAnimator: ValueAnimator? = null

    private var isSnappingEnabled = true

    override fun layoutDependsOn(parent: CoordinatorLayout, child: FloatingActionButton, dependency: View): Boolean {
        if (dependency is Snackbar.SnackbarLayout) {
            updateSnackbar(child, dependency)
        }

        return super.layoutDependsOn(parent, child, dependency)
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton,
                                     directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        if (axes == ViewCompat.SCROLL_AXIS_VERTICAL) {
            lastStartedType = type
            offsetAnimator?.cancel()

            return true
        }

        return false
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton,
                                   target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        child.translationY = max(0f, min(child.height.toFloat() * 2, child.translationY + dy))

        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton,
                                    target: View, type: Int) {
        if (!isSnappingEnabled) {
            return
        }

        if (lastStartedType == ViewCompat.TYPE_TOUCH
            || type == ViewCompat.TYPE_NON_TOUCH
            || type == ViewCompat.SCROLL_AXIS_VERTICAL) {
            // find nearest seam
            val currentTranslation = child.translationY
            val childHalfHeight = child.height * 0.5f

            // translate down
            if (currentTranslation >= childHalfHeight) {
                animateBarVisibility(child, isVisible = false)
            }
            else {
                animateBarVisibility(child, isVisible = true)
            }
        }
    }

    private fun animateBarVisibility(child: View, isVisible: Boolean) {
        offsetAnimator ?: createValueAnimator(child)
        offsetAnimator?.let {
            offsetAnimator?.cancel()
        }

        val targetTranslation = if (isVisible) 0f else child.height.toFloat() * 2

        offsetAnimator?.setFloatValues(child.translationY, targetTranslation)
        offsetAnimator?.start()
    }

    private fun createValueAnimator(child: View) {
        offsetAnimator = ValueAnimator().apply {
            interpolator = DecelerateInterpolator()
            duration = 50L
        }

        offsetAnimator?.addUpdateListener {
            child.translationY = it.animatedValue as Float
        }
    }

    private fun updateSnackbar(child: View, snackbarLayout: Snackbar.SnackbarLayout) {
        if (snackbarLayout.layoutParams is CoordinatorLayout.LayoutParams) {
            val params = snackbarLayout.layoutParams as CoordinatorLayout.LayoutParams

            params.anchorId = child.id
            params.anchorGravity = Gravity.TOP
            params.gravity = Gravity.TOP
            snackbarLayout.layoutParams = params
        }
    }
}