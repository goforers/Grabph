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

class DepthPageTransformer: androidx.viewpager.widget.ViewPager.PageTransformer {
    companion object {
        const val DEFAULT_SCALE = 1f
        const val MIN_SCALE = 0.75f
    }

    override fun transformPage(view: View, position: Float) {
        if (position <= 0) {
            view.translationX = 0f
            view.scaleX = DEFAULT_SCALE
            view.scaleY = DEFAULT_SCALE
        } else if (position <= 1) {
            val scaleFactor: Float =
                    MIN_SCALE + (DEFAULT_SCALE - MIN_SCALE)* (DEFAULT_SCALE - Math.abs(position))
            view.alpha = DEFAULT_SCALE - position
            view.pivotY = 0.5f * view.height
            view.translationX = view.height * -position
            view.scaleX = scaleFactor
            view.scaleY = scaleFactor
        }
    }
}