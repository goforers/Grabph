package com.goforer.grabph.presentation.ui.home.profile.fragment.pin

import android.animation.ValueAnimator
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.goforer.base.presentation.view.customs.layout.CustomStaggeredGridLayoutManager
import com.goforer.base.presentation.view.decoration.GapItemDecoration
import com.goforer.base.presentation.view.fragment.BaseFragment
import com.goforer.grabph.R
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.LocalPin
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
    internal val homeActivity: HomeActivity by lazy { activity as HomeActivity }

    private var adapter: ProfilePinAdapter? = null

    private lateinit var gridLayoutManager: CustomStaggeredGridLayoutManager

    private lateinit var acvAdapter: AutoClearedValue<ProfilePinAdapter>

    private var offsetAnimator: ValueAnimator? = null
    private var bottomNavHeight: Float = 0f
    private var navHalfHeight: Double = 0.0

    @field:Inject
    lateinit var viewModel: HomeProfileViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val acvView =
            AutoClearedValue(this, inflater.inflate(R.layout.fragment_home_profile_photos, container, false))

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

        pin.observe(homeActivity, Observer { pins ->
            submitPinnedPhotos(pins)
            showEmptyPhotosMessage(pins.isEmpty())
            this.recycler_profile_photos.visibility = View.VISIBLE
            this.layout_before_loading_gallery.visibility = View.GONE
        })

        // pin.observe(homeActivity, Observer { resource ->
        //     when (resource?.getStatus()) {
        //         Status.SUCCESS -> {
        //             resource.getData()?.let { list ->
        //                 val pin = list as? PagedList<Photo>
        //                 pin?.let {
        //                     if (it.isNotEmpty()) {
        //                         submitPinnedPhotos(it)
        //                     }
        //                 }
        //             }
        //         }
        //         Status.LOADING -> {
        //             pin.removeObservers(this)
        //         }
        //         Status.ERROR -> {
        //             pin.removeObservers(this)
        //         }
        //         else -> {
        //             pin.removeObservers(this)
        //         }
        //     }
        // })
    }

    private fun submitPinnedPhotos(photos: List<LocalPin>) {
        if (::acvAdapter.isInitialized) acvAdapter.get()?.addList(photos)
        this.tv_empty_list.visibility = View.GONE
        this.recycler_profile_photos.visibility = View.VISIBLE
    }

    private fun showEmptyPhotosMessage(isEmpty: Boolean) {
        this.tv_empty_list.visibility = if (isEmpty) View.VISIBLE else View.GONE
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
        return object : GapItemDecoration(VERTICAL_LIST, resources.getDimensionPixelSize(R.dimen.space_4)) {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.left = 4
                outRect.right = 4
                outRect.bottom = 4

                // Add top margin only for the first item to avoid double space between items
                if (parent.getChildAdapterPosition(view) == 0 || parent.getChildAdapterPosition(view) == 1) {
                    outRect.top = 4
                }
            }
        }
    }

    private fun setScrollAddListener() {
        this@HomeProfilePinFragment.recycler_profile_photos.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                    else -> { }
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