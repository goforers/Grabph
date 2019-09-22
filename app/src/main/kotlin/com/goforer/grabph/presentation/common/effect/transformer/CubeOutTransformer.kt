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

class CubeOutTransformer: ViewPager.PageTransformer {
    override fun transformPage(view: View, position: Float) {
        view.pivotY = view.height * 0.5f
        view.rotationY = 50f * position
        if (position < 0) {
            view.pivotX = view.width.toFloat()
        } else {
            view.pivotX = 0f
        }
    }
}