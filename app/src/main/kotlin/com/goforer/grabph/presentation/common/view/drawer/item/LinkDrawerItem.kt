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

import androidx.annotation.LayoutRes
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.goforer.grabph.R
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.BaseDrawerItem
import com.mikepenz.materialdrawer.util.DrawerUIUtils
import com.mikepenz.materialize.util.UIUtils
import kotlinx.android.synthetic.main.drawer_item_link.view.*

open class LinkDrawerItem : BaseDrawerItem<LinkDrawerItem, LinkDrawerItem.ViewHolder>() {
    override fun getType(): Int {
        return R.id.material_drawer_item_link/*"PRIMARY_ITEM"*/
    }

    @LayoutRes
    override fun getLayoutRes(): Int {
        return R.layout.drawer_item_link
    }

    override fun bindView(viewHolder: LinkDrawerItem.ViewHolder, payloads: List<*>?) {
        super.bindView(viewHolder, payloads)

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
    private fun bindViewHelper(viewHolder: LinkDrawerItem.ViewHolder) {
        val ctx = viewHolder.itemView.context

        //set the identifier from the drawerItem here. It can be used to run tests
        viewHolder.itemView.id = hashCode()

        //set the item selected if it is
        viewHolder.itemView.isSelected = isSelected

        //set the item enabled if it is
        viewHolder.itemView.isEnabled = isEnabled

        //get the correct color for the background
        val selectedColor = getSelectedColor(ctx)
        //get the correct color for the text
        val color = getColor(ctx)
        val selectedTextColor = getTextColorStateList(color, getSelectedTextColor(ctx))
        //get the correct color for the icon
        val iconColor = getIconColor(ctx)
        val selectedIconColor = getSelectedIconColor(ctx)

        //set the background for the item
        UIUtils.setBackground(viewHolder.view, UIUtils.getSelectableBackground(ctx, selectedColor, true))
        //set the text for the name
        StringHolder.applyTo(this.getName(), viewHolder.title)

        //set the colors for textViews
        viewHolder.title.setTextColor(selectedTextColor)

        //define the typeface for our textViews
        if (getTypeface() != null) {
            viewHolder.title.typeface = getTypeface()
        }

        //get the drawables for our icon and set it
        val icon = ImageHolder.decideIcon(getIcon(), ctx, iconColor, isIconTinted, 1)
        if (icon != null) {
            val selectedIcon = ImageHolder.decideIcon(getSelectedIcon(), ctx, selectedIconColor, isIconTinted, 1)
            ImageHolder.applyMultiIconTo(icon, iconColor, selectedIcon, selectedIconColor, isIconTinted, viewHolder.icon)
        } else {
            ImageHolder.applyDecidedIconOrSetGone(getIcon(), viewHolder.icon, iconColor, isIconTinted, 1)
        }

        //for android API 17 --> Padding not applied via xml
        DrawerUIUtils.setDrawerVerticalPadding(viewHolder.view, getLevel())
    }

    override fun getViewHolder(v: View): LinkDrawerItem.ViewHolder {
        return LinkDrawerItem.ViewHolder(v)
    }

    open class ViewHolder(var view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        var icon: AppCompatImageView = view.drawer_link_icon
        var title: AppCompatTextView = view.drawer_link_title
    }
}
