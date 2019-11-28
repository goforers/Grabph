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

package com.goforer.grabph.presentation.ui.home.feed.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import com.goforer.base.domain.common.GeneralFunctions
import com.goforer.base.presentation.utils.CommonUtils
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_REGULAR
import com.goforer.base.presentation.view.helper.ItemTouchHelperListener
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_FEED
import com.goforer.grabph.presentation.caller.Caller.SELECTED_FEED_ITEM_POSITION
import com.goforer.grabph.presentation.common.effect.transition.TransitionObject
import com.goforer.grabph.presentation.ui.home.feed.fragment.HomeFeedFragment
import com.goforer.grabph.data.datasource.model.cache.data.entity.feed.FeedItem
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.grid_feed_item.*
import kotlinx.android.synthetic.main.recycler_view_container.*
import kotlinx.coroutines.delay

class HomeFeedAdapter(private val fragment: HomeFeedFragment): PagedListAdapter<FeedItem, HomeFeedAdapter.HomeFeedHolder>(DIFF_CALLBACK),
    ItemTouchHelperListener {
    private var currentHolderPosition: Int = -1

    private lateinit var holderView: View

    companion object {
        private const val DELAY_TIMER_INTERVAL = 10

        private val PAYLOAD_TITLE = Any()

        private val DIFF_CALLBACK = object: DiffUtil.ItemCallback<FeedItem>() {
            override fun areItemsTheSame(oldFeedItem: FeedItem, newFeedItem: FeedItem): Boolean =
                oldFeedItem.media.m ==  newFeedItem.media.m

            override fun areContentsTheSame(oldFeedItem: FeedItem, newFeedItem: FeedItem): Boolean =
                oldFeedItem ==  newFeedItem

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeFeedHolder {
        val view = LayoutInflater.from(parent.context.applicationContext)
            .inflate(R.layout.grid_feed_item, parent, false)

        return HomeFeedHolder(view, fragment, this)
    }

    override fun onBindViewHolder(holder: HomeFeedHolder, position: Int) {
        val item = getItem(position)

        item?.let {
            holder.bindItemHolder(holder, it, position)
        }
    }

    override fun onCurrentListChanged(previousList: PagedList<FeedItem>?, currentList: PagedList<FeedItem>?) {
        fragment.stopRefreshing(true)
        fragment.launchWork {
            delay(DELAY_TIMER_INTERVAL.toLong())
            if (!fragment.isFeedUpdated) {
                fragment.recycler_view.post {
                    CommonUtils.betterSmoothScrollToPosition(fragment.recycler_view, 0)
                }
            } else {
                fragment.isFeedUpdated = false
            }
        }
    }

    override fun onItemDrag(actionState: Int) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            fragment.swipe_layout.isRefreshing = false
            fragment.swipe_layout.isEnabled = false
        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            fragment.swipe_layout.isEnabled = true
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        // PageList does not support the method Collections.swap()
        // Let's try to handle it if PageList does support method Collections.swap()
        /*
        val pagedList = currentList ?: return false

        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(pagedList, i, i + 1)
            }

            notifyItemMoved(fromPosition, toPosition)
            notifyItemRangeChanged(fromPosition, toPosition)
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(pagedList, i, i - 1)
            }

            notifyItemMoved(toPosition, fromPosition)
            notifyItemRangeChanged(toPosition, fromPosition)
        }

        return true
        */

        return false
    }

    override fun onItemDismiss(position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    internal fun setCurrentHolderPosition(position: Int) {
        currentHolderPosition = position
    }

    internal fun setHolderView(view: View) {
        holderView = view
    }

    @Suppress("NAME_SHADOWING", "DEPRECATION")
    class HomeFeedHolder(override val containerView: View, private val fragment: HomeFeedFragment,
        private val adapter: HomeFeedAdapter): BaseViewHolder<FeedItem>(containerView), LayoutContainer {
        @SuppressLint("SetTextI18n")
        override fun bindItemHolder(holder: BaseViewHolder<*>, item: FeedItem, position: Int) {
            val photoPath = if (item.mediaType == "photo") {
                item.media.m?.substring(0, item.media.m!!.indexOf("_m")) + ".jpg"
            } else {
                item.media.m!!
            }

            // In case of applying transition effect to views, have to use findViewById method
            fragment.homeActivity.setFontTypeface(tv_feed_item_title, FONT_TYPE_REGULAR)
            iv_play_btn.requestLayout()
            iv_feed_item_content.requestLayout()
            tv_feed_item_title.requestLayout()
            iv_feed_item_content.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + position
            fragment.homeActivity.setFixedImageSize(0, 0)
            fragment.homeActivity.setImageDraw(iv_feed_item_content, feedConstraintLayoutContainer, photoPath, true)
            if (item.title == "" || item.title == " ") {
                item.title = containerView.resources.getString(R.string.no_title)
            }

            when (item.mediaType) {
                "video" -> {
                    iv_play_btn.visibility = View.VISIBLE
                }
                "photo" -> {
                    iv_play_btn.visibility = View.GONE
                }
                else -> {
                    iv_play_btn.visibility = View.VISIBLE
                }
            }

            tv_feed_item_title.text = item.title
            tv_feed_item_title.visibility = View.VISIBLE
            iv_feed_item_content.setOnClickListener {
                adapter.setHolderView(holder.itemView)
                adapter.setCurrentHolderPosition(holder.adapterPosition)
                val link = GeneralFunctions.removeLastCharRegex(item.link.toString()).toString()
                val photoId = link.substring(link.lastIndexOf("/") + 1)
                Caller.callFeedInfo(fragment.homeActivity, iv_feed_item_content, item.idx, holder.adapterPosition,
                    item.authorId, photoId, CALLED_FROM_FEED, SELECTED_FEED_ITEM_POSITION)
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