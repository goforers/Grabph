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

import android.graphics.Color
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.goforer.base.presentation.utils.CommonUtils.convertDateToString
import com.goforer.base.presentation.view.adatper.BaseListAdapter
import com.goforer.base.presentation.view.fragment.RecyclerFragment
import com.goforer.base.presentation.view.helper.ItemTouchHelperListener
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.base.presentation.view.holder.DefaultViewHolder
import com.goforer.grabph.R
import com.goforer.grabph.presentation.event.action.DeleteKeywordAction
import com.goforer.grabph.presentation.ui.search.FeedSearchActivity
import com.goforer.grabph.repository.model.cache.data.entity.search.RecentKeyword
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_search_filter.*
import kotlinx.android.synthetic.main.view_search_filter.view.*
import org.greenrobot.eventbus.EventBus
import java.util.*

class RecentKeywordAdapter(fragment: RecyclerFragment<RecentKeyword>):
        BaseListAdapter<RecentKeyword>(R.layout.view_search_filter, fragment, DIFF_CALLBACK),
        ItemTouchHelperListener {
    private val fragment: RecyclerFragment<*>

    private lateinit var keywordItems: List<RecentKeyword>

    init {
        this.fragment = fragment
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RecentKeyword>() {
            override fun areItemsTheSame(
                    oldItem: RecentKeyword, newItem: RecentKeyword): Boolean {
                // User properties may have changed if reloaded from the DB, but ID is fixed
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                    oldItem: RecentKeyword, newItem: RecentKeyword): Boolean {
                // NOTE: if you use equals, your object must properly override Object#equals()
                // Incorrectly returning false here will result in too many animations.
                return oldItem == newItem
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): BaseViewHolder<*> {
        val view: View

        return when (type) {
            VIEW_TYPE_LOADING -> {
                view = LayoutInflater.from(parent.context.applicationContext).inflate(
                                            R.layout.list_loading_item, parent, false)
                DefaultViewHolder(view)
            }
            else -> super.onCreateViewHolder(parent, type)
        }
    }

    override fun createViewHolder(viewGroup: ViewGroup, view: View, type: Int): BaseViewHolder<*> {
        return SearchKeywordHolder(view, fragment)
    }

    override fun onBindViewHolder(viewHolder: BaseViewHolder<*>, position: Int,
                                  payloads: List<Any>) {
        val safePosition = viewHolder.adapterPosition

        when (getItemViewType(safePosition)) {
            VIEW_TYPE_LOADING -> return
            else -> if (payloads.isEmpty()) {
                super.onBindViewHolder(viewHolder, safePosition)
            } else {
                // TO DO:: Implement some code if we detect small changes we want to propagate
                // to the UI without totally rebinding the concerned item view.

            }
        }
    }

    override fun areItemsTheSame(oldItem: RecentKeyword, newItem: RecentKeyword): Boolean {
        return oldItem.id == newItem.id && oldItem.keyword == newItem.keyword
    }

    override fun areContentsTheSame(oldItem: RecentKeyword, newItem: RecentKeyword): Boolean {
        return oldItem.searchedDate == newItem.searchedDate && oldItem.keyword == newItem.keyword
    }

    override fun dispatchUpdates(diffResult: DiffUtil.DiffResult) {
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onAddItems(items: List<RecentKeyword>?) {
        items?.let {
            keywordItems = items
        }

        fragment.stopLoading(RecyclerFragment.STOP_ADDING_TIMEOUT)
    }

    override fun onUpdateItems(items: List<RecentKeyword>?) {
        items?.let {
            keywordItems = items

        }

        fragment.stopLoading(RecyclerFragment.STOP_REFRESHING_TIMEOUT)
    }

    override fun onItemDismiss(position: Int) {
        val action = DeleteKeywordAction()

        action.keyword = items!![position].keyword
        EventBus.getDefault().post(action)

        items!!.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, items!!.size)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(items!!, i, i + 1)
            }

            notifyItemMoved(fromPosition, toPosition)
            notifyItemRangeChanged(fromPosition, toPosition)
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(items!!, i, i - 1)
            }

            notifyItemMoved(toPosition, fromPosition)
            notifyItemRangeChanged(toPosition, fromPosition)
        }

        return true
    }

    override fun onItemDrag(actionState: Int) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            fragment.swipe_layout.isRefreshing = false
            fragment.swipe_layout.isEnabled = false
        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            fragment.swipe_layout.isEnabled = true
        }
    }

    fun setEnableLoadingImage(usedLoadingImage: Boolean) {
        setUsedLoadingImage(usedLoadingImage)
    }

    override fun getItems(): MutableList<RecentKeyword>? {
        return keywordItems as MutableList<RecentKeyword>?
    }

    class SearchKeywordHolder internal constructor(override val containerView: View,
                                                   private val fragment: RecyclerFragment<*>):
                                                                BaseViewHolder<RecentKeyword>(containerView), LayoutContainer {

        override fun bindItemHolder(holder: BaseViewHolder<*>, item: RecentKeyword, position: Int) {
            // In case of applying transition effect to views, have to use findViewById method
            with (holder.itemView) {
                filter_keyword.setOnClickListener {
                    (fragment.baseActivity as FeedSearchActivity)
                            .doTransactFragment(item.keyword)
                }

                et_keyword.text = item.keyword
                et_date.text = convertDateToString(item.searchedDate)
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
