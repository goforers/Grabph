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

import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.goforer.grabph.R
import com.mikepenz.materialdrawer.holder.ColorHolder
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.BaseDrawerItem
import com.mikepenz.materialdrawer.util.DrawerUIUtils
import com.mikepenz.materialdrawer.util.DrawerUIUtils.themeDrawerItem
import kotlinx.android.synthetic.main.drawer_item_advanced_primary.view.*

open class AdvancePrimaryDrawerItem : BaseDrawerItem<AdvancePrimaryDrawerItem, AdvancePrimaryDrawerItem.ViewHolder>() {
    private var description: StringHolder? = null

    private var descriptionTextColor: ColorHolder? = null

    private var line: Int = 0
    private var maxLines: Int = 0

    fun withDescription(description: String): AdvancePrimaryDrawerItem {
        this.description = StringHolder(description)
        return this
    }

    fun withDescription(@StringRes descriptionRes: Int): AdvancePrimaryDrawerItem {
        this.description = StringHolder(descriptionRes)
        return this
    }

    fun withDescriptionTextColor(@ColorInt color: Int): AdvancePrimaryDrawerItem {
        this.descriptionTextColor = ColorHolder.fromColor(color)
        return this
    }

    fun withDescriptionTextColorRes(@ColorRes colorRes: Int): AdvancePrimaryDrawerItem {
        this.descriptionTextColor = ColorHolder.fromColorRes(colorRes)
        return this
    }

    fun withLine(line: Int): AdvancePrimaryDrawerItem {
        this.line = line

        return this
    }

    fun withMaxLines(maxLines: Int): AdvancePrimaryDrawerItem {
        this.maxLines = maxLines

        return this
    }

    override fun getType(): Int {
        return R.id.material_drawer_item_base_primary/*"PRIMARY_ITEM"*/
    }

    @LayoutRes
    override fun getLayoutRes(): Int {
        return R.layout.drawer_item_advanced_primary
    }

    override fun bindView(viewHolder: ViewHolder, payloads: List<*>?) {
        super.bindView(viewHolder, payloads)

        viewHolder.description.setLines(this.line)
        viewHolder.description.maxLines = this.maxLines

        //bind the basic view parts
        bindViewHelper(viewHolder)

        //call the onPostBindView method to trigger post bind view actions (like the listener to modify the item if required)
        onPostBindView(this, viewHolder.itemView)
    }

    /**
     * a helper method to have the logic for all secondaryDrawerItems only once
     *
     * @param viewHolder
     */
    private fun bindViewHelper(viewHolder: ViewHolder) {
        val context = viewHolder.itemView.context

        //set the identifier from the drawerItem here. It can be used to run tests
        viewHolder.itemView.id = hashCode()

        //set the item selected if it is
        viewHolder.itemView.isSelected = isSelected

        //set the item enabled if it is
        viewHolder.itemView.isEnabled = isEnabled

        //get the correct color for the background
        val selectedColor = getSelectedColor(context)
        //get the correct color for the text
        val color = getColor(context)
        val selectedTextColor = getTextColorStateList(color, getSelectedTextColor(context))
        //get the correct color for the icon
        val iconColor = getIconColor(context)
        val selectedIconColor = getSelectedIconColor(context)

        //set the background for the item
        themeDrawerItem(context, viewHolder.view, selectedColor, isSelectedBackgroundAnimated)

        //set the text for the name
        StringHolder.applyTo(this.getName(), viewHolder.name)
        //set the text for the description or hide
        StringHolder.applyToOrHide(this.description, viewHolder.description)

        //set the colors for textViews
        viewHolder.name.setTextColor(selectedTextColor)
        //set the description text color
        ColorHolder.applyToOr(descriptionTextColor, viewHolder.description, selectedTextColor)

        //define the typeface for our textViews
        if (getTypeface() != null) {
            viewHolder.name.typeface = getTypeface()
            viewHolder.description.typeface = getTypeface()
        }

        //get the drawables for our icon and set it
        val icon = ImageHolder.decideIcon(getIcon(), context, iconColor, isIconTinted, 1)
        if (icon != null) {
            val selectedIcon = ImageHolder.decideIcon(getSelectedIcon(), context, selectedIconColor, isIconTinted, 1)
            ImageHolder.applyMultiIconTo(icon, iconColor, selectedIcon, selectedIconColor, isIconTinted, viewHolder.icon)
        } else {
            ImageHolder.applyDecidedIconOrSetGone(getIcon(), viewHolder.icon, iconColor, isIconTinted, 1)
        }

        //for android API 17 --> Padding not applied via xml
        DrawerUIUtils.setDrawerVerticalPadding(viewHolder.view, level)
    }

    override fun getViewHolder(view: View): ViewHolder {
        return ViewHolder(view)
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var icon: AppCompatImageView = view.drawer_icon
        var name: AppCompatTextView = view.drawer_name
        var description: AppCompatTextView = view.drawer_description
    }
}
