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

@file:Suppress("KDocUnresolvedReference")

package com.goforer.base.presentation.view.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import androidx.recyclerview.widget.RecyclerView.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.goforer.base.presentation.view.adatper.BaseListAdapter
import com.goforer.base.presentation.view.decoration.DividerItemDecoration
import com.goforer.grabph.R
import kotlinx.android.synthetic.main.recycler_view_container.*
import timber.log.Timber
import java.util.*
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager
import com.goforer.base.domain.common.GeneralFunctions

abstract class RecyclerFragment<T>: BaseFragment() {
    private var baseArrayAdapter: BaseListAdapter<*>? = null
    private var listener: OnProcessListener? = null

    private var itemTouchHelper: ItemTouchHelper? = null

    private var itemTouchHelperEnabled = false
    private var isLoading = false
    private var isUpdated = false

    /**
     * Get the total page count
     *
     * @return The total page count
     */
    /**
     * Set the total page count
     *
     * @param totalPage The total page count
     */
    protected var totalPage = 0
        set(totalPage) {
            if (totalPage == 0) {
                baseArrayAdapter?.isReachedToLastPage=true
            }

            field=totalPage
        }
    /**
     * Gets current page
     *
     * @return current page
     */
    var currentPage = 0
        private set

    private val items=ArrayList<T>()

    private var onScrollListener: OnScrollListener? = null

    /**
     * Get the last visible item index
     *
     * @return the last visible item index
     */
    protected val lastVisibleItem: Int
        get() {
            return when (val layoutManager= recycler_view?.layoutManager) {
                is LinearLayoutManager -> (layoutManager as? LinearLayoutManager)?.findLastVisibleItemPosition() ?: 0
                is StaggeredGridLayoutManager -> GeneralFunctions.getLastVisibleItem(
                                                layoutManager.findLastVisibleItemPositions(null))
                else -> (layoutManager as? GridLayoutManager)?.findLastVisibleItemPosition() ?: 0
            }

        }

    /**
     * Get the first completely visible item index
     *
     * @return the last visible item index
     */
    private val firstCompletelyVisibleItem: Int
        get() {
            val layoutManager= recycler_view?.layoutManager
            return (layoutManager as? LinearLayoutManager)?.findFirstCompletelyVisibleItemPosition()
                    ?: 0

        }

    private val lastCompletelyVisibleItem: Int
        get() {
            val layoutManager= recycler_view?.layoutManager
            return (layoutManager as? LinearLayoutManager)?.findLastCompletelyVisibleItemPosition()
                    ?: 0

        }

    /**
     * Check if the DividerItemDecoration is visible.
     *
     *
     *
     * To check if the given page is the last page, you must override
     * This method is called in [.onViewCreated].
     *
     *
     * @return true if the ItemDecoration in [RecyclerView] must be visible
     */
    abstract fun isItemDecorationVisible(): Boolean

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_default_recycler, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViews()
    }

    override fun onResume() {
        super.onResume()

        setScrollListener()
    }

    override fun onPause() {
        super.onPause()

        removeScrollListener()
    }

    private fun setViews() {
        setupSwipeLayout()
        recycler_view?.layoutManager=createLayoutManager()
        if (isItemDecorationVisible()) {
            addItemDecorations()
        }

        addItemTouchListener()
        recycler_view?.itemAnimator=createItemAnimator()
        val adapter= createAdapter()
        val callback= createItemTouchHelper()
        callback?.let {
            setItemTouchHelper(it)
        }

        setScrollListener()
        if (adapter is BaseListAdapter<*>) {
            baseArrayAdapter = adapter
        }

        Timber.i("Initialize views")
    }

    private fun request(isRefreshed: Boolean) {
        if (totalPage > 0 && isLastPage(currentPage)) {
            stopRefreshing(false)
            baseArrayAdapter?.isReachedToLastPage = true
        }

        if (!isLoading) {
            isLoading = true
            baseArrayAdapter?.isLoadingItems = true

            currentPage = FIRST_PAGE
            //setTotalPage(FIRST_PAGE);
            if (isRefreshed) {
                requestData(false)
            } else {
                requestData(true)
            }
        }
    }

    private fun requestUpdate() {
        if (!isLoading) {
            isLoading = true
            baseArrayAdapter?.isLoadingItems = true
            isUpdated = true

            updateData()
        }
    }

    /**
     * An OnScrollListener can be set on a RecyclerView to receive messages
     * when a scrolling event has occurred on that RecyclerView.
     *
     * If you are planning to have several listeners at the same time, use
     * RecyclerView#addOnScrollListener. If there will be only one listener at the time and you
     * want your components to be able to easily replace the listener use
     * RecyclerView#setOnScrollListener.
     */
    private fun setScrollListener() {
        removeScrollListener()

        onScrollListener=object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                listener?.onScrolled(recyclerView, dx, dy)
                if (!isLoading && dy >= 0) {
                    val lastVisibleItemPosition= lastVisibleItem
                    val totalItemCount= recyclerView.layoutManager!!.itemCount

                    if (lastVisibleItemPosition >= totalItemCount - 1) {
                        baseArrayAdapter?.isReachedToLastItem = true

                        if (isLastPage(currentPage)) {
                            reachToLastPage()
                            stopRefreshing(false)
                            //mBaseArrayAdapter.setReachedToLastPage(true);

                            return
                        }

                        scrollToEndPage()
                        reachToEndPage(currentPage)
                        listener?.onScrolledToLast(recyclerView, dx, dy)
                    } else {
                        //mBaseArrayAdapter.setReachedToLastItem(false);
                    }
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    SCROLL_STATE_IDLE -> doScrollSateIdle()
                    SCROLL_STATE_DRAGGING -> listener?.onScrolling(recyclerView, recyclerView.scrollX, recyclerView.scrollY)
                    SCROLL_STATE_SETTLING -> listener?.onScrollSetting()
                    else -> doScrollSateIdle()
                }
            }
        }

        recycler_view?.addOnScrollListener(onScrollListener!!)
    }

    /**
     * Notify when the page reach to the end of tha page
     *
     * @param page ther current page
     */
    private fun reachToEndPage(page: Int) {
        Timber.i("reachToEndPage : $page" )
    }

    private fun doScrollSateIdle() {
        listener?.onScrollIdle(lastVisibleItem)
        // TO DO::Implement playing the video file with position in case of video item
        onFirstVisibleItem(firstCompletelyVisibleItem)
        onLastVisibleItem(lastCompletelyVisibleItem)
    }

    /**
     * Remove a listener that was notified of any changes in scroll state or position.
     */
    private fun removeScrollListener() {
        onScrollListener?.let {
            recycler_view?.removeOnScrollListener(it)
        }
    }

    /**
     * Set an ItemTouchHelper that will work with the given Callback
     *
     * @param callback This is the contract between ItemTouchHelper and your application.
     * It lets you control which touch behaviors are enabled per each ViewHolder
     * and also receive callbacks when user performs these actions.
     *
     * @see ItemTouchHelper.Callback
     */
    private fun setItemTouchHelper(callback: ItemTouchHelper.Callback) {
        itemTouchHelperEnabled = true
        attachItemTouchHelperToRecyclerView(callback)
    }

    /**
     * Attaches the ItemTouchHelper to the provided RecyclerView. If TouchHelper is already
     * attached to a RecyclerView, it will first detach from the previous one. You can call this
     * method with `null` to detach it from the current RecyclerView.
     *
     * @param callback This is the contract between ItemTouchHelper and your application.
     * It lets you control which touch behaviors are enabled per each ViewHolder
     * and also receive callbacks when user performs these actions.
     *
     * @return the ItemTouchHelper for attaching to the provided RecyclerView
     *
     * @see ItemTouchHelper
     */
    private fun attachItemTouchHelperToRecyclerView(callback: ItemTouchHelper.Callback): ItemTouchHelper {
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper?.attachToRecyclerView(recycler_view)

        return itemTouchHelper as ItemTouchHelper
    }

    /**
     * Check if the given page is the last page.
     * Return true if the given page is the last page
     *
     *
     * To check if the given page is the last page, you must override
     * This method is called whenever the swipe gesture triggers a refresh.
     *
     *
     * @param pageNum the current page number to check if given page is the last page
     */
    protected fun isLastPage(pageNum: Int): Boolean {
        return totalPage == pageNum && totalPage >= 0
    }

    /**
     * RecyclerView can perform several optimizations if it can know in advance that changes in
     * mainAdapter content cannot change the size of the RecyclerView itself.
     * If your use of RecyclerView falls into this category, set this to true.
     *
     * @param hasFixedSize true if mainAdapter changes cannot affect the size of the RecyclerView.
     * setItemHasFixedSize(true) means the RecyclerView has children (items)
     * that has fixed width and
     */
    protected fun setItemHasFixedSize(hasFixedSize: Boolean) {
        recycler_view?.setHasFixedSize(hasFixedSize)
    }

    /**
     * Add an [ItemDecoration] to this RecyclerView. SnapItem decorations can
     * affect both measurement and drawing of individual item views.
     *
     *
     * SnapItem decorations are ordered. Decorations placed earlier in the list will
     * be run/queried/drawn first for their effects on item views. Padding added to views
     * will be nested; a padding added by an earlier decoration will mean further
     * item decorations in the list will be asked to draw/pad within the previous decoration's
     * given area.
     *
     */
    private fun addItemDecorations() {
        recycler_view?.addItemDecoration(createItemDecoration())
    }

    /**
     * Add the touch listener on RecyclerView
     */
    private fun addItemTouchListener() {}

    /**
     * Sets the [SwipeRefreshLayout]'s setting that this RecyclerFragment will use.
     *
     *
     *
     * To set the [SwipeRefreshLayout]'s more setting, you must override
     *
     *
     */
    private fun setupSwipeLayout() {
        swipe_layout?.setColorSchemeResources(R.color.redLight)
        swipe_layout?.setOnRefreshListener { requestUpdate() }
    }

    /**
     * Create the [LayoutManager] that this RecyclerFragment will use.
     *
     *
     *
     * To set the [LayoutManager] to provide RecyclerFragment, you must override
     *
     *
     *
     * In contrast to other mainAdapter-backed views such as [android.widget.ListView]
     * or [android.widget.GridView], RecyclerFragment allows client code to provide custom
     * layout arrangements for child views. These arrangements are controlled by the
     * [LayoutManager]. A LayoutManager must be provided for RecyclerFragment to function.
     *
     *
     * Several default strategies are provided for common uses such as lists and grids.
     *
     * @return LayoutManager to use
     *
     * If you'd like to use GridLayout, you have to override this setLayoutManager method
     * in your fragment as below example:
     *
     * Example
     * @@Override
     * protected RecyclerView.LayoutManager setLayoutManager() {
     * return new GridLayoutManager(activity, 1);
     * }
     * or to use LinearLayout, you have to override this setLayoutManager method
     * in your fragment as below:
     *
     * @@Override
     * protected RecyclerView.LayoutManager setLayoutManager() {
     * return new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
     * }
     */
    protected abstract fun createLayoutManager(): LayoutManager

    /**
     * Create the ItemDecoration.
     *
     * An ItemDecoration allows the application to add a special drawing and layout offset
     * to specific item views from the mainAdapter's data set. This can be useful for drawing dividers
     * between items, highlights, visual grouping boundaries and more.
     *
     *
     * All ItemDecorations are drawn in the order item were added.
     *
     */
    protected open fun createItemDecoration(): ItemDecoration {
        return DividerItemDecoration(baseActivity, DividerItemDecoration.VERTICAL_LIST)
    }

    /**
     * Starts dragging the provided ViewHolder.
     *
     * @param viewHolder The ViewHolder to start dragging. It must be a direct child of
     * RecyclerView.
     */
    @Deprecated("")
    protected fun startDrag(viewHolder: ViewHolder) {
        if (itemTouchHelperEnabled) {
            swipe_layout?.isRefreshing = false
            itemTouchHelper?.startDrag(viewHolder)
        }
    }

    /**
     * This implementation of [RecyclerView.ItemAnimator] provides basic
     * animations on remove, add, and move events that happen to the items in
     * a RecyclerView. RecyclerView uses a DefaultItemAnimator by default.
     *
     * @return The ItemAnimator for the animations that take place on items as changes are made
     * to the mainAdapter.
     *
     * @see RecyclerView.setItemAnimator
     */
    private fun createItemAnimator(): ItemAnimator {
        return DefaultItemAnimator()
    }

    /**
     * Create a new mainAdapter to provide child views on demand.
     *
     *
     * To set a new mainAdapter to provide child views on demand, you must override
     *
     *
     *
     * When mainAdapter is changed, all existing views are recycled back to the pool. If the pool has
     * only one mainAdapter, it will be cleared.
     *
     * @return The new mainAdapter to set, or null to set no mainAdapter.
     */
    protected abstract fun createAdapter(): Adapter<*>

    /**
     * Attach an ItemTouchHelper to the provided RecyclerView.
     *
     *
     * To attach an ItemTouchHelper to the provided RecyclerView, you must override.
     *
     *
     * @return The callback which is the contract between ItemTouchHelper and your application.
     * It lets you control which touch behaviors are enabled per each ViewHolder
     * and also receive callbacks when user performs these actions.
     *
     * return null if you don't want to attach an ItemTouchHelper to provided RecyclerView.
     */
    protected abstract fun createItemTouchHelper(): ItemTouchHelper.Callback?

    /**
     * RequestClient to get the information or images from server.
     *
     *
     * To request information or data to Web server, you must override
     * This method is called whenever the mainAdapter position of the last visible view is to last item on the list.
     *
     *
     * Don't implement any code in this overridden method if you don't like to request any information
     * or data to Web server.
     *
     * @param isNew set to true to request new information or images, or false
     */
    protected abstract fun requestData(isNew: Boolean)


    /**
     * RequestClient to get the updated information or images from server.
     *
     *
     * To request information or data to Web server, you must override
     * This method is called whenever the swipe gesture triggers a refresh.
     *
     *
     */
    protected abstract fun updateData()

    /**
     * Notify when the end of the page is reached
     */
    protected abstract fun reachToLastPage()

    /**
     * Notify when the current items are sorted
     *
     * @param items the sorted items
     */
    abstract fun onSorted(items: List<T>)

    /**
     * Callback method to be invoked to notify the first visible position when RecyclerView's scroll
     * state is idle.
     *
     * @param position the mainAdapter position of the first fully visible item on RecyclerView
     */
    abstract fun onFirstVisibleItem(position: Int)

    /**
     * Callback method to be invoked to notify the last visible position when RecyclerView's scroll
     * state is idle.
     *
     * @param position the mainAdapter position of the last fully visible item on RecyclerView
     */
    abstract fun onLastVisibleItem(position: Int)

    /**
     * The information should be refreshed whenever the RecyclerFragment is created or
     * the user refresh the contents of a view via a vertical swipe gesture.
     *
     * <P> Must.
     * This method have to be called in override onViewCreated[.onViewCreated] method
     * to get the information from server.
    </P> *
     *
     *
     *
     * Be refreshed as a result of the gesture in case a vertical swipe gesture.
     * The information must be provided to allow refresh of the content wherever this gesture
     * is used in case of a vertical swipe gesture.
     *
     *
     */
    protected fun refresh(refreshed: Boolean) {
        Timber.i("refresh")

        if (refreshed) {
            swipe_layout?.post {
                swipe_layout?.isRefreshing = true
                request(true)
            }
        }
    }

    /**
     * Set the scrolling is reached to last
     */
    protected fun scrollToEndPage() {
        Timber.i("scrolledReachToLast")

        if (!isLoading) {
            isLoading=totalPage != 0

            baseArrayAdapter?.isLoadingItems = true

            currentPage++
        }
    }

    protected fun clear() {
        currentPage = FIRST_PAGE

        if (items.size > 0) {
            items.clear()
        }
    }

    /**
     * Stop the data-parsing processing.
     */
    fun stopRefreshing(isPagedList: Boolean) {
        isLoading = false
        isUpdated = false

        if (!isPagedList) {
            baseArrayAdapter?.isLoadingItems = false
        }

        swipe_layout?.let {
            if (it.isRefreshing) {
                it.isRefreshing = false
            }
        }
    }

    private fun stopLoading() {
        stopRefreshing(false)
    }

    private fun stopMoreLoading() {
        stopRefreshing(true)
    }

    @MainThread
    fun stopLoading(delayMillis: Long) {
        Handler(Looper.getMainLooper()).postDelayed({ this.stopLoading() }, delayMillis)
    }

    @MainThread
    fun stopMoreLoading(delayMillis: Long) {
        Handler(Looper.getMainLooper()).postDelayed({ this.stopMoreLoading() }, delayMillis)
    }

    /**
     * Sets the listener to be notified when a process is completed
     */
    fun setOnProcessListener(listener: OnProcessListener) {
        this.listener = listener
    }

    /**
     * Classes that wish to be notified when a process is completed should implement this interfaces.
     * An OnProcessListener allows the application to intercept the process events.
     *
     *
     * This can be useful for applications that wish to implement some module
     * after the process is done.
     * /p>
     */
    interface OnProcessListener {
        /**
         * This listener method to be invoked when the RecyclerView has been scrolled to the last.
         * This will be called after the scroll has completed.
         *
         *
         * This listener method will also be called if visible item range changes after a layout
         * calculation. In that case, dx and dy will be 0.
         *
         * @param recyclerView The RecyclerView which scrolled.
         * @param dx The amount of horizontal scroll.
         * @param dy The amount of vertical scroll.
         */
        fun onScrolledToLast(recyclerView: RecyclerView, dx: Int, dy: Int)

        /**
         * This listener method to be invoked when scrolling is doing.
         *
         * @param recyclerView The RecyclerView which scrolled.
         * @param dx The amount of horizontal scroll.
         * @param dy The amount of vertical scroll.
         */
        fun onScrolling(recyclerView: RecyclerView, dx: Int, dy: Int)


        /**
         * This listener method to be invoked when scrolling state is currently idle.
         *
         *  * @param position The current position.
         */
        fun onScrollIdle(position: Int)

        /**
         * This listener method to be invoked when scrolling state is currently animating to a final
         * position while not under outside control.
         *
         */
        fun onScrollSetting()

        /**
         * This listener method to be invoked when scrolling is done.
         *
         * @param recyclerView The RecyclerView which scrolled.
         * @param dx The amount of horizontal scroll.
         * @param dy The amount of vertical scroll.
         */
        fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int)

        fun onError(message: String)
    }

    companion object {
        private const val FIRST_PAGE=1

        const val STOP_LOADING_TIME0UT: Long = 600
        const val STOP_ADDING_TIMEOUT: Long = 100
        const val STOP_REFRESHING_PROMPT_TIMEOUT: Long = 1
        const val STOP_REFRESHING_TIMEOUT: Long = 500
        const val STOP_UPDATE_TIMEOUT: Long = 1000
        const val SCROLL_TO_FIRST_TIMEOUT: Long = 1400

        const val LAYOUT_TYPE_LINEAR = 0
        const val LAYOUT_TYPE_GRID = 1
        const val LAYOUT_TYPE_STAGGEREDGRID = 2

        const val MSG_TYPE_NO_RESULT = 0
        const val MSG_TYPE_ERROR = 1
    }
}
