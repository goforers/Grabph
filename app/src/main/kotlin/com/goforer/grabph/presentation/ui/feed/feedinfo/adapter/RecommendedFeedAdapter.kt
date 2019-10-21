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

package com.goforer.grabph.presentation.ui.feed.feedinfo.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.ui.feed.feedinfo.FeedInfoActivity
import com.goforer.grabph.data.datasource.model.cache.data.mock.entity.feed.FeedsContent
import kotlinx.android.synthetic.main.grid_feed_item.view.*

class RecommendedFeedAdapter(private val activity: FeedInfoActivity): RecyclerView.Adapter<RecommendedFeedAdapter.ViewHolder>() {
    private var item: List<FeedsContent.Content> = ArrayList()

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_feed_item, parent, false)

        return ViewHolder(activity, view!!)
    }

    override fun getItemCount(): Int {
        return item.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItemHolder(holder, item[position], position)
    }

    fun addItem(item: List<FeedsContent.Content>) {
        this.item = item
    }

    class ViewHolder(private val activity: FeedInfoActivity, val holderItemView: View): BaseViewHolder<Any>(holderItemView) {
        @SuppressLint("SetTextI18n")
        @Suppress("UNCHECKED_CAST")
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: Any, position: Int) {
            // In case of applying transition effect to views, have to use findViewById method
            val content = item as FeedsContent.Content

            with(holder.itemView) {
                activity.setFontTypeface(tv_feed_item_title, BaseActivity.FONT_TYPE_REGULAR)
                iv_feed_item_content.requestLayout()
                tv_feed_item_title.requestLayout()
                activity.setFixedImageSize(0, 0)
                activity.setImageDraw(iv_feed_item_content, feedConstraintLayoutContainer, content.media.urls.small!!, true)
                tv_feed_item_title.text = item.title
            }
        }

        override fun onItemSelected() {
        }

        override fun onItemClear() {
            holderItemView.setBackgroundColor(0)
        }
    }
}