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

package com.goforer.grabph.presentation.ui.hottopic.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_REGULAR
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.ui.hottopic.HotTopicContentActivity
import com.goforer.grabph.repository.model.cache.data.entity.hottopic.HotTopicContent.Content
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_hot_topic_content_item.*

class HotTopicContentAdapter(private val activity: HotTopicContentActivity): RecyclerView.Adapter<HotTopicContentAdapter.ViewHolder>() {
    private var item: List<Content> = ArrayList()

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_hot_topic_content_item, parent, false)

        return ViewHolder(view, activity)
    }

    override fun getItemCount(): Int {
        return item.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItemHolder(holder, item[position], position)
    }

    fun addItem(item: List<Content>) {
        this.item = item
    }

    class ViewHolder(override val containerView: View, private val activity: HotTopicContentActivity): BaseViewHolder<Any>(containerView), LayoutContainer {
        @SuppressLint("SetTextI18n")
        @Suppress("UNCHECKED_CAST")
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: Any, position: Int) {
            // In case of applying transition effect to views, have to use findViewById method
            val content = item as Content

            activity.setFontTypeface(tv_topic_description, FONT_TYPE_REGULAR)
            iv_topic_content.requestLayout()
            tv_topic_description.requestLayout()
            activity.setFixedImageSize(0, 0)
            activity.setImageDraw(iv_topic_content, hot_topic_constraintLayoutContainer, content.media.urls.regular!!, false)
            tv_topic_description.text = item.description
        }

        override fun onItemSelected() {
        }

        override fun onItemClear() {
            containerView.setBackgroundColor(0)
        }
    }
}