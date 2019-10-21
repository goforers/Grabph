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

package com.goforer.grabph.presentation.ui.search.fragment

import android.annotation.SuppressLint
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.goforer.base.presentation.utils.CommonUtils.dismissKeyboard
import com.goforer.base.presentation.utils.CommonUtils.showToastMessage
import com.goforer.base.presentation.view.decoration.GapItemDecoration
import com.goforer.base.presentation.view.fragment.RecyclerFragment
import com.goforer.base.presentation.view.helper.RecyclerItemTouchHelperCallback
import com.goforer.grabph.R
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.event.action.TakeSearchedFeedsAction
import com.goforer.grabph.presentation.ui.search.FeedSearchActivity
import com.goforer.grabph.presentation.ui.search.adapter.FeedSearchAdapter
import com.goforer.grabph.presentation.vm.search.FeedSearchViewModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.feed.FeedItem
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.data.datasource.network.response.Status
import com.goforer.grabph.presentation.ui.search.SearchedFeedItem
import com.goforer.grabph.presentation.vm.BaseViewModel.Companion.NONE_TYPE
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_FEED_SEARCH
import com.goforer.grabph.data.repository.remote.Repository.Companion.BOUND_FROM_BACKEND
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import kotlinx.android.synthetic.main.activity_feed_search.*
import kotlinx.android.synthetic.main.fragment_feed_search.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import javax.inject.Inject

class FeedSearchFragment : RecyclerFragment<FeedItem>() {
    private lateinit var mGridLayoutManager: StaggeredGridLayoutManager

    internal val feedSearchActivity: FeedSearchActivity by lazy {
        activity as FeedSearchActivity
    }

    internal lateinit var adapter: FeedSearchAdapter

    private lateinit var acvAdapter: AutoClearedValue<FeedSearchAdapter>

    private lateinit var query: String

    @field:Inject
    lateinit var feedSearchModel: FeedSearchViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        val acvView = AutoClearedValue(this,
                inflater.inflate(R.layout.fragment_feed_search, container, false))

        return acvView.get()?.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        query = feedSearchActivity.getKeyword()
        setItemHasFixedSize(true)
        refresh(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        EventBus.getDefault().unregister(this)
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

                recycler_view.isNestedScrollingEnabled = false
            }

            override fun onScrolling(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("onScrolling")

                recycler_view.isNestedScrollingEnabled = true

                val view = feedSearchActivity.getSearchView()

                view.clearFocus()
                dismissKeyboard(view.windowToken, baseActivity)
            }

            override fun onScrollIdle(position: Int) {
                recycler_view.isNestedScrollingEnabled = false
            }

            override fun onScrollSetting() {

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("onScrolled")

                mGridLayoutManager.invalidateSpanAssignments()
            }

            override fun onError(message: String) {
                showToastMessage(baseActivity, message, Toast.LENGTH_SHORT)
            }
        })

        this@FeedSearchFragment.recycler_view.setHasFixedSize(false)
        this@FeedSearchFragment.recycler_view.setItemViewCacheSize(20)
        this@FeedSearchFragment.recycler_view.isDrawingCacheEnabled = true
        this@FeedSearchFragment.recycler_view.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        mGridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        mGridLayoutManager.isItemPrefetchEnabled = true

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
        return mGridLayoutManager
    }

    override fun createItemDecoration(): RecyclerView.ItemDecoration {
        return object : GapItemDecoration(VERTICAL_LIST, resources.getDimensionPixelSize(R.dimen.space_1)) {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                        state: RecyclerView.State) {
                outRect.left = gap
                outRect.right = gap
                outRect.bottom = gap

                // Add top margin only for the first item to avoid double space between items
                if (parent.getChildAdapterPosition(view) == 0)
                    outRect.top = gap
            }
        }
    }

    override fun createAdapter(): RecyclerView.Adapter<*> {
        adapter = FeedSearchAdapter(this)
        acvAdapter = AutoClearedValue(this, adapter)
        recycler_view.adapter = acvAdapter.get()

        return adapter
    }

    override fun createItemTouchHelper(): ItemTouchHelper.Callback {
        return RecyclerItemTouchHelperCallback(this.context.applicationContext, adapter, Color.BLUE)
    }

    override fun isItemDecorationVisible(): Boolean {
        return true
    }

    override fun requestData(isNew: Boolean) {
        feedSearchModel.setParameters(
            Parameters(
                query,
                -1,
                LOAD_FEED_SEARCH,
                BOUND_FROM_BACKEND
            ), NONE_TYPE)
        getSearchedFeed(query)

        Timber.i("requestData")
    }

    override fun updateData() {
        /*
         * Please put some module to update new data here, instead of doneRefreshing() method if
         * there is some data to be updated from the backend side.
         * I just put doneRefreshing() method because there is no data to be updated from
         * the backend side in this app-architecture project.
         */
        //stopRefreshing();

        feedSearchModel.setParameters(
            Parameters(
                query,
                -1,
                LOAD_FEED_SEARCH,
                BOUND_FROM_BACKEND
            ), NONE_TYPE)
        Timber.i("updateData")
    }

    override fun reachToLastPage() {}

    fun searchKeyword(keyword: String) {
        feedSearchModel.setParameters(
            Parameters(
                keyword,
                -1,
                LOAD_FEED_SEARCH,
                BOUND_FROM_BACKEND
            ), NONE_TYPE)
        getSearchedFeed(keyword)
    }

    private fun getSearchedFeed(query: String) = feedSearchModel.feed.observe(this, Observer { resource ->
        when(resource?.getStatus()) {
            Status.SUCCESS -> {
                this@FeedSearchFragment.swipe_layout.visibility = View.VISIBLE
                @Suppress("UNCHECKED_CAST")
                val feedItems = resource.getData() as? PagedList<FeedItem>?
                if (feedItems?.size!! > 0) {
                    getSearchedFeedsAction()
                    acvAdapter.get()?.submitList(feedItems)
                } else {
                    resource.getMessage() ?: searchKeyword(query)
                    resource.getMessage()?.let {
                        stopLoading(STOP_REFRESHING_TIMEOUT)
                        stopSearchFeed(resource)
                    }
                }
            }

            Status.LOADING -> {
            }

            Status.ERROR -> {
                stopLoading(STOP_REFRESHING_TIMEOUT)
                stopSearchFeed(resource)
            }

            else -> {
                stopLoading(STOP_REFRESHING_TIMEOUT)
                stopSearchFeed(resource)
            }
        }
    })

    private fun getSearchedFeedsAction() {
        val action = TakeSearchedFeedsAction()

        action.feedSearchViewModel = feedSearchModel
        EventBus.getDefault().post(action)
    }

    @SuppressLint("SetTextI18n")
    private fun stopSearchFeed(resource: Resource) = when(resource.errorCode) {
        in 400..499 -> {
            Snackbar.make(feedSearchActivity.coordinator_feed_search_layout, getString(R.string.phrase_client_wrong_request), LENGTH_LONG).show()
        }

        in 500..599 -> {
            Snackbar.make(feedSearchActivity.coordinator_feed_search_layout, getString(R.string.phrase_server_wrong_response), LENGTH_LONG).show()
        }

        else -> {}
    }

    private fun setItemsClear() {
        val pagedList = acvAdapter.get()?.currentList
        pagedList?.clear()

        acvAdapter.get()?.notifyDataSetChanged()
    }

    /*
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onAction(action: SearchKeywordSubmitAction) {
        // Will be implemented
    }
    */

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onAction(action: TakeSearchedFeedsAction) {
        SearchedFeedItem.setSearchedFeedItems(action.feedSearchViewModel.feeds)
    }
}
