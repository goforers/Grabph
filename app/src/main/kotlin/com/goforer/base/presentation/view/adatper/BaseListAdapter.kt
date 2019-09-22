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

@file:Suppress("NAME_SHADOWING", "KDocUnresolvedReference")

package com.goforer.base.presentation.view.adatper

import android.os.AsyncTask
import androidx.annotation.MainThread
import androidx.recyclerview.widget.DiffUtil
import com.goforer.base.presentation.view.fragment.RecyclerFragment
import com.goforer.base.presentation.view.holder.BaseViewHolder
import com.goforer.base.presentation.view.holder.ItemHolderBinder
import java.lang.ref.WeakReference
import kotlin.collections.ArrayList

abstract class BaseListAdapter<T>(layoutResId: Int, private val mFragment: RecyclerFragment<T>,
                                  DIFF_CALLBACK: DiffUtil.ItemCallback<T>):
                                                        BaseAdapter<T>(layoutResId, DIFF_CALLBACK) {
    /**
     * Check if the item is reached to the last.
     *
     * @return true if the item or page is reached to the last
     */
    /**
     * Set true if the item is reached to the last.
     *
     * @param isReachedToLast true if the item is reached to the last
     */
    var isReachedToLastItem = false
    /**
     * Check if the page is reached to the last.
     *
     * @return true if the item or page is reached to the last
     */
    /**
     * Set true if the page is reached to the last and notify any registered observers that the item
     * reflected at last item in last page has been newly inserted for last footer.
     *
     * @param isReachedToLast true if the page is reached to the last
     */
    var isReachedToLastPage = false
        set(isReachedToLast) {
            field = isReachedToLast
            if (isReachedToLast) {
                isReachedToLastItem = false
                notifyItemInserted(items?.size?:0)
            }
        }
    /**
     * Check if the items is empty.
     *
     * @return true if the items is empty
     */
    /**
     * Set true if the items is empty.
     *
     * @param isEmptyItems true if the items is empty
     */
    var isEmptyItems = false
    /**
     * Check if the items is loading.
     *
     * @return true if the items is loading
     */
    /**
     * Set true if the items is loading.
     *
     * @param isLoadingItems true if the items is loading
     */
    var isLoadingItems = false
    private var usedLoadingImage = false

    internal var items: MutableList<T>? = ArrayList()


    var lastItems: List<T>? = null
        private set

    // each time data is set, we update this variable so that if DiffUtil calculation returns
    // after repetitive updates, we can ignore the old calculation
    private var dataVersion = 0

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        if (position >= items?.size!!) { //if (items != null) items!!.size else 0)) {
            return
        }

        val item = items!![position] //if (items != null) items!![position] else null
        (holder as ItemHolderBinder<T>).bindItemHolder(holder, item, position)
    }

    @MainThread
    fun addItem(item: T) {
        val items = ArrayList<T>()
        items += item
        putItems(items, TYPE_FOR_ONE_ITEM)
    }

    @MainThread
    fun addItems(items: MutableList<T>, type: Int, isSorted: Boolean) {
        if (isSorted) {
            this.items = items
            notifyDataSetChanged()

            return
        }

        putItems(items, type)
    }

    fun updateItems(items: MutableList<T>, isSorted: Boolean) {
        if (isSorted) {
            this.items = items
            notifyDataSetChanged()

            return
        }

        update(items)
    }

    /**
     * Delete an item selected on the list of downloaded photo
     */
    fun deleteItem(position: Int) {
        items!!.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, items!!.size)
    }

    /**
     * Scroll to the specified mainAdapter position.
     * Actual position of the item on the screen depends on the LayoutManager implementation.
     *
     * @param layoutManager The currently bound LayoutManager
     * @param position Scroll to this mainAdapter position
     */
    fun moveSelectedPosition(layoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager, position: Int): Boolean {
        if (position in 0..(itemCount - 1)) {
            layoutManager.scrollToPosition(position)
            return true
        }

        return false
    }

    /**
     * Check if the loading image is used to the list as an item.
     *
     * @return true if the loading image is used
     */
    fun usedLoadImage(): Boolean {
        return usedLoadingImage
    }

    open fun getItems(): MutableList<T>? {
        return items
    }

    private fun putItems(items: MutableList<T>?, type: Int) {
        val type = type
        dataVersion++

        items ?: if (this.items != null) {
                    val oldSize = this.items!!.size
                    this.items = null
                    lastItems = null
                    notifyItemRangeRemoved(0, oldSize)

                    return
                }

        notifyItems(type, items)
    }

    private fun notifyItems(type: Int, items: MutableList<T>?) {
        when (type) {
            TYPE_NONE, TYPE_FOR_SEARCH_KEYWORD -> {
                this.items = items
                lastItems = items
                notifyDataSetChanged()
                onAddItems(this.items)
            }
            TYPE_FOR_ONE_ITEM -> {
                this.items?.addAll(0, items!!)

                lastItems = items
                notifyItemRangeInserted(0, 1)
                notifyItemRangeChanged(0, this.items!!.size)
                onAddItems(this.items)
            }
            TYPE_FOR_MORE_ITEMS -> {
                lastItems?.let {
                    val startVersion = dataVersion
                    val oldItems = lastItems

                    if (oldItems != null && oldItems.size < MINIMUM_ITEM_COUNT) {
                        notifyItemRangeRemoved(0, oldItems.size)
                    }

                    InsertDiff(this, oldItems, items, startVersion).execute()

                    return
                }

                this.items = items
                lastItems = items
                notifyDataSetChanged()
                onAddItems(items)
            }
            TYPE_FOR_PINNEDUP_ITEMS -> {
                this.items = items
                lastItems = items
                notifyDataSetChanged()
                onAddItems(items)
            }
            else -> {
            }
        }

    }

    private fun update(items: MutableList<T>) {
        dataVersion++
        val startVersion = dataVersion
        val oldItems = this.items

        UpdateDiff(this, oldItems, items, startVersion).execute()
    }

    /**
     * Set true if the loading image is used.
     *
     * @param usedLoadingImage true if the loading image is used
     */
    protected fun setUsedLoadingImage(usedLoadingImage: Boolean) {
        this.usedLoadingImage = usedLoadingImage
    }

    private class InsertDiff<T> internal constructor(private val mAdapter: BaseListAdapter<T>,
                                                     private val mOldItems: List<T>?,
                                                     private val mNewItems: List<T>?,
                                                     private val mStartVersion: Int):
                                                    AsyncTask<Void, Void, DiffUtil.DiffResult>() {

        private val mLoaderWeakRef: WeakReference<BaseListAdapter<T>> = WeakReference(mAdapter)

        override fun doInBackground(vararg voids: Void): DiffUtil.DiffResult {
            return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return mOldItems?.size ?: 0

                }

                override fun getNewListSize(): Int {
                    return mNewItems!!.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val oldItem = mOldItems!![oldItemPosition]
                    val newItem = mNewItems!![newItemPosition]
                    return mAdapter.areItemsTheSame(oldItem, newItem)
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val oldItem = mOldItems!![oldItemPosition]
                    val newItem = mNewItems!![newItemPosition]
                    return mAdapter.areContentsTheSame(oldItem, newItem)
                }
            })
        }

        override fun onPostExecute(diffResult: DiffUtil.DiffResult) {
            mLoaderWeakRef.get()?.let {
                if (mStartVersion != it.dataVersion) {
                    // ignore update
                    return
                }

                it.lastItems = mNewItems
                if (it.items != null) {
                    it.items!!.addAll(0, mNewItems!!)
                }

                it.dispatchUpdates(diffResult)
                it.onAddItems(it.items)
            }
        }
    }

    private class UpdateDiff<T> internal constructor(private val mAdapter: BaseListAdapter<T>, private val mOldItems: List<T>?, private val mNewItems: MutableList<T>,
                                                     private val mStartVersion: Int) : AsyncTask<Void, Void, DiffUtil.DiffResult>() {

        internal val mLoaderWeakRef: WeakReference<BaseListAdapter<T>> = WeakReference(mAdapter)

        override fun doInBackground(vararg voids: Void): DiffUtil.DiffResult {
            return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return mOldItems?.size ?: 0

                }

                override fun getNewListSize(): Int {
                    return mNewItems.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val oldItem = mOldItems!![oldItemPosition]
                    val newItem = mNewItems[newItemPosition]
                    return mAdapter.areItemsTheSame(oldItem, newItem)
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val oldItem = mOldItems!![oldItemPosition]
                    val newItem = mNewItems[newItemPosition]
                    return mAdapter.areContentsTheSame(oldItem, newItem)
                }
            })
        }

        override fun onPostExecute(diffResult: DiffUtil.DiffResult) {
            val adapter = mLoaderWeakRef.get() ?: return

            if (mStartVersion != adapter.dataVersion) {
                // ignore update
                return
            }

            adapter.items = mNewItems
            adapter.dispatchUpdates(diffResult)
            adapter.onUpdateItems(adapter.items)
        }
    }

    protected abstract fun areItemsTheSame(oldItem: T, newItem: T): Boolean

    protected abstract fun areContentsTheSame(oldItem: T, newItem: T): Boolean

    protected abstract fun dispatchUpdates(diffResult: DiffUtil.DiffResult)

    protected abstract fun onAddItems(items: List<T>?)

    protected abstract fun onUpdateItems(items: List<T>?)

    companion object {
        const val TYPE_FOR_MORE_ITEMS = 1
        const val TYPE_FOR_PINNEDUP_ITEMS = 2
        const val TYPE_FOR_SEARCH_KEYWORD = 3

        private const val TYPE_NONE = -1
        private const val TYPE_FOR_ONE_ITEM = 0

        private const val MINIMUM_ITEM_COUNT = 7
    }
}