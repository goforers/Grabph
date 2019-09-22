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

@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.goforer.grabph.presentation.common.view.drawer.item

import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.goforer.grabph.presentation.common.view.drawer.holder.IconBaseViewHolder
import com.mikepenz.materialdrawer.holder.BadgeStyle
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.interfaces.ColorfulBadgeable

open class PrimaryIconDrawerItem: BasePrimaryIconDrawerItem<PrimaryIconDrawerItem,
        PrimaryIconDrawerItem.ViewHolder>(), ColorfulBadgeable<PrimaryIconDrawerItem> {
    private var badge: StringHolder? = null
    private var badgeStyle = BadgeStyle()

    override fun withBadge(badge: StringHolder): PrimaryIconDrawerItem {
        this.badge = badge
        return this
    }

    override fun withBadge(badge: String): PrimaryIconDrawerItem {
        this.badge = StringHolder(badge)
        return this
    }

    override fun withBadge(@StringRes badgeRes: Int): PrimaryIconDrawerItem {
        this.badge = StringHolder(badgeRes)
        return this
    }

    override fun withBadgeStyle(badgeStyle: BadgeStyle): PrimaryIconDrawerItem {
        this.badgeStyle = badgeStyle
        return this
    }

    override fun getBadge(): StringHolder? {
        return badge
    }

    override fun getBadgeStyle(): BadgeStyle {
        return badgeStyle
    }

    override fun getType(): Int {
        return com.mikepenz.materialdrawer.R.id.material_drawer_item_primary/*"PRIMARY_ITEM"*/
    }

    @LayoutRes
    override fun getLayoutRes(): Int {
        return com.mikepenz.materialdrawer.R.layout.material_drawer_item_primary
    }

    override fun bindView(holder: PrimaryIconDrawerItem.ViewHolder, payloads: List<Any>?) {
        super.bindView(holder, payloads)

        val context = holder.itemView.context
        //bind the basic view parts
        bindViewHelper(holder)

        //set the text for the badge or hide
        val badgeVisible = StringHolder.applyToOrHide(badge, holder.badge)
        //style the badge if it is visible
        if (badgeVisible) {
            badgeStyle.style(holder.badge, getTextColorStateList(getColor(context),
                    getSelectedTextColor(context)))
            holder.badgeContainer.visibility = View.VISIBLE
        } else {
            holder.badgeContainer.visibility = View.GONE
        }

        //define the typeface for our textViews
        if (getTypeface() != null) {
            holder.badge.typeface = getTypeface()
        }

        //call the onPostBindView method to trigger post bind view actions
        // (like the listener to modify the item if required)
        onPostBindView(this, holder.itemView)
    }

    override fun getViewHolder(view: View): PrimaryIconDrawerItem.ViewHolder {
        return PrimaryIconDrawerItem.ViewHolder(view)
    }

    class ViewHolder(view: View) : IconBaseViewHolder(view) {
        val badgeContainer: View = view.findViewById(
                com.mikepenz.materialdrawer.R.id.material_drawer_badge_container)
        val badge: AppCompatTextView = view.findViewById<View>(
                com.mikepenz.materialdrawer.R.id.material_drawer_badge) as AppCompatTextView
    }
}