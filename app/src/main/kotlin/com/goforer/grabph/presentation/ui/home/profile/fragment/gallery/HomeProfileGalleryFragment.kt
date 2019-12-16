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

package com.goforer.grabph.presentation.ui.home.profile.fragment.gallery

import android.animation.ValueAnimator
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.lifecycle.Observer
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
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.MyGallery
import com.goforer.grabph.data.datasource.network.response.Status
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.presentation.ui.home.profile.adapter.photos.ProfileGalleryAdapter
import com.goforer.grabph.presentation.vm.profile.HomeProfileViewModel
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_home_profile_photos.*
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

/**
 * This Fragment is a fragment inside HomeProfileFragment
 * This is one of the two fragments that can be accessed by tab layout.
 * */

class HomeProfileGalleryFragment: BaseFragment() {
    internal val homeActivity: HomeActivity by lazy { activity as HomeActivity }

    private var adapter: ProfileGalleryAdapter? = null

    private lateinit var glideRequestManager: RequestManager

    private lateinit var acvAdapterMyGallery: AutoClearedValue<ProfileGalleryAdapter>
    private lateinit var gridLayoutManager: CustomStaggeredGridLayoutManager

    private var offsetAnimator: ValueAnimator? = null
    private var bottomNavHeight: Float = 0f
    private var navHalfHeight: Double = 0.0

    @field:Inject
    lateinit var viewModel: HomeProfileViewModel

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
        observeLiveData()
    }

    @Suppress("UNCHECKED_CAST")
    private fun observeLiveData() {
        val liveData = viewModel.gallery
        liveData.observe(this, Observer { resource ->

            when (resource?.getStatus()) {
                Status.SUCCESS -> {
                    resource.getData()?.let { list ->
                        val gallery = list as? PagedList<MyGallery>
                        gallery?.let {
                            if (it.isNotEmpty()) {
                                submitMyPhotos(it)
                            } else {
                                // show something to say that the list is empty
                            }
                        }
                    }
                    resource.getMessage()?.let {
                        homeActivity.showNetworkError(resource)
                        liveData.removeObservers(this)
                    }
                }
                Status.LOADING -> { }
                Status.ERROR -> {
                    liveData.removeObservers(this)
                }
                else -> {
                    liveData.removeObservers(this)
                }
            }
        })
    }


    private fun createMyPhotosAdapter() {
        adapter = adapter ?: ProfileGalleryAdapter(homeActivity)
        acvAdapterMyGallery = AutoClearedValue(this, adapter)
        this.recycler_profile_photos.setHasFixedSize(true)
        this.recycler_profile_photos.isVerticalScrollBarEnabled = false
        gridLayoutManager = CustomStaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        gridLayoutManager.enabledSrcoll = true
        gridLayoutManager.isItemPrefetchEnabled = true
        this.recycler_profile_photos.layoutManager = gridLayoutManager
        this.recycler_profile_photos.addItemDecoration(createItemDecoration())
        this.recycler_profile_photos.adapter = acvAdapterMyGallery.get()
    }

    private fun createItemDecoration(): RecyclerView.ItemDecoration {
        return object : GapItemDecoration(VERTICAL_LIST, resources.getDimensionPixelSize(R.dimen.space_4)) {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.left = 2
                outRect.right = 2
                outRect.bottom = 2

                // Add top margin only for the first item to avoid double space between items
                if (parent.getChildAdapterPosition(view) == 0 || parent.getChildAdapterPosition(view) == 1) {
                    outRect.top = 2
                }
            }
        }
    }

    private fun submitMyPhotos(myGallery: PagedList<MyGallery>) {
        acvAdapterMyGallery.get()?.submitList(myGallery)
    }

    internal fun setRecyclerScrollable(value: Boolean) {
        gridLayoutManager.enabledSrcoll = value
    }

    private fun setScrollAddListener() {
        this@HomeProfileGalleryFragment.recycler_profile_photos.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val vnView = homeActivity.layout_bottom_navigation

                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        if (bottomNavHeight >= navHalfHeight) {
                            animateBarVisibility(vnView, false)
                        } else {
                            animateBarVisibility(vnView, true)
                        }
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        homeActivity.closeFab()
                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> {}
                    else -> {}
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // val vnView = homeActivity.bottom_navigation_view
                val vnView = homeActivity.layout_bottom_navigation

                vnView.translationY = max(0f, min(vnView.height.toFloat(), vnView.translationY + dy))
                homeActivity.closeFab()

                bottomNavHeight = vnView.translationY
                navHalfHeight = vnView.height * 0.5
            }
        })
    }

    private fun animateBarVisibility(child: View, isVisible: Boolean) {
        offsetAnimator ?: createValueAnimator(child)
        offsetAnimator?.let {
            offsetAnimator?.cancel()
        }

        val targetTranslation = if (isVisible) 0f else child.height.toFloat()

        offsetAnimator?.setFloatValues(child.translationY, targetTranslation)
        offsetAnimator?.start()
    }

    private fun createValueAnimator(child: View) {
        offsetAnimator = ValueAnimator().apply {
            interpolator = DecelerateInterpolator()
            duration = 50L
        }

        offsetAnimator?.addUpdateListener {
            child.translationY = it.animatedValue as Float
        }
    }
}