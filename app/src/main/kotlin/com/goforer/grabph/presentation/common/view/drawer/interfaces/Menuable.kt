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

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mikepenz.fastadapter.IIdentifyable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.holder.StringHolder

@SuppressWarnings("unused")
interface Menuable<T> : IIdentifyable<T> {
    val menu: StringHolder

    val menuSub1: StringHolder

    val menuSub2: StringHolder

    val description: StringHolder

    val icon: ImageHolder

    val isSelectable: Boolean
    fun withMenu(menu: String): T

    fun withMenu(@StringRes menuRes: Int): T

    fun withMenu(menu: StringHolder): T

    fun withMenuSub1(menuSub1: String): T

    fun withMenuSub1(@StringRes menuSub1Res: Int): T

    fun withMenuSub1(menuSub1: StringHolder): T

    fun withMenuSub2(menuSub2: String): T

    fun withMenuSub2(@StringRes menuSub2Res: Int): T

    fun withMenuSub2(menuSub2: StringHolder): T

    fun withDescription(description: String): T

    fun withDescription(@StringRes descriptionRes: Int): T

    fun withIcon(icon: Drawable): T

    fun withIcon(bitmap: Bitmap): T

    fun withIcon(@DrawableRes iconRes: Int): T

    fun withIcon(url: String): T

    fun withIcon(uri: Uri): T

    fun withIcon(icon: IIcon): T

    fun withSelectable(selectable: Boolean): T
}
