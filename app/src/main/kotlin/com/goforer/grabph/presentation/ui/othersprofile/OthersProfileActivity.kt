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
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.AppCompatButton
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.goforer.base.annotation.MockData
import com.goforer.base.annotation.RunWithMockData
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.base.presentation.view.customs.layout.CustomStaggeredGridLayoutManager
import com.goforer.base.presentation.view.customs.listener.OnSwipeOutListener
import com.goforer.base.presentation.view.decoration.GapItemDecoration
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PROFILE_USER_ID
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PROFILE_USER_NAME
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PROFILE_USER_PHOTO_URL
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PROFILE_USER_RANKING
import com.goforer.grabph.presentation.ui.othersprofile.adapter.OthersProfileAdapter
import com.goforer.grabph.presentation.vm.profile.OthersProfileViewModel
import com.goforer.grabph.repository.model.cache.data.mock.datasource.profile.MyPhotoDataSource
import com.goforer.grabph.repository.model.cache.data.mock.datasource.profile.ProfileDataSource
import com.goforer.grabph.repository.model.cache.data.entity.profile.HomeProfile
import com.goforer.grabph.repository.model.cache.data.entity.profile.MyPhoto
import com.goforer.grabph.repository.model.cache.data.entity.profile.Owner
import com.goforer.grabph.repository.network.resource.NetworkBoundResource
import com.goforer.grabph.repository.network.response.Resource
import com.goforer.grabph.repository.network.response.Status
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_others_profile.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.full.findAnnotation

@SuppressLint("Registered")
@RunWithMockData(true)
class OthersProfileActivity: BaseActivity() {
    private val mock = this::class.findAnnotation<RunWithMockData>()?.mock!!

    private var adapter: OthersProfileAdapter? = null

    private var userId: String = ""
    private var userName: String = ""
    private var userPhotoUrl: String = ""
    private var userBackgroundPhoto: String = ""
    private var userRanking: Int = 0
    private var calledFrom: Int = 0
    private var halfOffsetAppBar: Int = 0
    private var currentOffSet: Int = 0
    private var isAppBarExpanded = true
    private var isRecyclerTop = true

    private lateinit var params: CoordinatorLayout.LayoutParams
    private lateinit var behavior: AppBarLayout.Behavior
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var gridLayoutManager: CustomStaggeredGridLayoutManager

    @field:Inject
    internal lateinit var viewModel: OthersProfileViewModel

    override fun setContentView() { setContentView(R.layout.activity_others_profile) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isNetworkAvailable) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.TRANSPARENT
            networkStatusVisible(true)
        } else {
            networkStatusVisible(false)
        }
    }

    override fun setViews(savedInstanceState: Bundle?) {
        super.setViews(savedInstanceState)
        getIntentData(savedInstanceState)
        createAdapter()
        observeUserProfileLiveData()

        if (savedInstanceState == null) getProfile() else viewModel.loadUserProfileFromCache()
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
    }

    private fun observeUserProfileLiveData() {
        viewModel.getUserProfileLiveData().observe(this, Observer {
            setTopPortionViewData(it)

            val config = PagedList.Config.Builder()
                    .setInitialLoadSizeHint(10)
                    .setPageSize(20)
                    .setPrefetchDistance(10)
                    .build()

            LivePagedListBuilder(object: DataSource.Factory<Int, MyPhoto>() {
                override fun create(): DataSource<Int, MyPhoto> {
                    return MyPhotoDataSource(it.photos.photos)
                }
            }, config).build().observe(this, Observer { pagedList ->
                adapter?.submitList(pagedList)
            })
        })
    }

    private fun getProfile() {
        when (mock) {
            @MockData
            true -> transactMockData()
            false -> transactRealData()
        }
    }

    @MockData
    private fun transactMockData() {
        setBackgroundImageForMock()
        var profile: HomeProfile.MyPagePhotos? = null
        val homeProfile = ProfileDataSource()
        homeProfile.setHomeProfile()
        homeProfile.getHomeProfile()?.let { profile = it.sellPhotos }

        val userProfile = Owner(userId, "", 0, "", userName, "",
                homeProfile.getHomeProfile()?.coverletter, Owner.Photourl(userPhotoUrl),
                userRanking.toString(), "", "", "124", "16",
                "23", "46", userBackgroundPhoto, profile!!)

        viewModel.setUserProfile(userProfile)
        viewModel.setUserProfileLiveData(userProfile)
    }

    private fun transactRealData() {
        val liveData = viewModel.userProfile

        setUserProfileLoadParam(NetworkBoundResource.LOAD_OTHERS_PROFILE,
                NetworkBoundResource.BOUND_FROM_LOCAL, Caller.CALLED_FROM_OTHERS_PROFILE, "")

        liveData.observe(this, Observer { resource ->
            when (resource?.getStatus()) {
                Status.SUCCESS -> {
                    resource.getData()?.let { othersProfile ->
                        val profile = othersProfile as Owner
                        viewModel.setUserProfile(profile)
                        viewModel.setUserProfileLiveData(profile)
                    }

                    resource.getMessage()?.let {
                        showNetworkError(resource)
                        liveData.removeObservers(this)
                    }
                }

                Status.LOADING -> { /* Loading Screen to be implemented */ }

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
    @MockData
    private fun setTopPortionViewData(profile: Owner) {
        profile.grade?.toInt()?.let { userRanking = it }
        setRankColor(userRanking)

        profile.realname?.let { userName = it }
        this.tv_others_profile_name.text = userName

        profile.photourl?._content?.let { userPhotoUrl = it }
        setImageDraw(iv_others_profile_icon, userPhotoUrl)

        profile.backgroundPhoto?.let { userBackgroundPhoto = it }
        setFixedImageSize(0, 0)
        setImageDraw(this.iv_others_profile_title_photo, this.backdrop_container, userBackgroundPhoto, false)
        this.iv_others_profile_title_photo.scaleType = ImageView.ScaleType.CENTER_CROP

        this.tv_others_profile_coverLetter.text = profile.description
        this.tv_others_profile_number_searper.text = profile.followings
        this.tv_others_profile_number_searple.text = profile.followers
        this.tv_others_profile_number_like.text = profile.purchased // should be edited!!
        this.tv_photo_number_others_profile.text = "${profile.galleryCount} Photos"

        this.btn_follow_bottom_others_profile.translationY = this.btn_follow_bottom_others_profile.height.toFloat()

        setButtonsClickListener()
        setAppbarScrollingBehavior()
    }

    private fun setButtonsClickListener() {
        this.btn_follow_others_profile.isSelected = false
        this.btn_follow_others_profile.setOnClickListener {
            (it as AppCompatButton).text = if (it.isSelected) getString(R.string.follow_button) else getString(R.string.following_button)
            this.btn_follow_bottom_others_profile.text = if (it.isSelected) getString(R.string.follow_button) else getString(R.string.follow_button_bottom)
            it.isSelected = !it.isSelected
        }

        this.btn_follow_bottom_others_profile.setOnClickListener { button ->
            (button as AppCompatButton).text = if (this.btn_follow_others_profile.isSelected) getString(R.string.follow_button) else getString(R.string.follow_button_bottom)
            this.btn_follow_others_profile.let { it.text = if (it.isSelected) getString(R.string.follow_button) else getString(R.string.following_button) }
            this.btn_follow_others_profile.isSelected = ! this.btn_follow_others_profile.isSelected
        }

        this.backdrop_container.setOnClickListener { appBarLayout.setExpanded(false, true) }
        this.others_profile_container_searper.setOnClickListener {  }
        this.others_profile_container_searple.setOnClickListener {  }
        this.others_profile_container_like.setOnClickListener {  }
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
        val btnView = this.btn_follow_bottom_others_profile
        var alpha: Int
        var offSetPercentage: Float

        this.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { //appBarLayout Offset Change Listener
            appBarLayout: AppBarLayout, verticalOffset: Int ->

            currentOffSet = abs(verticalOffset)
            offSetPercentage = (currentOffSet.toFloat() / appBarLayout.totalScrollRange.toFloat())
            alpha = (255 * offSetPercentage).toInt()
            btnView.visibility = if (alpha == 0) View.GONE else View.VISIBLE

            when {
                abs(verticalOffset) == appBarLayout.totalScrollRange -> { // when appBarLayout Collapsed
                    this.collapsing_layout_others_profile.title = userName
                    this.others_profile_container_topPortion.visibility = View.INVISIBLE
                    gridLayoutManager.enabledSrcoll = true
                    isAppBarExpanded = false
                    btnView.background.alpha = alpha
//                    btnView.translationY = 0f
                }
                abs(verticalOffset) == 0 -> { // when appBarLayout Expanded
                    this.collapsing_layout_others_profile.title = ""
                    this.others_profile_container_topPortion.visibility = View.VISIBLE
                    enableAppBarDraggable(false)
                    gridLayoutManager.enabledSrcoll = false
                    isAppBarExpanded = true
                    btnView.background.alpha = 0
                    btnView.setTextColor(btnView.textColors.withAlpha(0))
//                    btnView.translationY = btnView.height.toFloat()
                }
                else -> { // when appBarLayout is on progress of collapsing OR expanding
                    this.appBarLayout
                    this.collapsing_layout_others_profile.title = ""
                    this.others_profile_container_topPortion.visibility = View.VISIBLE
                    halfOffsetAppBar = appBarLayout.totalScrollRange / 2
                    isAppBarExpanded = false
                    btnView.translationY = max(0f, min(btnView.height.toFloat(), btnView.translationY + appBarLayout.scrollY))
                    btnView.background.alpha = alpha
                    btnView.setTextColor(btnView.textColors.withAlpha(alpha))
                }
            }
        })
    }

    private fun setCoordinateLayoutSwipeListener() {
        var swipeUp = false
        var swipeDown = false

        this.others_profile_coordinator_layout.setOnSwipeOutListener(this, object : OnSwipeOutListener { //Coordinate Layout Swipe Listener
            override fun onSwipeLeft(x: Float, y: Float) { }
            override fun onSwipeRight(x: Float, y: Float) { }

            override fun onSwipeDown(x: Float, y: Float) {
                swipeUp = false
                swipeDown = true
            }

            override fun onSwipeUp(x: Float, y: Float) {
                swipeUp = true
                swipeDown = false
            }

            override fun onSwipeDone() { //when scroll movement stops
                if (swipeUp) {
                    appBarLayout.setExpanded(false, true)
                }

                if (swipeDown && isRecyclerTop) {
                    appBarLayout.setExpanded(true, true)
                    enableAppBarDraggable(true)
                }

                swipeUp = false
                swipeDown = false
            }
        })
    }

    private fun enableAppBarDraggable(draggable: Boolean) {
        behavior.setDragCallback(object: AppBarLayout.Behavior.DragCallback() { //block dragging behavior on appBarLayout
            override fun canDrag(p0: AppBarLayout): Boolean { return draggable }
        })
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun getIntentData(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            userId = intent.getStringExtra(EXTRA_PROFILE_USER_ID)
            userName = intent.getStringExtra(EXTRA_PROFILE_USER_NAME)
            userRanking = intent.getIntExtra(EXTRA_PROFILE_USER_RANKING, 0)
            userPhotoUrl = intent.getStringExtra(EXTRA_PROFILE_USER_PHOTO_URL)
            calledFrom = intent.getIntExtra(Caller.EXTRA_PLACE_CALLED_USER_PROFILE, 0)
        }
    }

    private fun createAdapter() {
        adapter = adapter ?: OthersProfileAdapter(this)
        this.recycler_others_profile.setHasFixedSize(true)
        this.recycler_others_profile.setItemViewCacheSize(20)
        this.recycler_others_profile.isVerticalScrollBarEnabled = false
        gridLayoutManager = CustomStaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        gridLayoutManager.enabledSrcoll = false //disable recyclerView's scroll
        gridLayoutManager.isItemPrefetchEnabled = true
        this.recycler_others_profile.layoutManager = gridLayoutManager
        this.recycler_others_profile.addItemDecoration(createItemDecoration())
        this.recycler_others_profile.adapter = adapter

        val btnView = this.btn_follow_bottom_others_profile
        var alpha: Int
        var percentage: Float

        this.recycler_others_profile.addOnScrollListener( object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                percentage = (1f - (btnView.translationY / btnView.height.toFloat()))
                alpha = (255 * percentage).toInt()
                btnView.translationY = max(0f, min(btnView.height.toFloat(), btnView.translationY + dy))
                btnView.background.alpha = alpha
                btnView.setTextColor(btnView.textColors.withAlpha(alpha))
            }
            /** newState indicates: 0 = idle // 1 = dragging // 2 = settling  */
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(-1)) { // if scroll reaches the top position
                    isRecyclerTop = true

                    if (newState == 0) { // if scroll movement stops
                        if (currentOffSet < appBarLayout.totalScrollRange && currentOffSet >= halfOffsetAppBar) {
                            appBarLayout.setExpanded(false, true)
                        } else if (currentOffSet in 1 until halfOffsetAppBar) {
                            appBarLayout.setExpanded(true, true)
                        }
                    }
                } else {
                    isRecyclerTop = false
                }
            }
        })
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

    private fun setRankColor(rank: Int) {
        when (rank) {
            PEOPLE_RANK_BEGINNER -> others_profile_profile_holder.setBackgroundResource(R.drawable.border_rounded_rank_yellow)
            PEOPLE_RANK_FIRST -> others_profile_profile_holder.setBackgroundResource(R.drawable.border_rounded_rank_blue)
            PEOPLE_RANK_SECOND -> others_profile_profile_holder.setBackgroundResource(R.drawable.border_rounded_rank_orange)
            PEOPLE_RANK_THIRD -> others_profile_profile_holder.setBackgroundResource(R.drawable.border_rounded_rank_purple)
            PEOPLE_RANK_FOURTH -> others_profile_profile_holder.setBackgroundResource(R.drawable.border_rounded_rank_red)
        }
    }

    private fun setUserProfileLoadParam(loadType: Int, boundType: Int, calledFrom: Int, id: String) {
        viewModel.loadType = loadType
        viewModel.boundType = boundType
        viewModel.calledFrom = calledFrom
        viewModel.setId(id)
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

    private fun showNetworkError(resource: Resource) {
        when(resource.errorCode) {
            in 400..499 -> {
                Snackbar.make(this.others_profile_coordinator_layout, getString(R.string.phrase_client_wrong_request), Snackbar.LENGTH_LONG).show()
            }

            in 500..599 -> {
                Snackbar.make(this.others_profile_coordinator_layout, getString(R.string.phrase_server_wrong_response), Snackbar.LENGTH_LONG).show()
            }

            else -> {
                Snackbar.make(this.others_profile_coordinator_layout, resource.getMessage().toString(), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun networkStatusVisible(isVisible: Boolean) = if (isVisible) {
        this.disconnect_container_pinned_profile.visibility = View.GONE
        this.appbar_others_profile.visibility = View.VISIBLE
        this.recycler_others_profile.visibility = View.VISIBLE
    } else {
        this.disconnect_container_pinned_profile.visibility = View.VISIBLE
        this.appbar_others_profile.visibility = View.GONE
        this.recycler_others_profile.visibility = View.GONE
    }
}
