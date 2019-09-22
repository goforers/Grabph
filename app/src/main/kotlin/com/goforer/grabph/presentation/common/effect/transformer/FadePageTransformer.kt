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

class FadePageTransformer: androidx.viewpager.widget.ViewPager.PageTransformer {

    override fun transformPage(view: View, position: Float) {
        if (position >= -1 || position <= 1) {
            fadePageOut(view, position, MIN_SCALE)
        }
    }

    private fun fadePageOut(view: View, position: Float, scale: Float) {
        // Fade the page out.
        view.alpha = DEFAULT_SCALE - position
        // Counteract the default slide transition
        view.pivotX = view.width.toFloat() * MIN_SCALE
        view.pivotY = view.height.toFloat() * MIN_SCALE
        view.translationX = MIN_TRANSLATION_X * view.width.toFloat() * position
        view.translationY = MIN_TRANSLATION_X * view.height.toFloat() * position
        // Scale the page down (between MIN_SCALE and 1)
        val scaleFactor = scale + (DEFAULT_SCALE - scale) * (DEFAULT_SCALE - Math.abs(position))
        view.scaleX = scaleFactor
        view.scaleY = scaleFactor
    }

    companion object {
        private const val DEFAULT_SCALE = 1f
        private const val MIN_SCALE = 0.75f
        private const val MIN_TRANSLATION_X = -0.01f
    }
}
