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

package com.goforer.grabph.presentation.ui.home.profile.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.goforer.base.annotation.MockData
import com.goforer.base.annotation.RunWithMockData
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_BOLD
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_MEDIUM
import com.goforer.base.presentation.view.activity.BaseActivity.Companion.FONT_TYPE_REGULAR
import com.goforer.base.presentation.view.fragment.BaseFragment
import com.goforer.grabph.R
import com.goforer.grabph.domain.usecase.Parameters
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.presentation.ui.home.profile.adapter.ProfilePagerAdapter
import com.goforer.grabph.presentation.ui.home.profile.fragment.photos.HomeProfilePhotosFragment
import com.goforer.grabph.presentation.ui.home.profile.fragment.sales.HomeProfileSalesFragment
import com.goforer.grabph.presentation.vm.BaseViewModel.Companion.NONE_TYPE
import com.goforer.grabph.presentation.vm.profile.HomeProfileViewModel
import com.goforer.grabph.repository.model.cache.data.entity.profile.HomeProfile
import com.goforer.grabph.repository.model.cache.data.mock.datasource.profile.ProfileDataSource
import com.goforer.grabph.repository.network.resource.NetworkBoundResource.Companion.BOUND_FROM_LOCAL
import com.goforer.grabph.repository.network.resource.NetworkBoundResource.Companion.LOAD_HOME_PROFILE
import com.goforer.grabph.repository.network.response.Status
import com.google.android.material.appbar.AppBarLayout
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.full.findAnnotation
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_home_profile.*

@RunWithMockData(true)
class HomeProfileFragment : BaseFragment() {
    private val mock = this::class.findAnnotation<RunWithMockData>()?.mock!!

    private var pagerAdapter: ProfilePagerAdapter? = null

    private var appBarVerticalOffset = 0

        private lateinit var acvPagerAdapter: AutoClearedValue<ProfilePagerAdapter>

    private var myPhotosFragment: HomeProfilePhotosFragment? = null
    private var mySalesFragment: HomeProfileSalesFragment? = null

    internal var isAppbarExpanded = true

    private lateinit var params: CoordinatorLayout.LayoutParams
    private lateinit var behavior: AppBarLayout.Behavior
    @SuppressLint("StaticFieldLeak")
    private lateinit var appBarLayout: AppBarLayout

    @SuppressLint("StaticFieldLeak")
    private lateinit var photosRv: RecyclerView

    @field:Inject
    lateinit var homeProfileViewModel: HomeProfileViewModel

    private val homeActivity: HomeActivity by lazy { activity as HomeActivity }

    companion object {
        internal const val TAB_SEARPER_INDEX = 1
        internal const val TAB_SEARPLE_INDEX = 2
        internal const val TAB_LIKE_INDEX = 3
        internal const val TAB_SELL_INDEX = 4

        private const val PHOTO_RATIO_WIDTH = 67
        private const val PHOTO_RATIO_HEIGHT = 67

        const val FRAGMENT_KEY_HOME_PHOTOS = "searp:fragment_home_photos"
        const val FRAGMENT_KEY_HOME_SALES = "searp:fragment_home_sales"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val acvView = AutoClearedValue(this, inflater.inflate(R.layout.fragment_home_profile, container, false))

        return acvView.get()?.rootView
    }

    @SuppressLint("SetTextI18n", "CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPagerAdapter(savedInstanceState)

        this@HomeProfileFragment.appbar_home_profile.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener {
            appBarLayout, verticalOffset ->
            val vnView = homeActivity.bottom_navigation_view

            when {
                abs(verticalOffset) == appBarLayout.totalScrollRange -> {
                    vnView.translationY = abs(verticalOffset).toFloat()
                }

                verticalOffset == 0 -> {
                    vnView.translationY = 0L.toFloat()
                }

                else -> {
                    if (appBarVerticalOffset < verticalOffset) {
                        vnView.translationY = 0L.toFloat()
                    } else {
                        vnView.translationY = max(0f, min(vnView.height.toFloat(), abs(verticalOffset).toFloat()))
                    }

                    appBarVerticalOffset = verticalOffset
                }
            }
        })

        setViewClickListener()
        observeProfileLiveData()
        if (savedInstanceState == null) getProfile() else homeProfileViewModel.loadProfileFromCache()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        myPhotosFragment?.let { if (it.isAdded) fragmentManager?.putFragment(outState, FRAGMENT_KEY_HOME_PHOTOS, it) }
        mySalesFragment?.let { if (it.isAdded) fragmentManager?.putFragment(outState, FRAGMENT_KEY_HOME_SALES, it) }
    }

    private fun setPagerAdapter(savedInstanceState: Bundle?) {
        savedInstanceState?.let { getFragmentInstance(it) }

        myPhotosFragment = myPhotosFragment ?: HomeProfilePhotosFragment()
        mySalesFragment = mySalesFragment ?: HomeProfileSalesFragment()

        pagerAdapter = pagerAdapter ?: ProfilePagerAdapter(requireFragmentManager())
        acvPagerAdapter = AutoClearedValue(this, pagerAdapter)

        myPhotosFragment?.let { acvPagerAdapter.get()?.addFragment(it, getString(R.string.my_profile_tab_photos)) }
        mySalesFragment?.let { acvPagerAdapter.get()?.addFragment(it, getString(R.string.my_profile_tab_sales)) }

        this@HomeProfileFragment.viewPager_profile.adapter = acvPagerAdapter.get()
        this@HomeProfileFragment.tabLayout_profile.setupWithViewPager(viewPager_profile)
    }

    private fun getFragmentInstance(savedInstanceState: Bundle) {
        myPhotosFragment = fragmentManager?.getFragment(savedInstanceState, FRAGMENT_KEY_HOME_PHOTOS)?.let { it as HomeProfilePhotosFragment }
        mySalesFragment = fragmentManager?.getFragment(savedInstanceState, FRAGMENT_KEY_HOME_SALES)?.let { it as HomeProfileSalesFragment }
    }

    private fun setViewClickListener() {
        this@HomeProfileFragment.profile_container_searper.setOnClickListener { startActivity(TAB_SEARPER_INDEX) }
        this@HomeProfileFragment.profile_container_searple.setOnClickListener { startActivity(TAB_SEARPLE_INDEX) }
        this@HomeProfileFragment.profile_container_like.setOnClickListener { startActivity(TAB_LIKE_INDEX) }
        this@HomeProfileFragment.profile_container_sell.setOnClickListener { startActivity(TAB_SELL_INDEX) }
    }

    private fun startActivity(tabType: Int) {
        when (tabType) {
            TAB_LIKE_INDEX -> { /*..*/ }
            else -> Caller.callPeopleList(this.activity as Activity, Caller.CALLED_FROM_HOME_PROFILE, tabType)
        }
    }

    private fun getProfile() {
        when (mock) {
            @MockData
            true -> transactMockData()
            false -> transactRealData()
        }
    }

    private fun observeProfileLiveData() {
        homeProfileViewModel.getHomeProfileLiveData().observe(this, Observer {
            setTopPortionView(it)
            setBottomPortionViews(it)
        })
    }

    @MockData
    private fun transactMockData() {
        val homeProfile = ProfileDataSource()

        homeProfile.setHomeProfile()
        homeProfile.getHomeProfile()?.let {
            homeProfileViewModel.setHomeProfile(it)
            homeProfileViewModel.setHomeProfileLiveData(it)
        }
    }

    private fun transactRealData() {
        val liveData = homeProfileViewModel.profile

        homeProfileViewModel.setParameters(Parameters("", -1, LOAD_HOME_PROFILE, BOUND_FROM_LOCAL), NONE_TYPE)
        liveData.observe(this, Observer { resource ->
            when (resource?.getStatus()) {

                Status.SUCCESS -> {
                    resource.getData()?.let { homeProfile ->
                        val profile = homeProfile as HomeProfile

                        homeProfileViewModel.setHomeProfileLiveData(profile)
                    }

                    resource.getMessage()?.let {
                        homeActivity.showNetworkError(resource)
                        liveData.removeObservers(this)
                    }
                }

                Status.LOADING -> { /*로딩 이미지 구현 필요*/ }

                Status.ERROR -> {
                    homeActivity.showNetworkError(resource)
                    liveData.removeObservers(this)
                }

                else -> {
                    homeActivity.showNetworkError(resource)
                    liveData.removeObservers(this)
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setTopPortionView(profile: HomeProfile) {
        setFontType()

        this@HomeProfileFragment.tv_profile_profit.text = "Income: $${profile.revenue}"
        this@HomeProfileFragment.tv_profile_point.text = "Point: ${profile.point}p"
        this@HomeProfileFragment.tv_profile_name.text = profile.realname
        this@HomeProfileFragment.tv_profile_coverLetter.text = profile.coverletter
        this@HomeProfileFragment.tv_profile_number_searper.text = "${profile.following}"
        this@HomeProfileFragment.tv_profile_number_searple.text = "${profile.follower}"
        this@HomeProfileFragment.tv_profile_number_like.text = "${profile.like}"
        this@HomeProfileFragment.tv_profile_number_sell.text = "${profile.sold}"
        homeActivity.setFixedImageSize(PHOTO_RATIO_HEIGHT, PHOTO_RATIO_WIDTH)
        profile.profilePhoto?.let { homeActivity.setImageDraw(iv_profile_icon, constraint_profile, it, false) }

        setAppbarLayoutScrollingBehavior()
    }

    private fun setBottomPortionViews(profile: HomeProfile) {
        myPhotosFragment?.setMyPhotosView(profile.sellPhotos.photos)
        myPhotosFragment?.recyclerView?.let { photosRv = it }
//        setViewPagerSwipeListener()
    }

    private fun setAppbarLayoutScrollingBehavior() {
        this.appBarLayout = this.appbar_home_profile
        this.params = appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
        this.params.behavior = AppBarLayout.Behavior()
        this.behavior = params.behavior as AppBarLayout.Behavior

        setAppbarOffsetChangedListener()
        setCoordinateLayoutSwipeListener()
    }

    private fun setAppbarOffsetChangedListener() {
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener {
            appBarLayout, verticalOffset ->

//            when {
//                abs(verticalOffset) == appBarLayout.totalScrollRange -> {
//                    isAppbarExpanded = false
//                    myPhotosFragment?.setRecyclerScrollable(true)
//
//                }
//                abs(verticalOffset) == 0 -> {
//                    enableAppBarDraggable(false)
//                    isAppbarExpanded = true
//                    myPhotosFragment?.setRecyclerScrollable(false)
//                }
//                else -> {
//                    isAppbarExpanded = false
//                }
//            }
        })
    }

    private fun setCoordinateLayoutSwipeListener() {
        var swipeUp = false
        var swipeDown = false

//        this.coordinator_home_profile_layout.setOnSwipeOutListener(homeActivity, object : OnSwipeOutListener {
//            override fun onSwipeLeft(x: Float, y: Float) { }
//            override fun onSwipeRight(x: Float, y: Float) { }
//
//            override fun onSwipeDown(x: Float, y: Float) {
//                swipeUp = false
//                swipeDown = true
//            }
//
//            override fun onSwipeUp(x: Float, y: Float) {
//                swipeUp = true
//                swipeDown = false
//            }
//
//            override fun onSwipeDone() {
//                if (swipeUp) {
//                    appBarLayout.setExpanded(false, true)
//                }
//
//                if (swipeDown && !isAppbarExpanded) {
//                    appBarLayout.setExpanded(true, true)
//                    enableAppBarDraggable(true)
//                }
//
//                swipeUp = false
//                swipeDown = false
//            }
//        })
    }

    private fun setViewPagerSwipeListener() {
//        this.viewPager_profile.setOnSwipeOutListener(object : SwipeViewPager.OnSwipeOutListener {
//            override fun onSwipeOutAtStart() {
//            }
//
//            override fun onSwipeOutAtEnd() {
//            }
//
//            override fun onSwipeLeft(x: Float, y: Float) {
//            }
//
//            override fun onSwipeRight(x: Float, y: Float) {
//            }
//
//            override fun onSwipeDown(x: Float, y: Float) {
//            }
//
//            override fun onSwipeUp(x: Float, y: Float) {
//            }
//        })
    }

    private fun enableAppBarDraggable(draggable: Boolean) {
        behavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() { // block dragging behavior on appBarLayout
            override fun canDrag(p0: AppBarLayout): Boolean { return draggable }
        })
    }

    private fun setFontType() {
        homeActivity.run {
            FONT_TYPE_REGULAR.let {
                setFontTypeface(tv_profile_profit, it)
                setFontTypeface(tv_profile_point, it)
                setFontTypeface(tv_profile_coverLetter, it)
                setFontTypeface(btn_profile_edit, it)
            }

            FONT_TYPE_BOLD.let {
                setFontTypeface(tv_profile_name, it)
                setFontTypeface(tv_profile_number_searper, it)
                setFontTypeface(tv_profile_number_searple, it)
                setFontTypeface(tv_profile_number_like, it)
                setFontTypeface(tv_profile_number_sell, it)
            }

            FONT_TYPE_MEDIUM.let {
                setFontTypeface(tv_profile_text_searper, it)
                setFontTypeface(tv_profile_text_searple, it)
                setFontTypeface(tv_profile_text_like, it)
                setFontTypeface(tv_profile_text_sell, it)
            }
        }
    }
}
