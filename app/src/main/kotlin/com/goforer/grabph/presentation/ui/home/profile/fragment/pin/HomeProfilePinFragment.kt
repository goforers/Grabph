package com.goforer.grabph.presentation.ui.home.profile.fragment.pin

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.Photo
import com.goforer.grabph.data.datasource.network.response.Status
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.presentation.ui.home.profile.adapter.photos.ProfilePinAdapter
import com.goforer.grabph.presentation.vm.profile.HomeProfileViewModel
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_home_profile_photos.*
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class HomeProfilePinFragment: BaseFragment() {
    private var adapter: ProfilePinAdapter? = null
    private lateinit var glideRequestManager: RequestManager
    private lateinit var acvAdapter: AutoClearedValue<ProfilePinAdapter>
    internal val homeActivity: HomeActivity by lazy { activity as HomeActivity }
    private lateinit var gridLayoutManager: CustomStaggeredGridLayoutManager
    internal val recyclerView = this.recycler_profile_photos
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
        createPinnedPhotoAdapter()
        observeLiveData()
    }

    private fun observeLiveData() {
        val pin = viewModel.pin
        pin.observe(this, Observer { resource ->
            when (resource?.getStatus()) {
                Status.SUCCESS -> {
                    resource.getData()?.let { list ->
                        val pin = list as? PagedList<Photo>
                        pin?.let {
                            if (it.isNotEmpty()) {
                                submitPinnedPhotos(it)
                            }
                        }
                    }
                }
                Status.LOADING -> {
                    pin.removeObservers(this)
                }
                Status.ERROR -> {
                    pin.removeObservers(this)
                }
                else -> {
                    pin.removeObservers(this)
                }
            }
        })
    }

    private fun submitPinnedPhotos(photos: PagedList<Photo>) {
        if (::acvAdapter.isInitialized) acvAdapter.get()?.submitList(photos)
    }

    private fun createPinnedPhotoAdapter() {
        adapter = adapter ?: ProfilePinAdapter(homeActivity)
        acvAdapter = AutoClearedValue(this, adapter)
        this.recycler_profile_photos.setHasFixedSize(true)
        this.recycler_profile_photos.isVerticalScrollBarEnabled = false
        gridLayoutManager = CustomStaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        gridLayoutManager.enabledSrcoll = true
        gridLayoutManager.isItemPrefetchEnabled = true
        this.recycler_profile_photos.layoutManager = gridLayoutManager
        this.recycler_profile_photos.addItemDecoration(createItemDecoration())
        this.recycler_profile_photos.adapter = acvAdapter.get()
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

    private fun setScrollAddListener() {
        this@HomeProfilePinFragment.recycler_profile_photos.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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