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

@file:Suppress("UNCHECKED_CAST")

package com.goforer.grabph.presentation.common.view.drawer.item

import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.goforer.grabph.presentation.common.view.drawer.holder.PanelViewHolder
import com.mikepenz.materialdrawer.holder.ColorHolder
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.BaseDrawerItem
import com.mikepenz.materialdrawer.util.DrawerUIUtils
import com.mikepenz.materialdrawer.util.DrawerUIUtils.themeDrawerItem

@SuppressWarnings("unchecked")
abstract class PanelDrawerItem<T, VH : PanelViewHolder> : BaseDrawerItem<T, VH>() {
    private var caption: StringHolder? = null

    private var captionTextColor: ColorHolder? = null

    private var captionBackgroundColorRes: Int = 0

    fun withCaption(count: String): T {
        this.caption = StringHolder(count)
        return this as T
    }

    fun withCaption(@StringRes countRes: Int): T {
        this.caption = StringHolder(countRes)
        return this as T
    }

    fun withCaptionTextColor(@ColorInt color: Int): T {
        this.captionTextColor = ColorHolder.fromColor(color)
        return this as T
    }

    fun withCaptionTextColorRes(@ColorRes colorRes: Int): T {
        this.captionTextColor = ColorHolder.fromColorRes(colorRes)
        return this as T
    }

    fun withCaptionBackgroundColor(@ColorRes colorRes: Int): T {
        this.captionBackgroundColorRes = colorRes
        return this as T
    }

    /**
     * a helper method to have the logic for all secondaryDrawerItems only once
     *
     * @param viewHolder A ViewHolder describes an item view and metadata about its place
     * within the RecyclerView.
     */
    fun bindViewHelper(viewHolder: PanelViewHolder) {
        val context = viewHolder.itemView.context

        //set the identifier from the drawerItem here. It can be used to run tests
        viewHolder.itemView.id = hashCode()

        //set the item selected if it is
        viewHolder.itemView.isSelected = isSelected

        //set the item enabled if it is
        viewHolder.itemView.isEnabled = isEnabled

        //
        viewHolder.itemView.tag = this

        //get the correct color for the background
        val selectedColor = getSelectedColor(context)
        //get the correct color for the text
        val color = getColor(context)
        val selectedTextColor = getTextColorStateList(color,
                getSelectedTextColor(context))
        //get the correct color for the icon
        val iconColor = getIconColor(context)
        val selectedIconColor = getSelectedIconColor(context)

        //set the background for the item
        themeDrawerItem(context, viewHolder.getView(), selectedColor, isSelectedBackgroundAnimated)

        viewHolder.caption.setBackgroundResource(captionBackgroundColorRes)

        //set the text for the name
        StringHolder.applyTo(this.getName(), viewHolder.getName())
        //set the text for the count or hide
        StringHolder.applyToOrHide(this.caption, viewHolder.caption)

        //set the colors for textViews
        viewHolder.getName().setTextColor(selectedTextColor)

        //set the caption text color
        ColorHolder.applyToOr(captionTextColor, viewHolder.caption, selectedTextColor)

        //define the typeface for our textViews
        getTypeface()?.let {
            viewHolder.getName().typeface = getTypeface()
            viewHolder.getDescription().typeface = getTypeface()
            viewHolder.caption.typeface = getTypeface()
        }

        //get the drawables for our icon and set it
        val icon = ImageHolder.decideIcon(getIcon(), context, iconColor, isIconTinted, 1)
        val selectedIcon = ImageHolder.decideIcon(getSelectedIcon(), context, selectedIconColor,
                isIconTinted, 1)
        ImageHolder.applyMultiIconTo(icon, iconColor, selectedIcon, selectedIconColor,
                isIconTinted, viewHolder.getIcon())

        //for android API 17 --> Padding not applied via xml
        DrawerUIUtils.setDrawerVerticalPadding(viewHolder.getView(), level)
    }
}
