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

@file:Suppress("DEPRECATION")

package com.goforer.grabph.presentation.common.view.drawer.item

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.goforer.grabph.R
import com.goforer.grabph.presentation.common.view.drawer.holder.PanelViewHolder
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.holder.ColorHolder
import com.mikepenz.materialdrawer.icons.MaterialDrawerFont
import com.mikepenz.materialdrawer.model.AbstractDrawerItem
import kotlinx.android.synthetic.main.drawer_item_caption_panel.view.*

open class CustomPanelDrawableItem : PanelDrawerItem<CustomPanelDrawableItem, CustomPanelDrawableItem.ViewHolder>() {
    private lateinit var onCustomCountPanelDrawerItemClickListener: Drawer.OnDrawerItemClickListener

    private var arrowVisible = true

    private var arrowColor: ColorHolder? = null

    private var arrowRotationAngleStart = 0

    private var arrowRotationAngleEnd = 180

    /**
     * our internal onDrawerItemClickListener which will handle the arrow animation
     */
    private val mOnArrowDrawerItemClickListener = Drawer.OnDrawerItemClickListener { view, position, drawerItem ->
        if (drawerItem is AbstractDrawerItem<*, *> && drawerItem.isEnabled()) {
            if (drawerItem.subItems != null) {
                if (drawerItem.isExpanded()) {
                    ViewCompat.animate(view.findViewById(
                            com.mikepenz.materialdrawer.R.id.material_drawer_arrow))
                            .rotation(180f).start()
                } else {
                    ViewCompat.animate(view.findViewById(
                            com.mikepenz.materialdrawer.R.id.material_drawer_arrow))
                            .rotation(0f).start()
                }
            }
        }

        onCustomCountPanelDrawerItemClickListener.onItemClick(
                view, position, drawerItem)
    }

    fun withArrowVisible(visible: Boolean): CustomPanelDrawableItem {
        this.arrowVisible = visible
        return this
    }

    fun withArrowColor(@ColorInt arrowColor: Int): CustomPanelDrawableItem {
        this.arrowColor = ColorHolder.fromColor(arrowColor)
        return this
    }

    fun withArrowColorRes(@ColorRes arrowColorRes: Int): CustomPanelDrawableItem {
        this.arrowColor = ColorHolder.fromColorRes(arrowColorRes)
        return this
    }

    fun withArrowRotationAngleStart(angle: Int): CustomPanelDrawableItem {
        this.arrowRotationAngleStart = angle
        return this
    }

    fun withArrowRotationAngleEnd(angle: Int): CustomPanelDrawableItem {
        this.arrowRotationAngleEnd = angle
        return this
    }

    override fun getType(): Int {
        return com.mikepenz.materialdrawer.R.id.material_drawer_item_expandable
    }

    @LayoutRes
    override fun getLayoutRes(): Int {
        return R.layout.drawer_item_caption_panel
    }

    override fun withOnDrawerItemClickListener(onDrawerItemClickListener: Drawer.OnDrawerItemClickListener):
            CustomPanelDrawableItem {
        onCustomCountPanelDrawerItemClickListener = onDrawerItemClickListener
        return this
    }

    override fun getOnDrawerItemClickListener(): Drawer.OnDrawerItemClickListener {
        return mOnArrowDrawerItemClickListener
    }

    override fun bindView(holder: ViewHolder, payloads: List<Any>?) {
        super.bindView(holder, payloads)

        val context = holder.itemView.context
        //bind the basic view parts
        bindViewHelper(holder)

        if (this.arrowVisible) {
            holder.arrow.visibility = View.VISIBLE
        } else {
            holder.arrow.visibility = View.GONE
        }

        //make sure all animations are stopped
        if (holder.arrow.drawable is IconicsDrawable) {
            (holder.arrow.drawable as IconicsDrawable).color(if (this.arrowColor != null) {
                this.arrowColor!!.color(context)
            } else {
                getIconColor(context)
            })
        }
        holder.arrow.clearAnimation()
        if (!isExpanded) {
            holder.arrow.rotation = this.arrowRotationAngleStart.toFloat()
        } else {
            holder.arrow.rotation = this.arrowRotationAngleEnd.toFloat()
        }

        //call the onPostBindView method to trigger post bind view actions
        // (like the listener to modify the item if required)
        onPostBindView(this, holder.itemView)
    }

    override fun getViewHolder(view: View): ViewHolder {
        return ViewHolder(view)
    }

    class ViewHolder(view: View) : PanelViewHolder(view) {
        internal var arrow: AppCompatImageView = view.material_drawer_arrow

        init {
            arrow.setImageDrawable(IconicsDrawable(view.context,
                    MaterialDrawerFont.Icon.mdf_expand_more).sizeDp(16).paddingDp(2).color(Color.BLACK))
        }
    }
}
