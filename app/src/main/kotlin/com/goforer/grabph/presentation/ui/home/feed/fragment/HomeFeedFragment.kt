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

package com.goforer.grabph.presentation.ui.home.feed.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.goforer.base.presentation.utils.CommonUtils
import com.goforer.base.presentation.view.decoration.GapItemDecoration
import com.goforer.base.presentation.view.fragment.RecyclerFragment
import com.goforer.grabph.R
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.event.action.*
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.presentation.ui.home.feed.adapter.HomeFeedAdapter
import com.goforer.grabph.presentation.vm.feed.FeedViewModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.feed.FeedItem
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.data.datasource.network.response.Status
import com.goforer.grabph.data.repository.remote.Repository
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.BOUND_FROM_LOCAL
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_FEED_LOCAL
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_home_feed.*
import kotlinx.android.synthetic.main.recycler_view_container.recycler_view
import kotlinx.android.synthetic.main.recycler_view_container.swipe_layout
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@Suppress("SameParameterValue")
class HomeFeedFragment: RecyclerFragment<FeedItem>() {
    private lateinit var adapter: HomeFeedAdapter
    private lateinit var acvAdapter: AutoClearedValue<HomeFeedAdapter>

    private var isOverFirst = false

    private val job = Job()

    private val mainScope = CoroutineScope(Dispatchers.Main + job)

    internal val homeActivity: HomeActivity by lazy {
        activity as HomeActivity
    }

    internal lateinit var feedItem: FeedItem

    internal var isFeedUpdated = false

    private var feedPosition: Int = 0

    private lateinit var gridLayoutManager: StaggeredGridLayoutManager

    @field:Inject
    internal lateinit var feedViewModel: FeedViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        val acvView = AutoClearedValue(this,
            inflater.inflate(R.layout.fragment_home_feed, container, false))

        return acvView.get()?.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        feedPosition = 0

        this@HomeFeedFragment.fam_feed_top.setOnClickListener {
            CommonUtils.betterSmoothScrollToPosition(recycler_view, 0)
            this@HomeFeedFragment.fam_feed_top.visibility = View.GONE
            isOverFirst = false
        }

        this@HomeFeedFragment.recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                    }

                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        // homeActivity.floatingActionMenu.close(true)
                    }

                    RecyclerView.SCROLL_STATE_SETTLING -> {}

                    else -> {
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val vnView = homeActivity.bottom_navigation_view

                vnView.translationY = max(0f, min(vnView.height.toFloat(), vnView.translationY + dy))
                this@HomeFeedFragment.recycler_view.invalidateItemDecorations()
                // homeActivity.floatingActionMenu.close(true)
            }
        })

        isFeedUpdated = false
        refresh(true)
        setItemHasFixedSize(false)
    }

    @ExperimentalCoroutinesApi
    override fun onDestroyView() {
        super.onDestroyView()

        EventBus.getDefault().unregister(this)
        mainScope.cancel()
        job.cancel()
    }

    override fun onSorted(items: List<FeedItem>) {}

    override fun onFirstVisibleItem(position: Int) {
        // To Do::Implement playing the video file with position in case of video item
        Timber.i("onFirstVisibleItem : $position")
    }

    override fun onLastVisibleItem(position: Int) {
        // To Do::Implement playing the video file with position in case of video item
        Timber.i("onLastVisibleItem : $position")
    }

    override fun createLayoutManager(): RecyclerView.LayoutManager {
        super.setOnProcessListener(object : OnProcessListener {
            override fun onScrolledToLast(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("onScrolledToLast")

                this@HomeFeedFragment.fam_feed_top.show(true)
            }

            override fun onScrolling(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("onScrolling")

                if (this@HomeFeedFragment.fam_feed_top.visibility == View.VISIBLE) {
                    this@HomeFeedFragment.fam_feed_top.hide(true)
                }

                homeActivity.searchView?.let {
                    val view = homeActivity.searchView!!

                    view.clearFocus()
                    CommonUtils.dismissKeyboard(view.windowToken, baseActivity)
                }
            }

            override fun onScrollIdle(position: Int) {
                if (position >= HomeActivity.VISIBLE_UPTO_ITEMS) {
                    isOverFirst = true
                    this@HomeFeedFragment.fam_feed_top.visibility = View.VISIBLE
                    this@HomeFeedFragment.fam_feed_top.show(true)
                }
            }

            override fun onScrollSetting() {

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("onScrolled")

                gridLayoutManager.invalidateSpanAssignments()
            }

            override fun onError(message: String) {
                CommonUtils.showToastMessage(baseActivity, message, Toast.LENGTH_SHORT)
            }
        })

        this@HomeFeedFragment.recycler_view.setItemViewCacheSize(30)
        this@HomeFeedFragment.recycler_view.isVerticalScrollBarEnabled = false
        gridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        gridLayoutManager.isItemPrefetchEnabled = true

        // Used StaggeredGridLayoutManager to show the photos instead GridLayoutManager
        /*
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mActivity, SPAN_COUNT,
                GridLayoutManager.VERTICAL, false);
        StaggeredGridLayoutManager.s.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == mAdapter.getItems().size()) {
                    return 6;
                } else if (mFeedItems.get(position).getWidth() >= 240 &&
                        mFeedItems.get(position).getWidth() > mFeedItems.get(position).getHeight()) {
                    return 3;
                } else if (mFeedItems.get(position).getHeight() <= 200 &&
                        mFeedItems.get(position).getHeight() > mFeedItems.get(position).getWidth()) {
                    return 2;
                } else {
                    return 2;
                }
            }
        };

        spanSizeLookup.setSpanIndexCacheEnabled(true);
        gridLayoutManager.setSpanSizeLookup(spanSizeLookup);
        */
        return gridLayoutManager
    }

    override fun createItemDecoration(): RecyclerView.ItemDecoration {
        return object : GapItemDecoration(VERTICAL_LIST,
            resources.getDimensionPixelSize(R.dimen.space_4)) {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                state: RecyclerView.State) {
                outRect.left = 0
                outRect.right = 0
                outRect.bottom = gap

                // Add top margin only for the first item to avoid double space between items
                if (parent.getChildAdapterPosition(view) == 0
                    || parent.getChildAdapterPosition(view) == 1) {
                    outRect.top = gap
                }
            }
        }
    }

    override fun createAdapter(): RecyclerView.Adapter<*> {
        adapter = HomeFeedAdapter(this)
        acvAdapter = AutoClearedValue(this, adapter)
        this@HomeFeedFragment.recycler_view.adapter = acvAdapter.get()

        return adapter
    }

    override fun createItemTouchHelper(): ItemTouchHelper.Callback? {
        return null

        //If the drag-drop function has to be opened, then below code could be unlocked.
        //return new RecyclerItemTouchHelperCallback(getContext(), mAdapter, Color.BLUE);
    }

    override fun isItemDecorationVisible(): Boolean {
        return true
    }

    override fun requestData(isNew: Boolean) {
        getFeed("", LOAD_FEED_LOCAL, BOUND_FROM_LOCAL, Caller.CALLED_FROM_FEED)

        Timber.i("requestData")
    }

    override fun updateData() {
        /*
         * Please put some module to update new data here, instead of doneRefreshing() method if
         * there is some data to be updated from the backend side.
         * I just put doneRefreshing() method because there is no data to be updated from
         * the backend side in this app-architecture project.
         */

        setLoadParam(NetworkBoundResource.LOAD_FEED_UPDATE, Repository.BOUND_FROM_BACKEND, "")

        Timber.i("updateData")
    }

    override fun reachToLastPage() {}

    private fun getFeed(query: String, loadType: Int, boundType: Int, calledFrom: Int) {
        setLoadParam(loadType, boundType, query)
        // feedViewModel.removeCache()
        feedViewModel.feed.observe(this, Observer { resource ->
            when(resource?.getStatus()) {
                Status.SUCCESS -> {
                    resource.getData() ?: return@Observer
                    this@HomeFeedFragment.swipe_layout.visibility = View.VISIBLE
                    @Suppress("UNCHECKED_CAST")
                    val feedItems = resource.getData() as? PagedList<FeedItem>?
                    // val feedItems = resource.getData() as? PagedList<RecentPhoto>?
                    when {
                        feedItems?.size!! > 0 -> {
                            if (feedItems.size >= Repository.FEED_ITEM_CACHE_SIZE) {
                                feedViewModel.deleteLastSeenItems(Repository.FEED_ITEM_LAST_SEEN_COUNT)
                            }

                            acvAdapter.get()?.submitList(feedItems)
                        }

                        resource.getMessage() != null -> {
                            stopFeed(resource)
                        }
                        //else -> getFeed("", LOAD_FEED_FIRST, BOUND_FROM_BACKEND, CALLED_FROM_FEED)
                    }
                }

                Status.LOADING -> {
                }

                Status.ERROR -> {
                    stopFeed(resource)
                }

                else -> {
                    stopFeed(resource)
                }
            }
        })
    }

    private fun stopFeed(resource: Resource) {
        stopMoreLoading(STOP_REFRESHING_TIMEOUT)
        when(resource.errorCode) {
            in 400..499 -> {
                Snackbar.make(homeActivity.coordinator_home_layout, getString(R.string.phrase_client_wrong_request), Snackbar.LENGTH_LONG).show()
            }

            in 500..599 -> {
                Snackbar.make(homeActivity.coordinator_home_layout, getString(R.string.phrase_server_wrong_response), Snackbar.LENGTH_LONG).show()
            }

            else -> {
                Snackbar.make(homeActivity.coordinator_home_layout, resource.getMessage().toString(), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setLoadParam(loadType: Int, boundType: Int, keyword: String) {
        feedViewModel.setParameters(
            Parameters(
                keyword,
                -1,
                loadType,
                boundType
            ), -1)
    }

    /**
     * Helper function to call something doing function
     *
     * By marking `block` as `suspend` this creates a suspend lambda which can call suspend
     * functions.
     *
     * @param block lambda to actually do some work. It is called in the uiScope.
     *              lambda the some work will do
     */
    internal inline fun launchWork(crossinline block: suspend () -> Unit): Job {
        return mainScope.launch {
            block()
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onAction(action: FeedUpdateAction) {
        isFeedUpdated = true
        feedPosition = action.position
    }
}