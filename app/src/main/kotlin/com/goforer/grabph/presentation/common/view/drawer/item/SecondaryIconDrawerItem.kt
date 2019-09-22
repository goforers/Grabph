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

package com.goforer.grabph.presentation.common.view.drawer.item

import android.content.Context
import androidx.annotation.LayoutRes
import com.mikepenz.materialdrawer.R
import com.mikepenz.materialdrawer.holder.ColorHolder

open class SecondaryIconDrawerItem : PrimaryIconDrawerItem() {
    override fun getType(): Int {
        return com.mikepenz.materialdrawer.R.id.material_drawer_item_secondary
    }

    @LayoutRes
    override fun getLayoutRes(): Int {
        return com.mikepenz.materialdrawer.R.layout.material_drawer_item_secondary
    }

    /**
     * helper method to decide for the correct color
     * OVERWRITE to get the correct secondary color
     *
     * @param context
     * @return
     */
    override fun getColor(context: Context): Int {
        return if (isEnabled) {
            ColorHolder.color(getTextColor(), context,
                    R.attr.material_drawer_secondary_text,
                    R.color.material_drawer_secondary_text)
        } else {
            ColorHolder.color(getDisabledTextColor(),
                    context, R.attr.material_drawer_hint_text,
                    R.color.material_drawer_hint_text)
        }
    }
}
