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

package com.goforer.grabph.presentation.ui.search.adapter

import android.annotation.SuppressLint
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import android.graphics.Color
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.goforer.base.domain.common.GeneralFunctions
import com.goforer.base.presentation.view.helper.ItemTouchHelperListener
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_SEARCHED_FEED
import com.goforer.grabph.presentation.caller.Caller.SELECTED_SEARCH_ITEM_POSITION
import com.goforer.grabph.presentation.ui.search.fragment.FeedSearchFragment
import com.goforer.grabph.repository.model.cache.data.entity.feed.FeedItem
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_feed_search.*
import kotlinx.android.synthetic.main.grid_feed_item.view.*

class FeedSearchAdapter(private val fragment: FeedSearchFragment):
        PagedListAdapter<FeedItem, FeedSearchAdapter.FeedViewHolder>(DIFF_CALLBACK), ItemTouchHelperListener {
    companion object {
        private const val TRANSITION_NAME_FOR_IMAGE = "Image "
        private const val TRANSITION_NAME_FOR_TITLE = "Title "

        private val PAYLOAD_TITLE = Any()

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FeedItem>() {
            override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
                // User properties may have changed if reloaded from the DB, but ID is fixed
                return oldItem.media.m == newItem.media.m
            }

            override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
                // NOTE: if you use equals, your object must properly override Object#equals()
                // Incorrectly returning false here will result in too many animations.
                return oldItem == newItem
            }

            override fun getChangePayload(oldFeedItem: FeedItem, newFeedItem: FeedItem): Any? {
                return if (sameExceptTitle(oldFeedItem, newFeedItem)) {
                    PAYLOAD_TITLE
                } else {
                    null
                }
            }
        }

        private fun sameExceptTitle(oldFeedItem: FeedItem, newFeedItem: FeedItem): Boolean {
            return oldFeedItem.copy(title = newFeedItem.title) == newFeedItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context.applicationContext)
        val view = layoutInflater.inflate(R.layout.grid_feed_item, parent, false)

        return FeedViewHolder(view, fragment)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val item = getItem(position)

        item?.let {
            holder.bindItemHolder(holder, it, position)
        }
    }

    override fun onItemDismiss(position: Int) {
        val pagedList = currentList

        pagedList?.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, currentList!!.size)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        // PageList does not support the method Collections.swap()
        /*
        PagedList<FeedItem> pagedList = getCurrentList();
        if (pagedList == null) {
            return false;
        }

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(pagedList, i, i + 1);
            }

            notifyItemMoved(fromPosition, toPosition);
            notifyItemRangeChanged(fromPosition, toPosition);
        } else {
            for (int i = fromPosition; i > toPosition; i--)
                Collections.swap(pagedList, i, i - 1);

            notifyItemMoved(toPosition, fromPosition);
            notifyItemRangeChanged(toPosition, fromPosition);
        }

        return true;
        */

        return false
    }

    override fun onItemDrag(actionState: Int) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            fragment.swipe_layout.isRefreshing = false
            fragment.swipe_layout.isEnabled = false
        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            fragment.swipe_layout.isEnabled = true
        }
    }

    override fun onCurrentListChanged(previousList: PagedList<FeedItem>?, currentList: PagedList<FeedItem>?) {
        fragment.stopRefreshing(true)
    }

    class FeedViewHolder internal constructor(override val containerView: View, private val fragment: FeedSearchFragment): BaseViewHolder<FeedItem>(containerView), LayoutContainer {
        @SuppressLint("SetTextI18n")
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: FeedItem, position: Int) {
            // In case of applying transition effect to views, have to use findViewById method
            with (holder.itemView) {
                iv_feed_item_content.requestLayout()
                tv_feed_item_title.requestLayout()
                iv_feed_item_content.transitionName = TRANSITION_NAME_FOR_IMAGE + holder.adapterPosition
                tv_feed_item_title.transitionName = TRANSITION_NAME_FOR_TITLE + holder.adapterPosition
                fragment.feedSearchActivity.setFixedImageSize(0, 0)
                fragment.feedSearchActivity.setImageDraw(iv_feed_item_content, feedConstraintLayoutContainer, item.media.m!!, false)
                tv_feed_item_title.text = item.title
                holder.view.setOnClickListener {
                    val link = GeneralFunctions.removeLastCharRegex(item.link.toString()).toString()
                    val photoId = link.substring(link.lastIndexOf("/") + 1)
                    Caller.callFeedInfo(fragment.feedSearchActivity, iv_feed_item_content,
                            tv_feed_item_title, item.idx, holder.adapterPosition,
                            item.authorId, photoId, CALLED_FROM_SEARCHED_FEED, SELECTED_SEARCH_ITEM_POSITION)
                }

                feed_item_holder?.visibility = View.VISIBLE
            }

            if (fragment.swipe_layout.isRefreshing) {
                fragment.stopRefreshing(true)
            }
        }

        override fun onItemSelected() {
            containerView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            containerView.setBackgroundColor(0)
        }
    }
}
