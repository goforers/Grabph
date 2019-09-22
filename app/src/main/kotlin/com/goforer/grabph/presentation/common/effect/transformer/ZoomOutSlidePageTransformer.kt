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

package com.goforer.grabph.presentation.common.effect.transformer

import androidx.viewpager.widget.ViewPager
import android.view.View

class ZoomOutSlidePageTransformer: androidx.viewpager.widget.ViewPager.PageTransformer {
    companion object {
        const val MIN_SCALE = 0.85f
        const val MIN_ALPHA = 0.5f
        const val MIN_PIVOT_Y = 0.75f
    }

    override fun transformPage(view: View, position: Float) {
        if (position >= -1 || position <= 1) {
            zoomOutSlide(view, position)
        }
    }

    private fun zoomOutSlide(view: View, position: Float) {
        // Modify the default slide transition to shrink the page as well
        val height = view.height.toFloat()
        val scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position))
        val verticalMargin = height * (1 - scaleFactor) / 2
        val horizontalMargin = view.width * (1 - scaleFactor) / 2

        // Center vertically
        view.pivotY = MIN_PIVOT_Y * height

        if (position < 0) {
            view.translationX = horizontalMargin - verticalMargin / 2
        } else {
            view.translationX = -horizontalMargin + verticalMargin / 2
        }

        // Scale the page down (between MIN_SCALE and 1)
        view.scaleX = scaleFactor
        view.scaleY = scaleFactor

        // Fade the page relative to its size.
        view.alpha = MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA)

    }
}