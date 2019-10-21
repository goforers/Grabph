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

package com.goforer.grabph.presentation.ui.home.profile.fragment.photos

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.annotation.GlideModule
import com.goforer.base.presentation.view.customs.layout.CustomStaggeredGridLayoutManager
import com.goforer.base.presentation.view.decoration.GapItemDecoration
import com.goforer.base.presentation.view.fragment.BaseFragment
import com.goforer.grabph.R
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.presentation.ui.home.profile.adapter.photos.ProfilePhotosAdapter
import com.goforer.grabph.data.datasource.model.cache.data.mock.datasource.profile.MyPhotoDataSource
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.MyPhoto
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_home_profile_photos.*
import kotlin.math.max
import kotlin.math.min

/**
 * This Fragment is a fragment inside HomeProfileFragment
 * This is one of the two fragments that can be accessed by tab layout.
 * */

class HomeProfilePhotosFragment: BaseFragment() {
    private var adapter: ProfilePhotosAdapter? = null

    private lateinit var glideRequestManager: RequestManager

    private lateinit var acvAdapterMyPhotos: AutoClearedValue<ProfilePhotosAdapter>

    internal val homeActivity: HomeActivity by lazy { activity as HomeActivity }

    private lateinit var gridLayoutManager: CustomStaggeredGridLayoutManager

    internal val recyclerView = this.recycler_profile_photos

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        @GlideModule
        glideRequestManager = Glide.with(this)

        val acvView = AutoClearedValue(this, inflater.inflate(R.layout.fragment_home_profile_photos, container, false))

        return acvView.get()?.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setScrollAddListener()
        createMyPhotosAdapter()
    }


    private fun createMyPhotosAdapter() {
        adapter = adapter ?: ProfilePhotosAdapter(homeActivity)
        acvAdapterMyPhotos = AutoClearedValue(this, adapter)
        this.recycler_profile_photos.setHasFixedSize(true)
        this.recycler_profile_photos.isVerticalScrollBarEnabled = false
        gridLayoutManager = CustomStaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        gridLayoutManager.enabledSrcoll = true
        gridLayoutManager.isItemPrefetchEnabled = true
        this.recycler_profile_photos.layoutManager = gridLayoutManager
        this.recycler_profile_photos.addItemDecoration(createItemDecoration())
        this.recycler_profile_photos.adapter = acvAdapterMyPhotos.get()
    }

    private fun createItemDecoration(): RecyclerView.ItemDecoration {
        return object : GapItemDecoration(VERTICAL_LIST,resources.getDimensionPixelSize(R.dimen.space_4)) {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.left = 2
                outRect.right = 2
                outRect.bottom = 0

                // Add top margin only for the first item to avoid double space between items
                if (parent.getChildAdapterPosition(view) == 0 || parent.getChildAdapterPosition(view) == 1) {
                    outRect.top = 0
                }
            }
        }
    }

    internal fun setMyPhotosView(myPhotos: List<MyPhoto>) {
        val config = PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(10)
                .setPrefetchDistance(5)
                .build()

        LivePagedListBuilder(object : DataSource.Factory<Int, MyPhoto>() {
            override fun create(): DataSource<Int, MyPhoto> {
                return MyPhotoDataSource(myPhotos)
            }
        }, config).build().observe(this, Observer { pagedList ->
            acvAdapterMyPhotos.get()?.let { adapter -> submitMyPhotos(adapter, pagedList) }
        })
    }

    private fun submitMyPhotos(photosAdapter: ProfilePhotosAdapter, myPhotos: PagedList<MyPhoto>) {
        photosAdapter.submitList(myPhotos)
    }

    internal fun setRecyclerScrollable(value: Boolean) {
        gridLayoutManager.enabledSrcoll = value
    }

    private fun setScrollAddListener() {
        this@HomeProfilePhotosFragment.recycler_profile_photos.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> { }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        homeActivity.closeFab()
                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> {}
                    else -> { }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val vnView = homeActivity.bottom_navigation_view

                vnView.translationY = max(0f, min(vnView.height.toFloat(), vnView.translationY + dy))
                homeActivity.closeFab()
            }
        })
    }
}