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
import android.os.Build
import android.os.Bundle
import android.text.Html
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
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.presentation.ui.home.profile.adapter.ProfilePagerAdapter
import com.goforer.grabph.presentation.ui.home.profile.fragment.gallery.HomeProfileGalleryFragment
import com.goforer.grabph.presentation.vm.BaseViewModel.Companion.NONE_TYPE
import com.goforer.grabph.presentation.vm.profile.HomeProfileViewModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.MyProfile
import com.goforer.grabph.data.datasource.model.cache.data.mock.datasource.profile.ProfileDataSource
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.BOUND_FROM_BACKEND
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_MY_GALLERYG_PHOTO
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_MY_PROFILE
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_PHOTOG_PHOTO
import com.goforer.grabph.data.datasource.network.response.Status
import com.goforer.grabph.presentation.ui.home.profile.fragment.pin.HomeProfilePinFragment
import com.google.android.material.appbar.AppBarLayout
import javax.inject.Inject
import kotlin.reflect.full.findAnnotation
import kotlinx.android.synthetic.main.fragment_home_profile.*

@Suppress("UNCHECKED_CAST")
@RunWithMockData(true)
class HomeProfileFragment : BaseFragment() {

    @MockData val userId = "184804690@N02"
    @MockData val pinId = "34721981@N06"
    private val mock = this::class.findAnnotation<RunWithMockData>()?.mock!!

    private var pagerAdapter: ProfilePagerAdapter? = null

    private var appBarVerticalOffset = 0

    private lateinit var acvPagerAdapter: AutoClearedValue<ProfilePagerAdapter>

    private var myGalleryFragment: HomeProfileGalleryFragment? = null
    private var myPinFragment: HomeProfilePinFragment? = null
    // private var mySalesFragment: HomeProfileSalesFragment? = null

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

        const val FRAGMENT_KEY_HOME_GALLERY = "searp:fragment_home_gallery"
        const val FRAGMENT_KEY_HOME_PIN = "searp:fragment_home_pin"
        const val FRAGMENT_KEY_HOME_SALES = "searp:fragment_home_sales"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val acvView = AutoClearedValue(this, inflater.inflate(R.layout.fragment_home_profile, container, false))
        return acvView.get()?.rootView
    }

    @SuppressLint("SetTextI18n", "CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // this@HomeProfileFragment.appbar_home_profile.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener {
        //     appBarLayout, verticalOffset ->
        //     val vnView = homeActivity.bottom_navigation_view
        //
        //     when {
        //         abs(verticalOffset) == appBarLayout.totalScrollRange -> {
        //             vnView.translationY = abs(verticalOffset).toFloat()
        //         }
        //
        //         verticalOffset == 0 -> {
        //             vnView.translationY = 0L.toFloat()
        //         }
        //
        //         else -> {
        //             if (appBarVerticalOffset < verticalOffset) {
        //                 vnView.translationY = 0L.toFloat()
        //             } else {
        //                 vnView.translationY = max(0f, min(vnView.height.toFloat(), abs(verticalOffset).toFloat()))
        //             }
        //
        //             appBarVerticalOffset = verticalOffset
        //         }
        //     }
        // })

        setPagerAdapter(savedInstanceState)
        setViewClickListener()
        removeCache()
        getProfile()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        myGalleryFragment?.let { if (it.isAdded) fragmentManager?.putFragment(outState, FRAGMENT_KEY_HOME_GALLERY, it) }
        myPinFragment?.let { if (it.isAdded) fragmentManager?.putFragment(outState, FRAGMENT_KEY_HOME_PIN, it) }
        // mySalesFragment?.let { if (it.isAdded) fragmentManager?.putFragment(outState, FRAGMENT_KEY_HOME_SALES, it) }
    }

    private fun setPagerAdapter(savedInstanceState: Bundle?) {
        savedInstanceState?.let { getFragmentInstance(it) }

        myGalleryFragment = myGalleryFragment ?: HomeProfileGalleryFragment()
        myPinFragment = myPinFragment ?: HomeProfilePinFragment()
        // mySalesFragment = mySalesFragment ?: HomeProfileSalesFragment()

        pagerAdapter = pagerAdapter ?: ProfilePagerAdapter(requireFragmentManager())
        acvPagerAdapter = AutoClearedValue(this, pagerAdapter)

        myGalleryFragment?.let { acvPagerAdapter.get()?.addFragment(it, getString(R.string.my_profile_tab_photos)) }
        myPinFragment?.let { acvPagerAdapter.get()?.addFragment(it, getString(R.string.pinned_photo)) }
        // mySalesFragment?.let { acvPagerAdapter.get()?.addFragment(it, getString(R.string.my_profile_tab_sales)) }

        this@HomeProfileFragment.viewPager_profile.adapter = acvPagerAdapter.get()
        this@HomeProfileFragment.tabLayout_profile.setupWithViewPager(viewPager_profile)
    }

    private fun getFragmentInstance(savedInstanceState: Bundle) {
        myGalleryFragment = fragmentManager?.getFragment(savedInstanceState, FRAGMENT_KEY_HOME_GALLERY)?.let { it as HomeProfileGalleryFragment }
        myPinFragment = fragmentManager?.getFragment(savedInstanceState, FRAGMENT_KEY_HOME_PIN)?.let { it as HomeProfilePinFragment }
        // mySalesFragment = fragmentManager?.getFragment(savedInstanceState, FRAGMENT_KEY_HOME_SALES)?.let { it as HomeProfileSalesFragment }
    }

    private fun setViewClickListener() {
        this@HomeProfileFragment.profile_container_searper.setOnClickListener { startActivity(TAB_SEARPER_INDEX) }
        this@HomeProfileFragment.profile_container_searple.setOnClickListener { startActivity(TAB_SEARPLE_INDEX) }
        this@HomeProfileFragment.profile_container_like.setOnClickListener { startActivity(TAB_LIKE_INDEX) }
        this@HomeProfileFragment.profile_container_sell.setOnClickListener { startActivity(TAB_SELL_INDEX) }
        this.constraint_profile.setOnClickListener {
        }
    }

    private fun startActivity(tabType: Int) {
        when (tabType) {
            TAB_LIKE_INDEX -> { /*..*/ }
            else -> Caller.callPeopleList(this.activity as Activity, Caller.CALLED_FROM_HOME_PROFILE, tabType)
        }
    }

    private fun getProfile() {
        setViewLoading()
        setMyPageData()
        observeMyProfile()
    }

    @MockData
    private fun setMyPageData() {
        val homeProfile = ProfileDataSource()
        // homeProfile.setHomeProfile()
        // homeProfile.getHomeProfile()?.let {
        //     homeProfileViewModel.setHomeProfile(it)
        //     homeProfileViewModel.setHomeProfileLiveData(it)
        // }

        homeProfileViewModel.setParameters(
            Parameters(userId, // userId
                -1, LOAD_MY_PROFILE, BOUND_FROM_BACKEND), NONE_TYPE)

        homeProfileViewModel.setParametersMyGallery(
            Parameters(userId, -1, LOAD_MY_GALLERYG_PHOTO, BOUND_FROM_BACKEND))

        homeProfileViewModel.setParametersMyPin(
            Parameters(pinId, -1, LOAD_PHOTOG_PHOTO, BOUND_FROM_BACKEND)
        )
    }

    @SuppressLint("SetTextI18n")
    private fun observeMyProfile() {
        val liveData = homeProfileViewModel.profile
        liveData.observe(this, Observer { resource ->
            setViewLoadingFinished()

            when (resource?.getStatus()) {
                Status.SUCCESS -> {
                    resource.getData()?.let { profile ->

                        val user = profile as MyProfile?
                        if (user?.id == userId) setTopPortionView(user)
                    }
                    resource.getMessage()?.let {
                        // showNetworkError(resource)
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
            // setAppbarLayoutScrollingBehavior()
            setFontType()
        })
    }

    private fun setTopPortionView(profile: MyProfile) {
        this@HomeProfileFragment.tv_profile_profit.text = "Income: $${1250}$"
        this@HomeProfileFragment.tv_profile_point.text = "Point: ${1850}p"
        this@HomeProfileFragment.tv_profile_name.text = profile.realname?._content?.let {
            if (it.isEmpty()) profile.username?._content else it
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.tv_profile_coverLetter.text = Html.fromHtml(profile.description?._content, Html.FROM_HTML_MODE_LEGACY)
        } else {
            this@HomeProfileFragment.tv_profile_coverLetter.text = profile.description?._content
        }
        this@HomeProfileFragment.tv_profile_number_searper.text
        this@HomeProfileFragment.tv_profile_number_searple.text
        this@HomeProfileFragment.tv_profile_number_like.text
        this@HomeProfileFragment.tv_profile_number_sell.text
        homeActivity.setFixedImageSize(PHOTO_RATIO_HEIGHT, PHOTO_RATIO_WIDTH)
        val photoUrl = getProfilePhotoUrl(profile.iconfarm, profile.iconserver, profile.id)
        baseActivity.setImageDraw(this.iv_profile_icon, photoUrl)
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

    private fun getProfilePhotoUrl(iconFarm: Int, iconServer: String, id: String): String {
        return "https://farm$iconFarm.staticflickr.com/$iconServer/buddyicons/${id}_m.jpg"
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

    private fun removeCache() {
        homeProfileViewModel.removeGallery()
    }

    private fun setViewLoading() {
        this.progress_bar_home_profile_holder.visibility = View.VISIBLE
        this.appbar_home_profile.visibility = View.GONE
        this.viewPager_profile.visibility = View.GONE
    }

    private fun setViewLoadingFinished() {
        this.progress_bar_home_profile_holder.visibility = View.GONE
        this.appbar_home_profile.visibility = View.VISIBLE
        this.viewPager_profile.visibility = View.VISIBLE
    }
}
