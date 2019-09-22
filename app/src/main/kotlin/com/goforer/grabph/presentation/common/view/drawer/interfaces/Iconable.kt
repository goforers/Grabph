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

package com.goforer.grabph.presentation.common.view.drawer.interfaces

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.materialdrawer.holder.ImageHolder

interface Iconable<T> {
    val icon: ImageHolder

    fun withIcon(icon: Drawable): T

    fun withIcon(iicon: IIcon): T

    fun withIcon(icon: ImageHolder): T

    fun withIcon(@DrawableRes iconRes: Int): T
}