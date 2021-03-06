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
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
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
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.BOUND_FROM_BACKEND
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_MY_GALLERYG_PHOTO
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_MY_PROFILE
import com.goforer.grabph.data.datasource.network.response.Status
import com.goforer.grabph.presentation.ui.home.profile.fragment.pin.HomeProfilePinFragment
import com.google.android.material.appbar.AppBarLayout
import javax.inject.Inject
import kotlin.reflect.full.findAnnotation
import kotlinx.android.synthetic.main.fragment_home_profile.*
import kotlinx.android.synthetic.main.layout_profile_photo_and_people.*
import kotlin.math.abs

@Suppress("UNCHECKED_CAST")
@RunWithMockData(true)
class HomeProfileFragment : BaseFragment() {
    @MockData val userId = "184804690@N02"
    @MockData val pinId = "34721981@N06"

    private val mock = this::class.findAnnotation<RunWithMockData>()?.mock!!

    private lateinit var userName: String
    private lateinit var userBackgroundPhoto: String
    private lateinit var acvPagerAdapter: AutoClearedValue<ProfilePagerAdapter>

    private lateinit var params: CoordinatorLayout.LayoutParams
    private lateinit var behavior: AppBarLayout.Behavior

    private var myGalleryFragment: HomeProfileGalleryFragment? = null
    private var myPinFragment: HomeProfilePinFragment? = null

    private var pagerAdapter: ProfilePagerAdapter? = null

    private var currentOffSet: Int = 0
    private var currentPage: Int = 0

    @SuppressLint("StaticFieldLeak")
    private lateinit var appBarLayout: AppBarLayout

    @field:Inject
    lateinit var homeProfileViewModel: HomeProfileViewModel

    private val homeActivity: HomeActivity by lazy { activity as HomeActivity }

    companion object {
        internal const val TAB_SEARPER_INDEX = 1
        internal const val TAB_SEARPLE_INDEX = 2
        internal const val TAB_SELL_INDEX = 3
        internal const val TAB_SETTING = 4

        private const val VIEWPAGER_GALLERY = 0
        private const val VIEWPAGER_PIN = 1

        private const val PHOTO_RATIO_WIDTH = 67
        private const val PHOTO_RATIO_HEIGHT = 67

        const val FRAGMENT_KEY_HOME_GALLERY = "searp:fragment_home_gallery"
        const val FRAGMENT_KEY_HOME_PIN = "searp:fragment_home_pin"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val acvView = AutoClearedValue(this, inflater.inflate(R.layout.fragment_home_profile, container, false))
        return acvView.get()?.rootView
    }

    @SuppressLint("SetTextI18n", "CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAppbarLayoutScrollingBehavior()
        setPagerAdapter(savedInstanceState)
        setViewClickListener()
        removeCache()
        getProfile()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        myGalleryFragment?.let { if (it.isAdded) homeActivity.supportFragmentManager.putFragment(outState, FRAGMENT_KEY_HOME_GALLERY, it) }
        myPinFragment?.let { if (it.isAdded) homeActivity.supportFragmentManager.putFragment(outState, FRAGMENT_KEY_HOME_PIN, it) }
    }

    private fun setPagerAdapter(savedInstanceState: Bundle?) {
        savedInstanceState?.let { getFragmentInstance(it) }

        myGalleryFragment = myGalleryFragment ?: HomeProfileGalleryFragment()
        myPinFragment = myPinFragment ?: HomeProfilePinFragment()

        pagerAdapter = pagerAdapter ?: ProfilePagerAdapter(homeActivity.supportFragmentManager)
        acvPagerAdapter = AutoClearedValue(this, pagerAdapter)

        myGalleryFragment?.let { acvPagerAdapter.get()?.addFragment(it, getString(R.string.my_profile_tab_photos)) }
        myPinFragment?.let { acvPagerAdapter.get()?.addFragment(it, getString(R.string.pinned_photo)) }

        this.viewPager_profile.adapter = acvPagerAdapter.get()
        this.tabLayout_profile.setupWithViewPager(viewPager_profile)

        this.tabLayout_profile.getTabAt(0)?.setIcon(R.drawable.ic_gallery_gradient)
        this.tabLayout_profile.getTabAt(1)?.setIcon(R.drawable.ic_bookmark_white)

        setPagerChangeListener()
    }

    private fun setPagerChangeListener() {
        this.viewPager_profile.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            @SuppressLint("MissingSuperCall")
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                this@HomeProfileFragment.tabLayout_profile.getTabAt(0)?.setIcon(R.drawable.ic_gallery_white)
                this@HomeProfileFragment.tabLayout_profile.getTabAt(1)?.setIcon(R.drawable.ic_bookmark_white)

                when (position) {
                    VIEWPAGER_GALLERY -> this@HomeProfileFragment.tabLayout_profile.getTabAt(position)?.setIcon(R.drawable.ic_gallery_gradient)
                    VIEWPAGER_PIN -> this@HomeProfileFragment.tabLayout_profile.getTabAt(position)?.setIcon(R.drawable.ic_bookmark_gradient)
                }

                currentPage = position // 0 or 1
            }
        })
    }

    private fun getFragmentInstance(savedInstanceState: Bundle) {
        myGalleryFragment = homeActivity.supportFragmentManager
            .getFragment(savedInstanceState, FRAGMENT_KEY_HOME_GALLERY)?.let { it as HomeProfileGalleryFragment }

        myPinFragment = homeActivity.supportFragmentManager
            .getFragment(savedInstanceState, FRAGMENT_KEY_HOME_PIN)?.let { it as HomeProfilePinFragment }
    }

    private fun setViewClickListener() {
        this.profile_container_following.setOnClickListener { startActivity(TAB_SEARPER_INDEX) }
        this.profile_container_follower.setOnClickListener { startActivity(TAB_SEARPLE_INDEX) }
        this.profile_container_pin.setOnClickListener { startActivity(TAB_SELL_INDEX) }
        this.ib_profile_setting.setOnClickListener { startActivity(TAB_SETTING) }
        this.iv_profile_arrow_up.setOnClickListener { appBarLayout.setExpanded(false, true) }
        this.ib_profile_notification.setOnClickListener {}
        this.constraint_profile.setOnClickListener {}
        this.fam_gallery_top.setOnClickListener { setPhotoListPositionToTop() }
    }

    private fun setPhotoListPositionToTop() {
        homeProfileViewModel.setPagerFab(currentPage)
    }

    private fun startActivity(tabType: Int) {
        when (tabType) {
            TAB_SETTING -> Caller.callSetting(this.activity as Activity)
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
        setBackgroundImageForMock()

        homeProfileViewModel.setParameters(
            Parameters(userId, // userId
                -1, LOAD_MY_PROFILE, BOUND_FROM_BACKEND), NONE_TYPE)

        homeProfileViewModel.setParametersMyGallery(
            Parameters(userId, -1, LOAD_MY_GALLERYG_PHOTO, BOUND_FROM_BACKEND))

        homeProfileViewModel.setParametersMyPin(userId)

        // homeProfileViewModel.setParametersMyPin(
        //     Parameters(pinId, -1, LOAD_PHOTOG_PHOTO, BOUND_FROM_BACKEND)
        // )
    }

    @SuppressLint("SetTextI18n")
    private fun observeMyProfile() {
        val liveData = homeProfileViewModel.profile
        liveData.observe(homeActivity, Observer { resource ->
            setViewLoadingFinished()

            when (resource?.getStatus()) {
                Status.SUCCESS -> {
                    resource.getData()?.let { profile ->

                        val user = profile as MyProfile?
                        if (user?.id == userId) setTopPortionView(user)
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
            setFontType()
        })
    }

    private fun setTopPortionView(profile: MyProfile) {
        homeActivity.setImageDraw(this.iv_profile_title_photo, userBackgroundPhoto)
        this.iv_profile_title_photo.scaleType = ImageView.ScaleType.CENTER_CROP
        userName = profile.realname?._content?.let {
            if (it.isEmpty()) profile.username?._content else it
        } ?: getString(R.string.unknown_user_eng)
        this.tv_profile_name.text = userName
        this.tv_home_profile_title.text = userName
        this.tv_home_profile_title.visibility = View.GONE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            this.tv_profile_coverLetter.text = Html.fromHtml(profile.description?._content, Html.FROM_HTML_MODE_LEGACY)
        else this.tv_profile_coverLetter.text = profile.description?._content

        this.tv_profile_number_following.text
        this.tv_profile_number_follower.text
        this.tv_profile_number_pin.text

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
    }

    private fun setAppbarOffsetChangedListener() {
        var alphaForDesc: Int
        var expandingPercentage: Float
        var collapsingPercentage: Float

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener {
                appBarLayout, verticalOffset ->

            currentOffSet = abs(verticalOffset)
            expandingPercentage = (currentOffSet.toFloat() / appBarLayout.totalScrollRange.toFloat())
            collapsingPercentage = 1f - expandingPercentage
            alphaForDesc = (255 * collapsingPercentage).toInt()

            setViewAlpha(alphaForDesc)

            when (abs(verticalOffset)) {
                appBarLayout.totalScrollRange -> setLayoutCollapsed()

                0 -> setLayoutExpanded()

                else -> setLayoutMoving()
            }
        })
    }

    private fun setViewAlpha(alphaForDesc: Int) {
        this.constraint_holder_description.background.alpha = alphaForDesc
        this.tv_profile_coverLetter.setTextColor(this.tv_profile_coverLetter.textColors.withAlpha(alphaForDesc))
    }

    private fun setLayoutCollapsed() {
        this.iv_profile_arrow_up.visibility = View.GONE
        this.tv_home_profile_title.visibility = View.VISIBLE
        this.fam_gallery_top.visibility = View.VISIBLE
    }

    private fun setLayoutExpanded() {
        this.fam_gallery_top.visibility = View.GONE
    }

    private fun setLayoutMoving() {
        this.iv_profile_arrow_up.visibility = View.VISIBLE
        this.tv_home_profile_title.visibility = View.GONE
        this.fam_gallery_top.visibility = View.GONE
    }

    private fun getProfilePhotoUrl(iconFarm: Int, iconServer: String, id: String): String {
        return "https://farm$iconFarm.staticflickr.com/$iconServer/buddyicons/${id}_m.jpg"
    }

    private fun setFontType() {
        homeActivity.run {
            FONT_TYPE_REGULAR.let {
                setFontTypeface(tv_profile_coverLetter, it)
                setFontTypeface(btn_profile_edit, it)
            }

            FONT_TYPE_BOLD.let {
                setFontTypeface(tv_profile_name, it)
                setFontTypeface(tv_profile_number_following, it)
                setFontTypeface(tv_profile_number_follower, it)
                setFontTypeface(tv_profile_number_pin, it)
                setFontTypeface(tv_home_profile_title, it)
            }

            FONT_TYPE_MEDIUM.let {
                setFontTypeface(tv_profile_text_following, it)
                setFontTypeface(tv_profile_text_follower, it)
                setFontTypeface(tv_profile_text_pin, it)
            }
        }
    }

    @MockData
    private fun setBackgroundImageForMock() {
        val images = ArrayList<String>()
        images.add("https://images.unsplash.com/photo-1566198602178-f4d01944794e?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=934&q=80")
        images.add("https://images.unsplash.com/photo-1566154247339-727cd5276a42?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=935&q=80")
        images.add("https://images.unsplash.com/photo-1565949860070-a6c44d127f88?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=934&q=80")
        images.add("https://images.unsplash.com/photo-1566154247159-2d392eb80e28?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1649&q=80")
        images.add("https://images.unsplash.com/photo-1566031687073-e328f7343951?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=934&q=80")
        images.add("https://images.unsplash.com/photo-1541795795328-f073b763494e?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=934&q=80")
        images.add("https://images.unsplash.com/photo-1546241072-48010ad2862c?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=934&q=80")
        images.shuffle()
        userBackgroundPhoto = images[0]
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
