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

import android.net.Uri
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.goforer.grabph.presentation.common.view.drawer.loader.SlidingDrawerImageLoader
import com.mikepenz.materialdrawer.holder.ColorHolder
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.BaseDrawerItem
import com.mikepenz.materialdrawer.model.BaseViewHolder
import com.mikepenz.materialdrawer.util.DrawerUIUtils
import com.mikepenz.materialdrawer.util.DrawerUIUtils.themeDrawerItem

@SuppressWarnings("unchecked")
abstract class BasePrimaryIconDrawerItem<T, VH : BaseViewHolder> : BaseDrawerItem<T, VH>() {
    private var description: StringHolder? = null

    private var descriptionTextColor: ColorHolder? = null

    fun withIcon(url: String): T {
        this.icon = ImageHolder(url)
        return this as T
    }

    fun withIcon(uri: Uri): T {
        this.icon = ImageHolder(uri)
        return this as T
    }

    fun withDescription(description: String): T {
        this.description = StringHolder(description)
        return this as T
    }

    fun withDescription(@StringRes descriptionRes: Int): T {
        this.description = StringHolder(descriptionRes)
        return this as T
    }

    fun withDescriptionTextColor(@ColorInt color: Int): T {
        this.descriptionTextColor = ColorHolder.fromColor(color)
        return this as T
    }

    fun withDescriptionTextColorRes(@ColorRes colorRes: Int): T {
        this.descriptionTextColor = ColorHolder.fromColorRes(colorRes)
        return this as T
    }

    override fun getIcon(): ImageHolder {
        return icon
    }

    /**
     * a helper method to have the logic for all secondaryDrawerItems only once
     *
     * @param viewHolder A ViewHolder describes an item view and metadata about its place
     * within the RecyclerView.
     */
    fun bindViewHelper(viewHolder: PrimaryIconDrawerItem.ViewHolder) {
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

        //set the text for the name
        StringHolder.applyTo(this.getName(), viewHolder.getName())
        //set the text for the description or hide
        StringHolder.applyToOrHide(this.description, viewHolder.getDescription())

        //set the colors for textViews
        viewHolder.getName().setTextColor(selectedTextColor)
        //set the description text color
        ColorHolder.applyToOr(descriptionTextColor, viewHolder.getDescription(),
                selectedTextColor)

        //define the typeface for our textViews
        getTypeface()?.let {
            viewHolder.getName().typeface = getTypeface()
            viewHolder.getDescription().typeface = getTypeface()
            viewHolder.getDescription().setLines(4)
        }

        //get the drawables for our icon and set it
        SlidingDrawerImageLoader.instance.cancelImage(viewHolder.getIcon())
        //set the placeholder
        viewHolder.getIcon().setImageDrawable(SlidingDrawerImageLoader.instance
                .imageLoader!!.placeholder(viewHolder.getIcon().context,
                SlidingDrawerImageLoader.Tags.PRIMARY_ICON.name))
        //set the icon
        ImageHolder.applyTo(getIcon(), viewHolder.getIcon(),
                SlidingDrawerImageLoader.Tags.PRIMARY_ICON.name)

        //for android API 17 --> Padding not applied via xml
        DrawerUIUtils.setDrawerVerticalPadding(viewHolder.getView(), getLevel())
    }
}
