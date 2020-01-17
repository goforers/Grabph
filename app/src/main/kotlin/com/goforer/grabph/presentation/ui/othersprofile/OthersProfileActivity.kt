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

package com.goforer.grabph.presentation.ui.othersprofile

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.text.Html
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.AppCompatButton
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.goforer.base.annotation.MockData
import com.goforer.base.annotation.RunWithMockData
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.base.presentation.view.customs.layout.CustomStaggeredGridLayoutManager
import com.goforer.base.presentation.view.customs.listener.OnSwipeOutListener
import com.goforer.grabph.R
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_FEED_INFO
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_HOME_MAIN
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_PHOTO_INFO
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PLACE_CALLED_USER_PROFILE
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PROFILE_USER_ID
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PROFILE_USER_NAME
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PROFILE_USER_PHOTO_URL
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PROFILE_USER_RANKING
import com.goforer.grabph.presentation.ui.othersprofile.adapter.OthersProfileAdapter
import com.goforer.grabph.presentation.vm.BaseViewModel.Companion.NONE_TYPE
import com.goforer.grabph.presentation.vm.othersprofile.OthersProfileViewModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.Person
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.BOUND_FROM_BACKEND
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_PERSON
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.data.datasource.network.response.Status
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.ui.home.profile.adapter.ProfilePagerAdapter
import com.goforer.grabph.presentation.ui.othersprofile.fragments.OthersProfileGalleryFragment
import com.goforer.grabph.presentation.ui.othersprofile.fragments.OthersProfilePinFragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.reflect.full.findAnnotation
import kotlinx.android.synthetic.main.activity_others_profile.*
import kotlinx.android.synthetic.main.layout_disconnection.*
import kotlinx.android.synthetic.main.layout_profile_photo_and_people.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

@SuppressLint("Registered")
@RunWithMockData(true)
class OthersProfileActivity : BaseActivity() {
    private val mock = this::class.findAnnotation<RunWithMockData>()?.mock!!

    private lateinit var userId: String
    private lateinit var userName: String
    private lateinit var userPhotoUrl: String
    private lateinit var userBackgroundPhoto: String

    private val job = Job()
    private val ioScope = CoroutineScope(Dispatchers.IO + job)

    private lateinit var params: CoordinatorLayout.LayoutParams
    private lateinit var behavior: AppBarLayout.Behavior
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var gridLayoutManager: CustomStaggeredGridLayoutManager

    private var galleryFragment: OthersProfileGalleryFragment? = null
    private var pinFragment: OthersProfilePinFragment? = null

    private var page: Int = -1
    private var userRanking: Int = 0
    private var calledFrom: Int = 0
    private var halfOffsetAppBar: Int = 0
    private var currentOffSet: Int = 0
    private var isAppBarExpanded = true
    private var isRecyclerTop = true

    private var adapter: OthersProfileAdapter? = null

    companion object {
        const val FRAGMENT_KEY_PROFILE_GALLERY = "searp:fragment_others_gallery"
        const val FRAGMENT_KEY_PROFILE_PIN = "searp:fragment_others_pin"
    }

    @field:Inject
    lateinit var viewModel: OthersProfileViewModel

    override fun setContentView() { setContentView(R.layout.activity_others_profile) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isNetworkAvailable) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.TRANSPARENT
            networkStatusVisible(true)
            showProgressBarLoading()
        } else {
            networkStatusVisible(false)
        }
    }

    override fun setViews(savedInstanceState: Bundle?) {
        super.setViews(savedInstanceState)
        getIntentData(savedInstanceState)
        setPageAdapter(savedInstanceState)
        setButtonsClickListener()
        setAppbarScrollingBehavior()
        removeCache()
        getProfileAfterClearCache()
        setFontType()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.run {
            putString(EXTRA_PROFILE_USER_ID, userId)
            putString(EXTRA_PROFILE_USER_NAME, userName)
            putInt(EXTRA_PROFILE_USER_RANKING, userRanking)
            putString(EXTRA_PROFILE_USER_PHOTO_URL, userPhotoUrl)
            putInt(EXTRA_PLACE_CALLED_USER_PROFILE, calledFrom)
        }

        galleryFragment?.let { if (it.isAdded) supportFragmentManager.putFragment(outState, FRAGMENT_KEY_PROFILE_GALLERY, it) }
        pinFragment?.let { if (it.isAdded) supportFragmentManager.putFragment(outState, FRAGMENT_KEY_PROFILE_PIN, it) }
    }

    override fun setActionBar() {
        super.setActionBar()
        setSupportActionBar(this.toolbar_others_profile)
        val actionBar = supportActionBar
        actionBar?.let {
            it.setHomeAsUpIndicator(R.drawable.ic_back)
            it.displayOptions = ActionBar.DISPLAY_HOME_AS_UP or ActionBar.DISPLAY_USE_LOGO
            it.setDisplayShowTitleEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        this.toolbar_others_profile.hideOverflowMenu()
    }

    override fun onDestroy() {
        super.onDestroy()

        ioScope.cancel()
        job.cancel()
    }

    private fun setPageAdapter(savedInstanceState: Bundle?) {
        savedInstanceState?.let { getFragmentInstance(it) }

        galleryFragment = galleryFragment ?: OthersProfileGalleryFragment.newInstance(userId)
        pinFragment = pinFragment ?: OthersProfilePinFragment()

        val pagerAdapter = ProfilePagerAdapter(supportFragmentManager)

        galleryFragment?.let { pagerAdapter.addFragment(it, "") }
        pinFragment?.let { pagerAdapter.addFragment(it, "") }

        this.viewpager_others_profile.adapter = pagerAdapter
        this.tablayout_others_profile.setupWithViewPager(this.viewpager_others_profile)

        this.tablayout_others_profile.getTabAt(0)?.setIcon(R.drawable.ic_gallery_gradient)
        this.tablayout_others_profile.getTabAt(1)?.setIcon(R.drawable.ic_bookmark_white)

        setPagerChangeListener()
    }

    private fun getFragmentInstance(savedInstanceState: Bundle) {
        galleryFragment = supportFragmentManager.getFragment(savedInstanceState, FRAGMENT_KEY_PROFILE_GALLERY)?.let {
            it as OthersProfileGalleryFragment
        }

        pinFragment = supportFragmentManager.getFragment(savedInstanceState, FRAGMENT_KEY_PROFILE_PIN)?.let {
            it as OthersProfilePinFragment
        }
    }

    private fun getProfileAfterClearCache() {
        viewModel.isCleared.observe(this, Observer { isCleared ->
            if (isCleared) {
                when (mock) {
                    @MockData
                    true -> getMockProfile()
                    false -> getRealProfile()
                }
            }
            viewModel.isCleared.removeObservers(this)
        })
    }

    private fun setPagerChangeListener() {
        this.viewpager_others_profile.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                this@OthersProfileActivity.tablayout_others_profile.getTabAt(0)?.setIcon(R.drawable.ic_gallery_white)
                this@OthersProfileActivity.tablayout_others_profile.getTabAt(1)?.setIcon(R.drawable.ic_bookmark_white)

                when (position) {
                    0 -> this@OthersProfileActivity.tablayout_others_profile.getTabAt(position)?.setIcon(R.drawable.ic_gallery_gradient)
                    1 -> this@OthersProfileActivity.tablayout_others_profile.getTabAt(position)?.setIcon(R.drawable.ic_bookmark_gradient)
                }
            }
        })
    }

    @MockData
    private fun getMockProfile() {
        setBackgroundImageForMock()
        when (calledFrom) {
            CALLED_FROM_HOME_MAIN, CALLED_FROM_FEED_INFO, CALLED_FROM_PHOTO_INFO -> {
                viewModel.setParameters(
                    Parameters(userId, -1, LOAD_PERSON, BOUND_FROM_BACKEND),
                    NONE_TYPE)

                val liveData = viewModel.profile
                liveData.observe(this, Observer { resource ->
                    when (resource?.getStatus()) {
                        Status.SUCCESS -> {
                            resource.getData().let { person ->
                                val user = person as Person?
                                if (user?.id == userId) setTopPortionViewData(user)
                            }

                            resource.getMessage()?.let {
                                showNetworkError(resource)
                                liveData.removeObservers(this)
                            }
                        }

                        Status.LOADING -> {}

                        Status.ERROR -> {
                            showNetworkError(resource)
                            liveData.removeObservers(this)
                        }

                        else -> {
                            showNetworkError(resource)
                            liveData.removeObservers(this)
                        }
                    }
                })
            }

            else -> {
                val mockPerson = Person(
                    userId,
                    userId,
                    "",
                    0,
                    Person.Username(userName),
                    Person.Realname(""),
                    Person.Location(""),
                    Person.Description(getString(R.string.description_sample)),
                    Person.Photosurl(""),
                    Person.Profileurl(userPhotoUrl),
                    Person.Mobileurl(""),
                    Person.Photos(
                        Person.Photos.Firstdatetaken(""),
                        Person.Photos.Firstdate(""),
                        Person.Photos.Count("49"))
                )
                mockPerson.followers = "24"
                mockPerson.followings = "45"
                setTopPortionViewData(mockPerson)
            }
        }
    }

    private fun getRealProfile() {
        viewModel.setParameters(
            Parameters(userId, -1, LOAD_PERSON, BOUND_FROM_BACKEND),
            NONE_TYPE)

        val liveData = viewModel.profile
        liveData.observe(this, Observer { resource ->
            when (resource?.getStatus()) {
                Status.SUCCESS -> {
                    resource.getData().let { person ->
                        val user = person as Person?
                        if (user?.id == userId) setTopPortionViewData(user)
                    }

                    resource.getMessage()?.let {
                        showNetworkError(resource)
                        liveData.removeObservers(this)
                    }
                }

                Status.LOADING -> {}

                Status.ERROR -> {
                    showNetworkError(resource)
                    liveData.removeObservers(this)
                }

                else -> {
                    showNetworkError(resource)
                    liveData.removeObservers(this)
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setTopPortionViewData(person: Person) {
        setRankColor(userRanking)
        if (person.iconserver == "0") {
            this.iv_profile_icon.setImageDrawable(getDrawable(R.drawable.ic_default_profile))
        } else {
            setImageDraw(this.iv_profile_icon, userPhotoUrl)
        }
        showLoadingFinished()

        // profile.backgroundPhoto?.let { userBackgroundPhoto = it }
        setFixedImageSize(0, 0)
        setImageDraw(this.iv_others_profile_title_photo, userBackgroundPhoto)

        this.tv_others_profile_title.text = userName
        this.tv_others_profile_title.visibility = View.GONE
        this.iv_others_profile_title_photo.scaleType = ImageView.ScaleType.CENTER_CROP
        this.tv_profile_name.text = person.realname?._content?.let {
            if (it.isEmpty()) person.username?._content else it
        } ?: person.username?._content
        val desc = getDescription(person.description?._content!!, userId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.tv_others_profile_coverLetter.text = Html.fromHtml(desc, Html.FROM_HTML_MODE_LEGACY)
        } else {
            this.tv_others_profile_coverLetter.text = desc
        }
        this.tv_profile_number_following.text = person.followings ?: "35"
        this.tv_profile_number_follower.text = person.followers ?: "46"
        this.tv_photo_number_others_profile.text = "${person.photos?.count?._content} Photos"
        this.btn_follow_bottom_others_profile.translationY = this.btn_follow_bottom_others_profile.height.toFloat()
    }

    private fun setButtonsClickListener() {
        this.btn_follow_others_profile.isSelected = false

        this.btn_follow_others_profile.setOnClickListener {
            if (it.isSelected) {
                (it as AppCompatButton).text = getString(R.string.follow_button)
                it.setBackgroundResource(R.drawable.border_of_upload_category_white)
            } else {
                (it as AppCompatButton).text = getString(R.string.following_button)
                it.setBackgroundResource(R.drawable.ic_border_of_following)
            }

            if (it.isSelected) this.btn_follow_bottom_others_profile.text = getString(R.string.follow_button)
            else this.btn_follow_bottom_others_profile.text = getString(R.string.follow_button_bottom)

            it.isSelected = !it.isSelected
        }

        this.btn_follow_bottom_others_profile.setOnClickListener { button ->
            (button as AppCompatButton).text = if (this.btn_follow_others_profile.isSelected) getString(R.string.follow_button) else getString(R.string.follow_button_bottom)
            this.btn_follow_others_profile.let {
                it.text = if (it.isSelected) getString(R.string.follow_button) else getString(R.string.following_button)
                it.setBackgroundResource(if (it.isSelected) R.drawable.border_of_upload_category_white else R.drawable.ic_border_of_following)
            }
            this.btn_follow_others_profile.isSelected = ! this.btn_follow_others_profile.isSelected
        }

        this.iv_others_profile_arrow_up.setOnClickListener { appBarLayout.setExpanded(false, true) }

        this.tv_profile_number_following.setOnClickListener { /* see my following */ }
        this.tv_profile_number_follower.setOnClickListener { /* see my follower */ }
        this.tv_profile_number_pin.setOnClickListener { /* see my pins */ }
        this.iv_profile_icon.setOnClickListener {}
        this.profile_container_following.setOnClickListener {
            Caller.callPeopleList(this, Caller.CALLED_FROM_HOME_PROFILE, 1)
        }
    }

    private fun setAppbarScrollingBehavior() {
        this.appBarLayout = this.appbar_others_profile
        this.params = appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
        this.params.behavior = AppBarLayout.Behavior()
        this.behavior = params.behavior as AppBarLayout.Behavior

        setAppBarOffsetChangedListener()
        setCoordinateLayoutSwipeListener()
    }

    private fun setAppBarOffsetChangedListener() {
        var alphaForButton: Int
        var alphaForDesc: Int
        var expandingPercentage: Float
        var collapsingPercentage: Float

        this.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener {
                appBarLayout: AppBarLayout, verticalOffset: Int ->

            currentOffSet = abs(verticalOffset)
            expandingPercentage = (currentOffSet.toFloat() / appBarLayout.totalScrollRange.toFloat())
            collapsingPercentage = 1f - expandingPercentage
            alphaForButton = (255 * expandingPercentage).toInt()
            alphaForDesc = (255 * collapsingPercentage).toInt()

            setViewAlpha(alphaForButton, alphaForDesc)

            when (abs(verticalOffset)) {
                appBarLayout.totalScrollRange -> setLayoutCollapsed()

                0 -> setLayoutExpanded()

                else -> setLayoutMoving(alphaForButton)
            }
        })
    }

    private fun setViewAlpha(alphaForButton: Int, alphaForDesc: Int) {
        this.btn_follow_bottom_others_profile.visibility = if (alphaForButton == 0) View.GONE else View.VISIBLE
        this.btn_follow_bottom_others_profile.background.alpha = alphaForButton

        this.constraint_holder_description.background.alpha = alphaForDesc
        this.tv_others_profile_coverLetter.setTextColor(this.tv_others_profile_coverLetter.textColors.withAlpha(alphaForDesc))
    }

    private fun setLayoutCollapsed() {
        this.collapsing_layout_others_profile.title = ""
        this.others_profile_container_topPortion.visibility = View.INVISIBLE
        this.iv_others_profile_arrow_up.visibility = View.GONE
        this.tv_others_profile_title.visibility = View.VISIBLE
        isAppBarExpanded = false
    }

    private fun setLayoutExpanded() {
        this.collapsing_layout_others_profile.title = ""
        this.others_profile_container_topPortion.visibility = View.VISIBLE
        isAppBarExpanded = true
        btn_follow_bottom_others_profile.setTextColor(btn_follow_bottom_others_profile.textColors.withAlpha(0))
    }

    private fun setLayoutMoving(alpha: Int) {
        this.collapsing_layout_others_profile.title = ""
        this.others_profile_container_topPortion.visibility = View.VISIBLE
        this.iv_others_profile_arrow_up.visibility = View.VISIBLE
        this.tv_others_profile_title.visibility = View.GONE
        halfOffsetAppBar = appBarLayout.totalScrollRange / 2
        isAppBarExpanded = false
        btn_follow_bottom_others_profile.setTextColor(btn_follow_bottom_others_profile.textColors.withAlpha(alpha))
    }

    private fun setCoordinateLayoutSwipeListener() {
        var swipeUp = false
        var swipeDown = false

        this.others_profile_coordinator_layout.setOnSwipeOutListener(
            this,
            object : OnSwipeOutListener { // Coordinate Layout Swipe Listener
                override fun onSwipeLeft(x: Float, y: Float) {}
                override fun onSwipeRight(x: Float, y: Float) {}

                override fun onSwipeDown(x: Float, y: Float) {
                    swipeUp = false
                    swipeDown = true
                }

                override fun onSwipeUp(x: Float, y: Float) {
                    swipeUp = true
                    swipeDown = false
                }

                override fun onSwipeDone() { // when scroll movement stops
                    // if (swipeUp) {
                    //     appBarLayout.setExpanded(false, true)
                    // }
                    //
                    // if (swipeDown && isRecyclerTop) {
                    //     appBarLayout.setExpanded(true, true)
                    //     enableAppBarDraggable(true)
                    // }
                    //
                    // swipeUp = false
                    // swipeDown = false
                }
            })
    }

    private fun enableAppBarDraggable(draggable: Boolean) {
        behavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() { // block dragging behavior on appBarLayout
            override fun canDrag(p0: AppBarLayout): Boolean { return draggable }
        })
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun getIntentData(savedInstanceState: Bundle?) {
        userId = intent.getStringExtra(EXTRA_PROFILE_USER_ID)
        userName = intent.getStringExtra(EXTRA_PROFILE_USER_NAME)
        userRanking = intent.getIntExtra(EXTRA_PROFILE_USER_RANKING, 0)
        userPhotoUrl = intent.getStringExtra(EXTRA_PROFILE_USER_PHOTO_URL)
        calledFrom = intent.getIntExtra(EXTRA_PLACE_CALLED_USER_PROFILE, 0)
    }

    private fun setFontType() {
        FONT_TYPE_REGULAR.let {

        }

        FONT_TYPE_BOLD.let {
            setFontTypeface(this.tv_profile_name, it)
            setFontTypeface(this.tv_others_profile_title, it)
        }

        FONT_TYPE_MEDIUM.let {

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

    @MockData
    private fun getDescription(desc: String, userID: String): String = when (desc) {
        "" -> {
            when (userID) {
                "149618477@N06" -> {
                    "여행 사진 그리고 중독 \uD83C\uDF34 팡포토<br>황도현 \uD83C\uDF3F COMMERICAL PHOTOGRAPHER"
                }

                else -> {
                    "Contact: mail@ladyironchef.com<br>Facebook.com/ladyironchef<br>www.ladyironchef.com"
                }
            }
        }

        else -> {
            when (userID) {
                "61533954@N00" -> {
                    "여행작가 심상우입니다<br>여행작가로 먹고 산지 7년째, 5권의 책 출간.<br>에세이 #당신의여행은행복한가요<br>" +
                        "가이드북 #하노이100배즐기기<br>-<br>⬇️ 서점에서 만나요 \uD83D\uDC9B<br>me2.do/FVfKro04"
                }

                else -> {
                    desc
                }
            }
        }
    }

    private fun setRankColor(rank: Int) {
        when (rank) {
            PEOPLE_RANK_BEGINNER -> constraint_profile.setBackgroundResource(R.drawable.border_rounded_rank_yellow)
            PEOPLE_RANK_1 -> constraint_profile.setBackgroundResource(R.drawable.border_rounded_rank_blue)
            PEOPLE_RANK_2 -> constraint_profile.setBackgroundResource(R.drawable.border_rounded_rank_orange)
            PEOPLE_RANK_3 -> constraint_profile.setBackgroundResource(R.drawable.border_rounded_rank_purple)
            PEOPLE_RANK_4 -> constraint_profile.setBackgroundResource(R.drawable.border_rounded_rank_red)
            PEOPLE_RANK_EXPERT -> constraint_profile.setBackgroundResource(R.drawable.border_rounded_rank_gradient)
        }
    }

    internal fun showNetworkError(resource: Resource) {
        when (resource.errorCode) {
            in 400..499 -> {
                Snackbar.make(this.btn_follow_bottom_others_profile, getString(R.string.phrase_client_wrong_request), Snackbar.LENGTH_LONG).show()
            }

            in 500..599 -> {
                Snackbar.make(this.btn_follow_bottom_others_profile, getString(R.string.phrase_server_wrong_response), Snackbar.LENGTH_LONG).show()
            }

            else -> {
                Snackbar.make(this.btn_follow_bottom_others_profile, resource.getMessage().toString(), Snackbar.LENGTH_LONG).show()
            }
        }

        this.progress_bar_others_profile_holder.visibility = View.GONE
    }

    private fun networkStatusVisible(isVisible: Boolean) = if (isVisible) {
        this.disconnect_container_pinned.visibility = View.GONE
        this.appbar_others_profile.visibility = View.VISIBLE
        this.viewpager_others_profile.visibility = View.VISIBLE
        this.btn_follow_bottom_others_profile.visibility = View.VISIBLE
    } else {
        this.disconnect_container_pinned.visibility = View.VISIBLE
        this.appbar_others_profile.visibility = View.GONE
        this.viewpager_others_profile.visibility = View.GONE
        this.progress_bar_others_profile_holder.visibility = View.GONE
        this.btn_follow_bottom_others_profile.visibility = View.GONE
    }

    private fun showProgressBarLoading() {
        this.progress_bar_others_profile_holder.visibility = View.VISIBLE
        this.appbar_others_profile.visibility = View.GONE
        this.viewpager_others_profile.visibility = View.GONE
        this.btn_follow_bottom_others_profile.visibility = View.GONE
    }

    private fun showLoadingFinished() {
        this.progress_bar_others_profile_holder.visibility = View.GONE
        this.appbar_others_profile.visibility = View.VISIBLE
        this.viewpager_others_profile.visibility = View.VISIBLE
        this.btn_follow_bottom_others_profile.visibility = View.VISIBLE
    }

    private fun removeCache() {
        viewModel.removeCache(userId)
    }
}
