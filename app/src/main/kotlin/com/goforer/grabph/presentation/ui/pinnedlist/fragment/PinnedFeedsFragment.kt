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

package com.goforer.grabph.presentation.ui.pinnedlist.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.annotation.GlideModule
import com.goforer.base.presentation.utils.CommonUtils.betterSmoothScrollToPosition
import com.goforer.base.presentation.utils.CommonUtils.showToastMessage
import com.goforer.base.presentation.view.decoration.RemoverItemDecoration
import com.goforer.base.presentation.view.fragment.RecyclerFragment
import com.goforer.base.presentation.view.helper.RecyclerItemTouchHelperCallback
import com.goforer.grabph.R
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import com.goforer.grabph.presentation.common.utils.handler.watermark.WatermarkHandler
import com.goforer.grabph.presentation.vm.feed.FeedViewModel
import com.goforer.grabph.presentation.ui.pinnedlist.PinnedFeedsActivity
import com.goforer.grabph.presentation.ui.pinnedlist.adapter.PinnedFeedsAdapter
import com.goforer.grabph.data.datasource.model.cache.data.entity.feed.FeedItem
import kotlinx.android.synthetic.main.fragment_pinnedup_item.*
import kotlinx.android.synthetic.main.recycler_view_container.*
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import javax.inject.Inject

class PinnedFeedsFragment: RecyclerFragment<FeedItem>() {
    internal val pinnedFeedsActivity: PinnedFeedsActivity by lazy {
        activity as PinnedFeedsActivity
    }

    private lateinit var acvAdapter: AutoClearedValue<PinnedFeedsAdapter>

    private lateinit var glideRequestManager: RequestManager

    private var isOverFirst = false
    internal var isUpdateNotified = false

    internal lateinit var feedItem: FeedItem

    internal var position: Int = 0

    @field:Inject
    lateinit var feedViewModel: FeedViewModel

    @field:Inject
    lateinit var workHandler: CommonWorkHandler

    @field:Inject
    lateinit var waterMarkHandler: WatermarkHandler

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        isUpdateNotified = false

        @GlideModule
        glideRequestManager = Glide.with(this)

        val acvView = AutoClearedValue(this,
                inflater.inflate(R.layout.fragment_pinnedup_item, container, false))
        return acvView.get()?.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refresh(true)
        setItemHasFixedSize(true)
        this@PinnedFeedsFragment.fam_pinned_top.setOnClickListener {
            betterSmoothScrollToPosition(recycler_view, 0)
            this@PinnedFeedsFragment.fam_pinned_top.visibility = View.GONE
            isOverFirst = false
        }
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
            }

            override fun onScrolling(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("onScrolling")
            }

            override fun onScrollIdle(position: Int) {}

            override fun onScrollSetting() {

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("onScrolled")
            }

            override fun onError(message: String) {
                showToastMessage(baseActivity, message, Toast.LENGTH_SHORT)
            }
        })

        this@PinnedFeedsFragment.recycler_view.setHasFixedSize(true)
        this@PinnedFeedsFragment.recycler_view.setItemViewCacheSize(10)
        this@PinnedFeedsFragment.recycler_view.isVerticalScrollBarEnabled = false

        return LinearLayoutManager(this.context.applicationContext, RecyclerView.VERTICAL, false)
    }

    override fun createItemDecoration(): RecyclerView.ItemDecoration {
        return RemoverItemDecoration(Color.TRANSPARENT)
    }

    override fun createAdapter(): RecyclerView.Adapter<*> {
        val adapter = PinnedFeedsAdapter(this, waterMarkHandler, workHandler)

        acvAdapter = AutoClearedValue(this, adapter)
        this@PinnedFeedsFragment.recycler_view.adapter = acvAdapter.get()

        return adapter
    }

    override fun createItemTouchHelper(): ItemTouchHelper.Callback? {
        return RecyclerItemTouchHelperCallback(this.context.applicationContext, acvAdapter.get()!!, Color.BLUE)

    }

    override fun isItemDecorationVisible(): Boolean {
        return true
    }

    override fun requestData(isNew: Boolean) {
        getPinnedUp()

        Timber.i("requestData")
    }

    override fun updateData() {
        /*
         * Please put some module to update new data here, instead of doneRefreshing() method if
         * there is some data to be updated from the backend side.
         * I just put doneRefreshing() method because there is no data to be updated from
         * the backend side in this app-architecture project.
         */

        stopLoading(STOP_REFRESHING_TIMEOUT)

        Timber.i("updateData")
    }

    override fun reachToLastPage() {
    }

    private fun getPinnedUp() {
        feedViewModel.pinnedUp.observe(this, Observer {
            it ?: stopRefreshing(true)
            it?.let {
                if (!isUpdateNotified) {
                    swipe_layout.visibility = View.VISIBLE
                }

                acvAdapter.get()?.submitList(it)
            }
        })

        if (!isUpdateNotified) {
            stopLoading(STOP_REFRESHING_TIMEOUT)
        }
    }
}