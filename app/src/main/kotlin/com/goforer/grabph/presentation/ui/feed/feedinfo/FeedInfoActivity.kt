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

package com.goforer.grabph.presentation.ui.feed.feedinfo

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.transition.Transition
import android.view.*
import android.view.animation.AnimationUtils
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.goforer.base.annotation.MockData
import com.goforer.base.domain.common.GeneralFunctions
import com.goforer.base.presentation.utils.CommonUtils.convertDateToLong
import com.goforer.base.presentation.utils.CommonUtils.withDelay
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.base.presentation.view.customs.listener.OnSwipeOutListener
import com.goforer.base.presentation.view.decoration.GapItemDecoration
import com.goforer.grabph.R
import com.goforer.grabph.domain.usecase.save.PhotoSaver
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_FEED
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_FEED_INFO
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_HOME_BEST_PICK_HOT_PHOTO
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_HOME_BEST_PICK_SEARPER_PHOTO
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_PINNED_FEED
import com.goforer.grabph.presentation.caller.Caller.EXTRA_FEED_IDX
import com.goforer.grabph.presentation.caller.Caller.EXTRA_FEED_INFO_CALLED_FROM
import com.goforer.grabph.presentation.caller.Caller.EXTRA_FEED_INFO_POSITION
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PHOTO_ID
import com.goforer.grabph.presentation.caller.Caller.EXTRA_SEARPER_ID
import com.goforer.grabph.presentation.caller.Caller.SELECTED_BEST_PICK_HOT_PHOTO_POSITION
import com.goforer.grabph.presentation.caller.Caller.SELECTED_FEED_INFO_PHOTO_VIEW
import com.goforer.grabph.presentation.caller.Caller.SELECTED_FEED_ITEM_POSITION
import com.goforer.grabph.presentation.common.effect.transition.TransitionCallback
import com.goforer.grabph.presentation.common.effect.transition.TransitionObject
import com.goforer.grabph.presentation.common.menu.MenuHandler
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import com.goforer.grabph.presentation.common.utils.handler.exif.EXIFHandler
import com.goforer.grabph.presentation.common.utils.handler.watermark.WatermarkHandler
import com.goforer.grabph.presentation.common.view.SlidingDrawer
import com.goforer.grabph.presentation.ui.feed.common.SavePhoto
import com.goforer.grabph.presentation.ui.feed.feedinfo.adapter.RecommendedFeedAdapter
import com.goforer.grabph.presentation.ui.feed.feedinfo.sharedelementcallback.FeedInfoItemCallback
import com.goforer.grabph.presentation.vm.feed.FeedContentViewModel
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.presentation.vm.home.HomeViewModel
import com.goforer.grabph.presentation.vm.feed.FeedViewModel
import com.goforer.grabph.presentation.ui.photoviewer.sharedelementcallback.PhotoViewerCallback
import com.goforer.grabph.presentation.vm.BaseViewModel.Companion.NONE_TYPE
import com.goforer.grabph.presentation.vm.comment.CommentViewModel
import com.goforer.grabph.presentation.vm.feed.exif.EXIFViewModel
import com.goforer.grabph.presentation.vm.feed.exif.LocalEXIFViewModel
import com.goforer.grabph.presentation.vm.feed.location.LocalLocationViewModel
import com.goforer.grabph.presentation.vm.feed.location.LocationViewModel
import com.goforer.grabph.presentation.vm.people.person.PersonViewModel
import com.goforer.grabph.presentation.vm.feed.photo.LocalSavedPhotoViewModel
import com.goforer.grabph.data.datasource.model.cache.data.mock.datasource.feeds.FeedsContentDataSource
import com.goforer.grabph.data.datasource.model.cache.data.entity.exif.EXIF
import com.goforer.grabph.data.datasource.model.cache.data.entity.feed.FeedItem
import com.goforer.grabph.data.datasource.model.cache.data.entity.location.Location
import com.goforer.grabph.data.datasource.model.cache.data.mock.entity.feed.FeedsContent
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.Person
import com.goforer.grabph.data.datasource.network.response.Status
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_COMMENTS
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_EXIF
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_GEO
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_PERSON
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.data.repository.remote.Repository.Companion.BOUND_FROM_BACKEND
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import kotlinx.android.synthetic.main.activity_feed_info.*
import kotlinx.android.synthetic.main.activity_feed_info.appbar_layout
import kotlinx.android.synthetic.main.activity_feed_info.backdrop_container
import kotlinx.android.synthetic.main.activity_feed_info.collapsing_layout
import kotlinx.android.synthetic.main.activity_feed_info.scroll_view
import kotlinx.android.synthetic.main.activity_feed_info.toolbar
import kotlinx.android.synthetic.main.cell_photo_info.*
import kotlinx.android.synthetic.main.cell_portion_photo_info.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.text.ParseException
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class FeedInfoActivity: BaseActivity(),  GoogleMap.OnMarkerDragListener {
    private lateinit var sharedElementCallback: FeedInfoItemCallback

    private var feedItem: FeedItem? = null

    internal lateinit var photoId: String

    private lateinit var searperId: String

    private lateinit var marker: Marker

    private lateinit var playBackStateListener: PlayBackStateListener

    private var feedTitle: String? = null
    private var description: String? = null
    private var searperPhotoUrl: String? = null
    private var videoUrl: String? = null
    private var mediaType: String? = null

    private var player: SimpleExoPlayer? = null
    private var currentWindow = 0
    private var playbackPosition: Long = 0

    private var feedIdx: Long = 0

    private var feedPosition = 0
    private var feedInfoCalledFrom = 0
    private var offsetChange = 0

    private var searper: Person? = null

    private var isAppBarLayoutExpanded = false
    private var isAppBarLayoutCollapsed = false
    private var isUpdated = false
    private var isUnfolded = false

    private var exifList: List<EXIF>? = null

    private var location: Location? = null

    private lateinit var slidingDrawer: SlidingDrawer

    private val job = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + job)
    private val ioScope = CoroutineScope(Dispatchers.IO + job)

    @field:Inject
    lateinit var feedViewModel: FeedViewModel
    @field:Inject
    lateinit var userProfileViewModel: PersonViewModel
    @field:Inject
    lateinit var localEXIFViewModel: LocalEXIFViewModel
    @field:Inject
    lateinit var localLocationViewModel: LocalLocationViewModel
    @field:Inject
    lateinit var localSavedPhotoViewModel: LocalSavedPhotoViewModel
    @field:Inject
    lateinit var feedContentViewModel: FeedContentViewModel
    @field:Inject
    lateinit var exifViewModel: EXIFViewModel
    @field:Inject
    lateinit var locationViewModel: LocationViewModel
    @field:Inject
    lateinit var commentViewModel: CommentViewModel
    @field:Inject
    lateinit var homeViewModel: HomeViewModel
    @field:Inject
    lateinit var workHandler: CommonWorkHandler

    @field:Inject
    lateinit var watermarkHandler: WatermarkHandler

    @field:Inject
    lateinit var saver: PhotoSaver

    @field:Inject
    lateinit var exifHandler: EXIFHandler

    private val sharedEnterListener = object : TransitionCallback() {
        override fun onTransitionEnd(transition: Transition) {
            removeCallback()
        }

        override fun onTransitionCancel(transition: Transition) {
            removeCallback()
        }

        private fun removeCallback() {
            window.sharedElementEnterTransition.removeListener(this)
            setEnterSharedElementCallback(null as SharedElementCallback?)
        }
    }

    private val sharedExitListener = object : TransitionCallback() {
        override fun onTransitionEnd(transition: Transition) {
            removeCallback()
        }

        override fun onTransitionCancel(transition: Transition) {
            removeCallback()
        }

        private fun removeCallback() {
            window.sharedElementExitTransition.removeListener(this)
            setExitSharedElementCallback(null as SharedElementCallback?)
        }
    }

    companion object {
        private const val CHECK_EXIF_LIST_SIZE = 26
        private const val CHECK_EXIF_NONE_ITEM_COUNT = 4
        private const val DELAY_COLLAPSED_TIMER_INTERVAL = 300

        private const val CACHE_USER_PROFILE_TYPE = 0
        private const val CACHE_PHOTO_EXIF_TYPE = 1
        private const val CACHE_PHOTO_LOCATION_TYPE = 2
        private const val CACHE_PHOTO_COMMENTS_TYPE = 3

        private const val MAP_ZOOM = 14f

        private const val MOCK_SIZE_DATA_DELAY_DURATION = 600L
        private const val TOOL_TIP_DEFAULT_DURATION = 1500L
        private const val TOOL_TIP_MULTI_LINE_DURATION = 5000L

        private const val TOO_TIP_MULTI_LINE_WIDTH = 1100

        private const val PURCHASE_BTN_ANIMATION_TIMEOUT = 1200L
        private const val PURCHASE_BTN_ANIMATION_DURATION = 300L
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isNetworkAvailable) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = Color.TRANSPARENT
            offsetChange = 0
            networkStatusVisible(true)
            savedInstanceState ?: getIntentData()
            savedInstanceState?.let {
                getIntentData()
            }

            val krBoldTypeface = Typeface.createFromAsset(applicationContext?.assets, NOTO_SANS_KR_BOLD)
            this@FeedInfoActivity.collapsing_layout.setCollapsedTitleTypeface(krBoldTypeface)
            this@FeedInfoActivity.collapsing_layout.setExpandedTitleTypeface(krBoldTypeface)
            playBackStateListener = PlayBackStateListener()

            getData()

            @MockData
            transactFeedsMockData()

            this@FeedInfoActivity.recycler_feed_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val btnView = this@FeedInfoActivity.btn_purchase

                    btnView.translationY = max(0f, min(btnView.height.toFloat(), btnView.translationY + dy))
                }
            })

            this@FeedInfoActivity.appbar_layout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener {
                    appBarLayout, verticalOffset ->
                this@FeedInfoActivity.collapsing_layout.title = feedTitle
                val btnView = this@FeedInfoActivity.btn_purchase

                when {
                    abs(verticalOffset) == appBarLayout.totalScrollRange -> {
                        btnView.translationY = abs(verticalOffset).toFloat()
                        isAppBarLayoutCollapsed = true
                        isAppBarLayoutExpanded = false
                    }

                    verticalOffset == 0 -> {
                        btnView.translationY = 0L.toFloat()
                        isAppBarLayoutExpanded = true
                        launchUIWork {
                            delay(DELAY_COLLAPSED_TIMER_INTERVAL.toLong())
                            isAppBarLayoutCollapsed = false
                        }
                    }

                    else -> {
                        btnView.translationY = max(0f, min(btnView.height.toFloat(), btnView.translationY + appBarLayout.scrollY))
                        isAppBarLayoutExpanded = false
                        isAppBarLayoutCollapsed = true
                        if (!this@FeedInfoActivity.folding_photo_info_cell.isUnfolded && isUnfolded) {
                            launchUIWork {
                                this@FeedInfoActivity.folding_photo_info_cell.unfold(true)
                            }
                        }
                    }
                }
            })

            this@FeedInfoActivity.coordinator_feed_info_layout.setOnSwipeOutListener(this, object : OnSwipeOutListener {
                override fun onSwipeLeft(x: Float, y: Float) {
                    Timber.d("onSwipeLeft")

                    finishAction()
                }

                override fun onSwipeRight(x: Float, y: Float) {
                    Timber.d("onSwipeRight")

                }

                override fun onSwipeDown(x: Float, y: Float) {
                    Timber.d( "onSwipeDown")

                    if (!isAppBarLayoutCollapsed && isAppBarLayoutExpanded) {
                        finishAction()
                    }
                }

                override fun onSwipeUp(x: Float, y: Float) {
                    Timber.d("onSwipeUp")
                }

                override fun onSwipeDone() {
                    Timber.d("onSwipeDone")
                }
            })

            launchUIWork {
                delay(PURCHASE_BTN_ANIMATION_TIMEOUT)
                purchaseButtonAnimation()
            }
        } else {
            networkStatusVisible(false)
        }
    }

    override fun setContentView() {
        setContentView(R.layout.activity_feed_info)
    }

    override fun setActionBar() {
        setSupportActionBar(this@FeedInfoActivity.toolbar)
        val actionBar= supportActionBar
        actionBar?.let {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_USE_LOGO
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
        }

        this@FeedInfoActivity.toolbar?.setNavigationOnClickListener{
            finishAfterTransition()
        }

        this@FeedInfoActivity.toolbar.hideOverflowMenu()
    }

    override fun setViews(savedInstanceState: Bundle?) {
        savedInstanceState ?: getIntentData()

        slidingDrawer = SlidingDrawer.SlidingDrawerBuilder()
            .setActivity(this)
            .setRootView(R.id.coordinator_layout)
            .setBundle(savedInstanceState)
            .setType(SlidingDrawer.DRAWER_SEARPER_PROFILE_TYPE)
            .setWorkHandler(workHandler)
            .build()

        // The cache should be removed whenever App is started again and then
        // the data are fetched from the Back-end.
        // The Cache has to be light-weight.
        removePhotoCache(CACHE_USER_PROFILE_TYPE)
        userProfileViewModel.loadType = LOAD_PERSON
        userProfileViewModel.boundType = BOUND_FROM_BACKEND


        // The cache should be removed whenever App is started again and then
        // the data are fetched from the Back-end.
        // The Cache has to be light-weight.
        removePhotoCache(CACHE_PHOTO_LOCATION_TYPE)
        locationViewModel.loadType = LOAD_GEO
        locationViewModel.boundType = BOUND_FROM_BACKEND

        // The cache should be removed whenever App is started again and then
        // the data are fetched from the Back-end.
        // The Cache has to be light-weight.
        removePhotoCache(CACHE_PHOTO_EXIF_TYPE)
        exifViewModel.loadType = LOAD_EXIF
        exifViewModel.boundType = BOUND_FROM_BACKEND

        // The cache should be removed whenever App is started again and then
        // the data are fetched from the Back-end.
        // The Cache has to be light-weight.
        removePhotoCache(CACHE_PHOTO_COMMENTS_TYPE)
        commentViewModel.loadType = LOAD_COMMENTS
        commentViewModel.boundType = BOUND_FROM_BACKEND

        this@FeedInfoActivity.recycler_feed_view.setHasFixedSize(true)
        this@FeedInfoActivity.recycler_feed_view.setItemViewCacheSize(20)
        this@FeedInfoActivity.recycler_feed_view.isVerticalScrollBarEnabled = false
        val gridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        this@FeedInfoActivity.recycler_feed_view.layoutManager = gridLayoutManager
        gridLayoutManager.isItemPrefetchEnabled = true
        this@FeedInfoActivity.recycler_feed_view?.addItemDecoration(createItemDecoration())

        when(feedInfoCalledFrom) {
            CALLED_FROM_FEED, CALLED_FROM_PINNED_FEED, CALLED_FROM_HOME_BEST_PICK_HOT_PHOTO, CALLED_FROM_HOME_BEST_PICK_SEARPER_PHOTO -> {
                window.sharedElementEnterTransition.addListener(sharedEnterListener)
                supportPostponeEnterTransition()
            }
        }
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)

        if (resultCode == SELECTED_FEED_INFO_PHOTO_VIEW) {
            // Listener to reset shared element exit transition callbacks.
            window.sharedElementExitTransition.addListener(sharedExitListener)
            supportPostponeEnterTransition()

            val callback = PhotoViewerCallback()

            callback.setViewBinding(this@FeedInfoActivity.iv_feed_info_photo)
            setEnterSharedElementCallback(callback)
            supportStartPostponedEnterTransition()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_normal_item, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onPreparePanel(featureId: Int, view: View?, menu: Menu): Boolean {
        if (menu.javaClass.simpleName == "MenuBuilder") {
            try {
                @SuppressLint("PrivateApi")
                val method= menu.javaClass.getDeclaredMethod("setOptionalIconsVisible", java.lang.Boolean.TYPE)
                method.isAccessible = true
                method.invoke(menu, true)
            } catch (e: NoSuchMethodException) {
                System.err.println(e.message)
                e.printStackTrace()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

        }

        return super.onPreparePanel(featureId, view, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_photo_share -> {
                val wrapper = ContextThemeWrapper(this, R.style.PopupMenu)
                val sharePopup = PopupMenu(wrapper, findViewById(R.id.action_photo_share), Gravity.CENTER)

                sharePopup.menuInflater.inflate(R.menu.menu_share_popup, sharePopup.menu)
                MenuHandler().applyFontToMenuItem(sharePopup, Typeface.createFromAsset(applicationContext?.assets, NOTO_SANS_KR_MEDIUM),
                    resources.getColor(R.color.colorHomeQuestFavoriteKeyword, theme))
                sharePopup.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_share_facebook ->
                            callShareToFacebook(this@FeedInfoActivity.iv_feed_info_photo.drawable as BitmapDrawable,
                                this.applicationContext.getString(R.string.phrase_title) + "\n\n"
                                    + this@FeedInfoActivity.collapsing_layout.title + "\n\n"
                                    + this.applicationContext.getString(R.string.phrase_description) + "\n\n"
                                    + description)

                        R.id.menu_share_ect -> {
                            watermarkHandler.putWatermark(this.applicationContext, workHandler,
                                (this@FeedInfoActivity.iv_feed_info_photo.drawable as BitmapDrawable).bitmap, feedTitle, description)
                        }

                        else -> {
                        }
                    }

                    true
                }

                sharePopup.show()

                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var outStateBundle = outState

        outStateBundle.putLong(EXTRA_FEED_IDX, feedIdx)
        outStateBundle.putString(EXTRA_PHOTO_ID, photoId)
        outStateBundle.putString(EXTRA_SEARPER_ID, searperId)
        outStateBundle.putInt(EXTRA_FEED_INFO_POSITION, feedPosition)
        outStateBundle.putInt(EXTRA_FEED_INFO_CALLED_FROM, feedInfoCalledFrom)

        slidingDrawer.searperProfileDrawerForFeedViewDrawer?.let {
            outStateBundle = slidingDrawer.searperProfileDrawerForFeedViewDrawer?.saveInstanceState(outStateBundle)!!
            outStateBundle = slidingDrawer.drawerHeader?.saveInstanceState(outStateBundle)!!
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        photoId = savedInstanceState.getString(EXTRA_PHOTO_ID, "")
        searperId = savedInstanceState.getString(EXTRA_SEARPER_ID, "")
        feedPosition = savedInstanceState.getInt(EXTRA_FEED_INFO_POSITION, 0)
        feedInfoCalledFrom = savedInstanceState.getInt(EXTRA_FEED_INFO_CALLED_FROM, -1)
    }

    override fun onBackPressed() {
        finishAfterTransition()

        super.onBackPressed()
    }

    override fun finishAfterTransition() {
        this@FeedInfoActivity.collapsing_layout.title = ""
        supportPostponeEnterTransition()
        setViewBind()
        setActivityResult(feedInfoCalledFrom)
        this.video_view_feed_info.visibility = View.GONE // for clear transition
        this.iv_feed_info_photo.visibility = View.VISIBLE

        super.finishAfterTransition()
    }

    @ExperimentalCoroutinesApi
    override fun onDestroy() {
        super.onDestroy()

        if (isUpdated) {
            feedViewModel.updateFeedItem(feedItem!!)
        }

        uiScope.cancel()
        ioScope.cancel()
        job.cancel()
    }

    override fun onMarkerDragStart(marker: Marker) { }

    override fun onMarkerDrag(marker: Marker) { }

    override fun onMarkerDragEnd(marker: Marker) { }

    override fun onPause() {
        super.onPause()
        if (mediaType == "video" && Build.VERSION.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (mediaType == "video" && Build.VERSION.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mediaType == "video" && Build.VERSION.SDK_INT < 24) {
            videoUrl?.let { initializePlayer(it) }
        }
    }

    override fun onStart() {
        super.onStart()
        if (mediaType == "video" && Build.VERSION.SDK_INT >= 24) {
            videoUrl?.let { initializePlayer(it) }
        }
    }

    private fun networkStatusVisible(isVisible: Boolean) = if (isVisible) {
        this@FeedInfoActivity.disconnect_container_pinned_feed_info.visibility = View.GONE
        this@FeedInfoActivity.iv_disconnect_pinned_feed_info.visibility = View.GONE
        this@FeedInfoActivity.tv_notice1_pinned_feed_info.visibility = View.GONE
        this@FeedInfoActivity.tv_notice2_pinned_feed_info.visibility = View.GONE
        this@FeedInfoActivity.appbar_layout.visibility = View.VISIBLE
        this@FeedInfoActivity.scroll_view.visibility = View.VISIBLE
    } else {
        this@FeedInfoActivity.disconnect_container_pinned_feed_info.visibility = View.VISIBLE
        this@FeedInfoActivity.iv_disconnect_pinned_feed_info.visibility = View.VISIBLE
        this@FeedInfoActivity.tv_notice1_pinned_feed_info.visibility = View.VISIBLE
        this@FeedInfoActivity.tv_notice2_pinned_feed_info.visibility = View.VISIBLE
        this@FeedInfoActivity.appbar_layout.visibility = View.GONE
        this@FeedInfoActivity.scroll_view.visibility = View.GONE
    }

    fun createItemDecoration(): RecyclerView.ItemDecoration {
        return object : GapItemDecoration(VERTICAL_LIST,
            resources.getDimensionPixelSize(R.dimen.space_4)) {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                state: RecyclerView.State) {
                outRect.left = 0
                outRect.right = 0
                outRect.bottom = gap

                // Add top margin only for the first item to avoid double space between items
                if (parent.getChildAdapterPosition(view) == 0
                    || parent.getChildAdapterPosition(view) == 1) {
                    outRect.top = gap
                }
            }
        }
    }

    private fun getData() {
        launchUIWork {
            getFeedInfo(feedIdx, feedInfoCalledFrom)
        }

        getSearplerProfile(searperId)
        setSearplerProfileObserver()
        getPhotoEXIF(photoId)
        setPhotoEXIObserver()
        getPhotoLocation(photoId)
        setPhotoLocationObserver()
    }

    private fun getIntentData() {
        feedIdx = intent.getLongExtra(EXTRA_FEED_IDX, -1)
        photoId = intent.getStringExtra(EXTRA_PHOTO_ID)
        searperId = intent.getStringExtra(EXTRA_SEARPER_ID)
        feedPosition = intent.getIntExtra(EXTRA_FEED_INFO_POSITION, -1)
        feedInfoCalledFrom = intent.getIntExtra(EXTRA_FEED_INFO_CALLED_FROM, -1)
    }

    private suspend fun getFeedInfo(idx: Long, calledFrom: Int) = when(feedInfoCalledFrom) {
        CALLED_FROM_HOME_BEST_PICK_SEARPER_PHOTO -> {
            homeViewModel.loadHome()?.observe(this, Observer {
                it?.hotSearp?.searperPhoto?.let { feedItem ->
                    this.feedItem = feedItem
                    this.mediaType = feedItem.mediaType
                    launchUIWork {
                        displayFeedInfo(feedItem, calledFrom)
                    }
                }
            })
        }

        else -> {
            feedViewModel.getFeed(idx)?.observe(this, Observer {
                it?.let { feedItem ->
                    this.feedItem = feedItem
                    this.mediaType = feedItem.mediaType
                    launchUIWork {
                        displayFeedInfo(feedItem, calledFrom)
                    }
                }
            })
        }
    }

    private fun getSearplerProfile(id: String) {
        userProfileViewModel.setParameters(
            Parameters(
                id,
                -1,
                LOAD_PERSON,
                BOUND_FROM_BACKEND
            ), NONE_TYPE)
    }

    private fun setSearplerProfileObserver() = userProfileViewModel.person.observe(this, Observer { resource ->
        when(resource?.getStatus()) {
            Status.SUCCESS -> {
                resource.getData()?.let { person ->
                    searper = person as? Person?
                    displayUserInfo(searper!!)
                    slidingDrawer.setHeaderBackground(GeneralFunctions.getHeaderBackgroundUrl())
                    // slidingDrawer.setSearperProfileDrawer(searper,
                    //     SlidingDrawer.PROFILE_SEARPER_TYPE_FROM_FEED_VIEWER)
                }

                resource.getMessage()?.let {
                    showNetworkError(resource)
                }
            }

            Status.LOADING -> {
            }

            Status.ERROR -> {
                showNetworkError(resource)
            }

            else -> {
                showNetworkError(resource)
            }
        }
    })

    private fun getPhotoEXIF(photoId: String) {
        exifViewModel.setParameters(
            Parameters(
                photoId,
                -1,
                LOAD_EXIF,
                BOUND_FROM_BACKEND
            ), NONE_TYPE)
    }

    private fun setPhotoEXIObserver() = exifViewModel.exif.observe(this, Observer { resource ->
        this@FeedInfoActivity.folding_photo_info_cell.visibility = View.GONE
        this@FeedInfoActivity.cell_portion_information_container.visibility = View.GONE
        this@FeedInfoActivity.cell_photo_info_container.visibility = View.GONE
        when(resource?.getStatus()) {
            Status.SUCCESS -> {
                resource.getData()?.let { exifs ->
                    @Suppress("UNCHECKED_CAST")
                    exifList = exifs as? List<EXIF>?
                    if (exifList?.size!! > CHECK_EXIF_LIST_SIZE) {
                        displayEXIF(exifList!!)
                    }
                }

                resource.getMessage()?.let {
                    showNetworkError(resource)
                }
            }

            Status.LOADING -> { }

            Status.ERROR -> {
                showNetworkError(resource)
            }

            else -> {
                showNetworkError(resource)
            }
        }
    })

    private fun getPhotoLocation(photoId: String) {
        locationViewModel.setParameters(
            Parameters(
                photoId,
                -1,
                LOAD_GEO,
                BOUND_FROM_BACKEND
            ), NONE_TYPE)
    }

    private fun setPhotoLocationObserver() = locationViewModel.location.observe(this, Observer { resource ->
        this@FeedInfoActivity.place_cardholder.visibility = View.GONE
        when(resource?.getStatus()) {
            Status.SUCCESS -> {
                resource.getData()?.let { location ->
                    this.location = location as? Location?
                    displayLocation(this.location!!)
                }

                resource.getMessage()?.let {
                    showNetworkError(resource)
                }
            }

            Status.LOADING -> { }

            Status.ERROR -> {
                showNetworkError(resource)
            }

            else -> {
                showNetworkError(resource)
            }
        }
    })

    @MockData
    private fun transactFeedsMockData() {
        val feedsContent = FeedsContentDataSource()

        feedContentViewModel.loadFeedsContent.observe(this, Observer {
            it?.let { content ->
                createAdapter(content)
            }
        })

        feedsContent.setFeedsContent()
        feedContentViewModel.setFeedsContent(feedsContent.getFeedsContent()!!)
    }

    private fun createAdapter(content: FeedsContent) {
        val adapter = RecommendedFeedAdapter(this)

        adapter.addItem(content.contents)
        this@FeedInfoActivity.recycler_feed_view.adapter = adapter
    }

    private fun showNetworkError(resource: Resource) = when(resource.errorCode) {
        in 400..499 -> {
            Snackbar.make(this@FeedInfoActivity.coordinator_feed_info_layout, getString(R.string.phrase_client_wrong_request), LENGTH_LONG).show()
        }

        in 500..599 -> {
            Snackbar.make(this@FeedInfoActivity.coordinator_feed_info_layout, getString(R.string.phrase_server_wrong_response), LENGTH_LONG).show()
        }

        else -> {
            Snackbar.make(this@FeedInfoActivity.coordinator_feed_info_layout, resource.getMessage().toString(), LENGTH_LONG).show()
        }
    }

    private fun purchaseButtonAnimation() {
        val animation = btn_purchase.animate().translationYBy(-40f).setDuration(PURCHASE_BTN_ANIMATION_DURATION)

        animation.withEndAction {
            btn_purchase.animate().translationYBy(40f).duration = PURCHASE_BTN_ANIMATION_DURATION
        }
    }

    private fun finishAction() {
        if (feedInfoCalledFrom == CALLED_FROM_PINNED_FEED && !feedItem?.isPinned!!) {
            finish()
        } else {
            finishAfterTransition()
        }
    }

    private suspend fun displayFeedInfo(feedItem: FeedItem, calledFrom: Int) {
        asyncUIWork {
            loadPhoto(this@FeedInfoActivity, feedItem.media.m, calledFrom, feedItem)
        }

        asyncUIWork {
            fillExtraInfo(feedItem)
        }
    }

    private fun loadPhoto(activity: FeedInfoActivity, url: String?, calledFrom: Int, feedItem: FeedItem) {
        val photoPath = if (feedItem.mediaType == "photo") url?.substring(0, url.indexOf("_m")) + ".jpg"
        else url!!

        setFixedImageSize(0, 0)
        setImageDraw(this@FeedInfoActivity.iv_feed_info_photo, this@FeedInfoActivity.backdrop_container, photoPath, true)
        this@FeedInfoActivity.iv_feed_info_photo.setOnClickListener {
            this@FeedInfoActivity.iv_feed_info_photo.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + 0
            Caller.callViewer(activity, this@FeedInfoActivity.iv_feed_info_photo, 0, CALLED_FROM_FEED_INFO,
                photoPath, SELECTED_FEED_INFO_PHOTO_VIEW)
        }

        this@FeedInfoActivity.appbar_layout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener {
                _, verticalOffset ->
            if (this@FeedInfoActivity.collapsing_layout.height + verticalOffset < 2
                * ViewCompat.getMinimumHeight(this@FeedInfoActivity.collapsing_layout)) {
                // collapsed
                this@FeedInfoActivity.iv_feed_info_photo.animate().alpha(1.0f).duration = 600
            } else {
                // extended
                this@FeedInfoActivity.iv_feed_info_photo.animate().alpha(1.0f).duration = 1000    // 1.0f means opaque
            }
        })

        withDelay(50L) {
            when(calledFrom) {
                CALLED_FROM_FEED, CALLED_FROM_PINNED_FEED, CALLED_FROM_HOME_BEST_PICK_HOT_PHOTO,
                CALLED_FROM_HOME_BEST_PICK_SEARPER_PHOTO -> {
                    this@FeedInfoActivity.iv_feed_info_photo.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + feedPosition
                    sharedElementCallback = FeedInfoItemCallback()
                    sharedElementCallback.setViewBinding(this@FeedInfoActivity.iv_feed_info_photo)
                    setEnterSharedElementCallback(sharedElementCallback)
                    supportStartPostponedEnterTransition()
                }
            }
        }

        when (feedItem.mediaType) {
            "photo" -> {
                this.iv_play_btn_feed_info.visibility = View.GONE
                this.iv_fullsize.visibility = View.GONE
                this.iv_feed_info_photo.visibility = View.VISIBLE
                this.video_view_feed_info.visibility = View.GONE
            }

            "video" -> {
                this.iv_play_btn_feed_info.visibility = View.VISIBLE
                this.iv_fullsize.visibility = View.VISIBLE
                this.iv_feed_info_photo.visibility = View.VISIBLE
                this.video_view_feed_info.visibility = View.GONE
                this.videoUrl = feedItem.videoSource!!
                initializePlayer(feedItem.videoSource)

                this.iv_play_btn_feed_info.setOnClickListener {
                    this.video_view_feed_info.visibility = View.VISIBLE
                    this.iv_feed_info_photo.visibility = View.INVISIBLE
                    player?.playWhenReady = true
                }
            }

            else -> {

            }
        }
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun fillExtraInfo(feedItem: FeedItem) {
        feedTitle = if (feedItem.title.isEmpty() || feedItem.title == " ") {
            this.applicationContext.getString(R.string.no_title)
        } else {
            feedItem.title
        }

        description = feedItem.description
        showDescription(description!!)
        setExtraFont()

        //In case of getting real data, have to be implemented with new code...
        @MockData
        this@FeedInfoActivity.tv_like_count.text = Random.nextInt(500).toString()

        //In case of getting real data, have to be implemented with new code...
        @MockData
        this@FeedInfoActivity.tv_comment_count.text = Random.nextInt(100).toString()

        //In case of getting real data, have to be implemented with new code...
        @MockData
        when(Random.nextInt(2)) {
            0 -> {
                this@FeedInfoActivity.tv_price.text = "4"
            }

            1 -> {
                this@FeedInfoActivity.tv_price.text = "8"
            }

            2 -> {
                this@FeedInfoActivity.tv_price.text = "12"
            }
        }

        this@FeedInfoActivity.iv_like.isSelected = feedItem.like == 1
        this@FeedInfoActivity.iv_like.setOnClickListener {
            this@FeedInfoActivity.iv_like.isSelected = !this@FeedInfoActivity.iv_like.isSelected
            if (this@FeedInfoActivity.iv_like.isSelected) {
                feedItem.like = 1
            } else {
                feedItem.like = 0
            }

            isUpdated = !isUpdated
        }

        this.iv_fullsize.setOnClickListener {
            videoUrl?.let { Caller.callFullSizePlayer(this, it) }
        }

        val licensePhrase = handleLicense()

        this@FeedInfoActivity.iv_license1.setOnClickListener {
            showToolTip(this@FeedInfoActivity.iv_license1, getString(R.string.license_buy_on_searp),
                resources.displayMetrics.widthPixels, TOOL_TIP_DEFAULT_DURATION)
        }

        this@FeedInfoActivity.iv_license2.setOnClickListener {
            showToolTip(this@FeedInfoActivity.iv_license2, licensePhrase, TOO_TIP_MULTI_LINE_WIDTH, TOOL_TIP_MULTI_LINE_DURATION)
        }

        this@FeedInfoActivity.iv_license3.setOnClickListener {
            showToolTip(this@FeedInfoActivity.iv_license2, licensePhrase, TOO_TIP_MULTI_LINE_WIDTH, TOOL_TIP_MULTI_LINE_DURATION)
        }

        this@FeedInfoActivity.iv_license4.setOnClickListener {
            showToolTip(this@FeedInfoActivity.iv_license2, licensePhrase, TOO_TIP_MULTI_LINE_WIDTH, TOOL_TIP_MULTI_LINE_DURATION)
        }

        this@FeedInfoActivity.iv_license5.setOnClickListener {
            showToolTip(this@FeedInfoActivity.iv_license2, licensePhrase, TOO_TIP_MULTI_LINE_WIDTH, TOOL_TIP_MULTI_LINE_DURATION)
        }

        val by=  getString(R.string.space) + getString(R.string.by_symbol) + getString(R.string.space)

        this@FeedInfoActivity.iv_size_small.isSelected = false
        this@FeedInfoActivity.iv_size_medium.isSelected = false
        this@FeedInfoActivity.iv_size_xl.isSelected = true

        @MockData
        withDelay(MOCK_SIZE_DATA_DELAY_DURATION) {
            //In case of getting real data, have to be implemented with new code...
            val width = photoWidth * 4
            val height = photoHeight * 4

            @MockData
            this@FeedInfoActivity.tv_size.text = width.toString() + by + height.toString()
            this@FeedInfoActivity.tv_size_price.text = "12 $"
        }

        this@FeedInfoActivity.iv_size_small.setOnClickListener {
            this@FeedInfoActivity.iv_size_small.isSelected = !this@FeedInfoActivity.iv_size_small.isSelected
            this@FeedInfoActivity.iv_size_medium.isSelected = false
            this@FeedInfoActivity.iv_size_xl.isSelected = false
            //In case of getting real data, have to be implemented with new code...
            @MockData
            this@FeedInfoActivity.tv_size.text = photoWidth.toString() + by + photoHeight.toString()
            this@FeedInfoActivity.tv_size_price.text = "4 $"
        }

        this@FeedInfoActivity.iv_size_medium.setOnClickListener {
            this@FeedInfoActivity.iv_size_medium.isSelected = !this@FeedInfoActivity.iv_size_medium.isSelected
            this@FeedInfoActivity.iv_size_small.isSelected = false
            this@FeedInfoActivity.iv_size_xl.isSelected = false
            //In case of getting real data, have to be implemented with new code...
            val width = photoWidth * 3
            val height = photoHeight * 3

            @MockData
            this@FeedInfoActivity.tv_size.text = width.toString() + by + height.toString()
            this@FeedInfoActivity.tv_size_price.text = "8 $"
        }

        this@FeedInfoActivity.iv_size_xl.setOnClickListener {
            this@FeedInfoActivity.iv_size_xl.isSelected = !this@FeedInfoActivity.iv_size_xl.isSelected
            this@FeedInfoActivity.iv_size_small.isSelected = false
            this@FeedInfoActivity.iv_size_medium.isSelected = false
            //In case of getting real data, have to be implemented with new code...
            val width = photoWidth * 4
            val height = photoHeight * 4

            @MockData
            this@FeedInfoActivity.tv_size.text = width.toString() + by + height.toString()
            this@FeedInfoActivity.tv_size_price.text = "12 $"
        }
    }

    @Suppress("NAME_SHADOWING")
    @SuppressLint("SetTextI18n")
    private fun showDescription(description: String) {
        setFontTypeface(this@FeedInfoActivity.tv_feed_explanation, FONT_TYPE_REGULAR)
        val html = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(description)
        }

        if (html.length < 78) {
            this@FeedInfoActivity.tv_feed_explanation.text = html.toString() + getString(R.string.default_description)
        } else {
            this@FeedInfoActivity.tv_feed_explanation.text = html.toString()
        }

    }

    private fun displayEXIF(listEXIF: List<EXIF>) {
        launchEXIF(listEXIF)
    }

    private fun addEXIF(lisEXIF: List<EXIF>): ArrayList<EXIF> {
        val usefulEXIF = ArrayList<EXIF>()
        val model = exifHandler.getEXIFByLabel(lisEXIF, "Model")
            ?: exifHandler.putManualEXIF("Model")
        usefulEXIF.add(model)
        val make = exifHandler.getEXIFByLabel(lisEXIF, "Make")
            ?: exifHandler.putManualEXIF("Make")
        usefulEXIF.add(make)
        val exifExposure = exifHandler.getEXIFByLabel(lisEXIF, "Exposure")
            ?: exifHandler.putManualEXIF("Exposure")
        usefulEXIF.add(exifExposure)
        val exifAperture = exifHandler.getEXIFByLabel(lisEXIF, "Aperture")
            ?: exifHandler.putManualEXIF("Aperture")
        usefulEXIF.add(exifAperture)
        val exifISO = exifHandler.getEXIFByLabel(lisEXIF, "ISO Speed")
            ?: exifHandler.putManualEXIF("ISO Speed")
        usefulEXIF.add(exifISO)
        val exifFlash = exifHandler.getEXIFByLabel(lisEXIF, "Flash")
            ?: exifHandler.putManualEXIF("Flash")
        usefulEXIF.add(exifFlash)
        val exifWhiteBalance = exifHandler.getEXIFByLabel(lisEXIF, "White Balance")
            ?: exifHandler.putManualEXIF("White Balance")
        usefulEXIF.add(exifWhiteBalance)
        val exifFocalLength = exifHandler.getEXIFByLabel(lisEXIF, "Focal Length")
            ?: exifHandler.putManualEXIF("Focal Length")
        usefulEXIF.add(exifFocalLength)

        return usefulEXIF
    }

    private fun displayLocation(location: Location) {
        setLocationInvisible()
        animateLocation(location)
    }

    private fun setFoldingCellInvisible() {
        if (!this@FeedInfoActivity.folding_photo_info_cell.isShown) {
            this@FeedInfoActivity.folding_photo_info_cell.visibility = View.GONE
            this@FeedInfoActivity.cell_portion_information_container.visibility = View.GONE
            this@FeedInfoActivity.cell_photo_info_container.visibility = View.GONE
        }
    }

    private fun setLocationInvisible() {
        if (!this@FeedInfoActivity.place_cardholder.isShown) {
            this@FeedInfoActivity.place_cardholder.visibility = View.GONE
        }
    }

    private fun doDownload(searperId: String, publishedDate: String?) {
        val savePhoto = SavePhoto(workHandler, saver)

        savePhoto.bitmapDrawable = BitmapDrawable(resources, photo)
        val dateTime: Long
        try {
            dateTime = convertDateToLong(publishedDate!!)
        } catch (e: ParseException) {
            e.printStackTrace()
            Timber.d(e)

            return
        }

        var photoURL = "None"

        searper?.let {
            photoURL = if (searper?.iconserver == "0") {
                "None"
            } else {
                workHandler.getProfilePhotoURL(searper?.iconfarm!!, searper?.iconserver!!, searper?.id!!)
            }
        }

        savePhoto.fileName = String.format("%s.jpg", searperId + "_" + dateTime.toString())
        exifList?.let {
            addEXIF(exifList!!)
            if (exifHandler.getEXIFNoneItemCount() < CHECK_EXIF_LIST_SIZE) {
                savePhoto.exifItems = addEXIF(exifList!!)
            }
        }

        savePhoto.location = location
        savePhoto.searperInfo = searper
        savePhoto.searperPhotoUrl = photoURL
        savePhoto.title = feedTitle
        savePhoto.enabledSaveEXIF = this@FeedInfoActivity.cell_portion_information_container.isShown
        savePhoto.enabledSaveLocation = this@FeedInfoActivity.place_cardholder.isShown
        savePhoto.showSaveDialog(this, localEXIFViewModel, localLocationViewModel,
            localSavedPhotoViewModel, savePhoto)
    }

    private fun callShareToFacebook(drawable: BitmapDrawable, caption: String) {
        workHandler.shareToFacebook(drawable.bitmap, this)
    }

    private fun animateEXIF(lisEXIF: List<EXIF>) {
        this@FeedInfoActivity.folding_photo_info_cell.visibility = View.VISIBLE
        this@FeedInfoActivity.divider_4.visibility = View.VISIBLE
        if (!this@FeedInfoActivity.folding_photo_info_cell.isUnfolded) {
            if (!this@FeedInfoActivity.cell_photo_info_container.isShown) {
                this@FeedInfoActivity.cell_photo_info_container.visibility = View.VISIBLE
                val animation = AnimationUtils.loadAnimation(
                    this.applicationContext, R.anim.scale_up_enter)
                animation.duration = 300
                this@FeedInfoActivity.cell_photo_info_container.animation = animation
                this@FeedInfoActivity.cell_photo_info_container.animate()
                animation.start()
                isUnfolded = true
                setEXINFO(lisEXIF)
            }

            this@FeedInfoActivity.folding_photo_info_cell.setOnClickListener {
                this@FeedInfoActivity.folding_photo_info_cell.toggle(false)
                isUnfolded = !isUnfolded
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setEXINFO(items: List<EXIF>) {
        val model: String = (items[CommonWorkHandler.EXIF_ITEM_INDEX_MAKE].raw._content + " "
            + items[CommonWorkHandler.EXIF_ITEM_INDEX_MODEL].raw._content)
        val none = "None"
        val by=  getString(R.string.space) + getString(R.string.by_symbol) + getString(R.string.space)

        this@FeedInfoActivity.tv_cam_model.text = model
        this@FeedInfoActivity.tv_exposure.text = none

        setEXIFFont()
        this@FeedInfoActivity.tv_photo_date.text = feedItem?.takenDate

        //In case of getting real data, have to be implemented with new code...
        val width = photoWidth * 3
        val height = photoHeight * 3
        @MockData
        this@FeedInfoActivity.tv_photo_size.text =  width.toString() + by + height.toString()

        this@FeedInfoActivity.tv_exposure.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_EXPOSURE].raw._content
        this@FeedInfoActivity.tv_aperture.text = none
        this@FeedInfoActivity.tv_aperture.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_APERTURE].raw._content
        this@FeedInfoActivity.tv_iso.text = none
        this@FeedInfoActivity.tv_iso.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_ISO_SPEED].raw._content
        this@FeedInfoActivity.tv_flash.text = none
        this@FeedInfoActivity.tv_flash.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_FLASH].raw._content
        this@FeedInfoActivity.tv_white_balance.text = none
        this@FeedInfoActivity.tv_white_balance.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_WHITE_BALANCE].raw._content
        this@FeedInfoActivity.tv_focal_length.text = none
        this@FeedInfoActivity.tv_focal_length.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_FOCAL_LENGTH].raw._content
    }

    private fun animateLocation(location: Location) {
        this@FeedInfoActivity.divider_5.visibility = View.VISIBLE
        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync { map ->
            map.setOnMarkerDragListener(this@FeedInfoActivity)
            map.uiSettings.isZoomControlsEnabled = false
            // Add a marker in my location and move the camera
            val myLocation = LatLng(location.latitude!!.toDouble(), location.longitude!!.toDouble())
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, MAP_ZOOM))
            val zoom = CameraUpdateFactory.zoomTo(MAP_ZOOM)
            map.animateCamera(zoom)
            myLocation.let {
                marker = map.addMarker(MarkerOptions()
                    .position(myLocation)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pegman))
                    .title("Address").infoWindowAnchor(0.5f, 0.5f)
                    .snippet(getAddress(location))
                    .draggable(true))
            }

            marker.showInfoWindow()
            map.setOnMarkerClickListener {
                false
            }
        }

        if (!this@FeedInfoActivity.place_cardholder.isShown) {
            this@FeedInfoActivity.place_cardholder.visibility = View.VISIBLE
            val animation = AnimationUtils.loadAnimation(
                this.applicationContext, R.anim.scale_up_enter)

            animation.duration = 300
            this@FeedInfoActivity.place_cardholder.animation = animation
            this@FeedInfoActivity.place_cardholder.animate()
            animation.start()
        }

        this@FeedInfoActivity.place_cardholder.setOnClickListener {
            Caller.callViewMap(this.applicationContext,
                this@FeedInfoActivity.collapsing_layout.title.toString(),
                location.latitude!!.toDouble(),
                location.longitude!!.toDouble(),
                getAddress(location))
        }
    }

    private fun getAddress(location: Location): String {
        val addressArray = arrayOf("", "", "", "", "")

        addressArray[0] = location.neighbourhood?._content ?: ""
        addressArray[1] = location.locality?._content ?: ""
        addressArray[2] = location.county?._content ?: ""
        addressArray[3] = location.region?._content ?: ""
        addressArray[4] = location.country?._content ?: ""

        return addressArray[0] + ", " + addressArray[1] + ", " + addressArray[2] + ", " + addressArray[3] + ", " + addressArray[4]
    }

    private fun setViewBind() {
        sharedElementCallback = FeedInfoItemCallback()

        this@FeedInfoActivity.iv_feed_info_photo.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + feedPosition
        sharedElementCallback.setViewBinding(this@FeedInfoActivity.iv_feed_info_photo)
    }

    @SuppressLint("CheckResult")
    fun displayUserInfo(userInfo: Person) {
        var name = userInfo.realname?._content

        name = name ?: userInfo.username?._content
        setFontTypeface(this@FeedInfoActivity.tv_searper_name, FONT_TYPE_MEDIUM)
        if (name == "") {
            this@FeedInfoActivity.tv_searper_name.text = userInfo.username?._content
        } else {
            this@FeedInfoActivity.tv_searper_name.text = name
        }

        searperPhotoUrl = workHandler.getProfilePhotoURL(userInfo.iconfarm, userInfo.iconserver, userInfo.id)
        if (userInfo.iconserver == "0") {
            this@FeedInfoActivity.iv_searper_pic.setImageDrawable(this.applicationContext!!
                .getDrawable(R.drawable.ic_default_profile))
        } else {
            setImageDraw(this@FeedInfoActivity.iv_searper_pic, searperPhotoUrl!!)
        }

        this@FeedInfoActivity.iv_searper_pic.setOnClickListener{
            // slidingDrawer.searperProfileDrawerForFeedViewDrawer?.openDrawer()
            Caller.callOtherUserProfile(this, CALLED_FROM_FEED_INFO, searperId, name!!, 3, searperPhotoUrl!!)
        }

        // Temporarily get blocked.....
        /*
        val userName=if (userInfo.username?._content?.isEmpty()!!) {
            fragment.getString(R.string.no_username)
        } else {
            userInfo.username._content
        }
        */
    }

    //In case of getting real data, have to be implemented with new code...
    @MockData
    private fun handleLicense(): String {
        var licensePhrase = ""

        this@FeedInfoActivity.iv_license1.isSelected = true
        this@FeedInfoActivity.iv_license2.isSelected = true
        when(Random.nextInt(5)) {
            0 -> {
                this@FeedInfoActivity.iv_license3.isSelected = false
                this@FeedInfoActivity.iv_license4.isSelected = false
                this@FeedInfoActivity.iv_license5.isSelected = false
                licensePhrase = getString(R.string.license_cc_by)
            }

            1 -> {
                this@FeedInfoActivity.iv_license3.isSelected = false
                this@FeedInfoActivity.iv_license4.isSelected = false
                this@FeedInfoActivity.iv_license5.isSelected = true
                licensePhrase = getString(R.string.license_cc_by_sa)
            }

            2 -> {
                this@FeedInfoActivity.iv_license3.isSelected = false
                this@FeedInfoActivity.iv_license4.isSelected = true
                this@FeedInfoActivity.iv_license5.isSelected = false
                licensePhrase = getString(R.string.license_by_nd)
            }

            3 -> {
                this@FeedInfoActivity.iv_license3.isSelected = true
                this@FeedInfoActivity.iv_license4.isSelected = false
                this@FeedInfoActivity.iv_license5.isSelected = false
                licensePhrase = getString(R.string.license_by_nc)
            }

            4 -> {
                this@FeedInfoActivity.iv_license3.isSelected = true
                this@FeedInfoActivity.iv_license4.isSelected = false
                this@FeedInfoActivity.iv_license5.isSelected = true
                licensePhrase = getString(R.string.license_by_nc_sa)
            }

            5 -> {
                this@FeedInfoActivity.iv_license3.isSelected = true
                this@FeedInfoActivity.iv_license4.isSelected = true
                this@FeedInfoActivity.iv_license5.isSelected = false
                licensePhrase = getString(R.string.license_by_nc_nd)
            }
        }

        return licensePhrase
    }

    private fun setEXIFFont() {
        setFontTypeface(this@FeedInfoActivity.tv_photo_date, FONT_TYPE_REGULAR)
        setFontTypeface(this@FeedInfoActivity.tv_cam_model, FONT_TYPE_REGULAR)
        setFontTypeface(this@FeedInfoActivity.tv_photo_size, FONT_TYPE_REGULAR)
        setFontTypeface(this@FeedInfoActivity.tv_exposure, FONT_TYPE_REGULAR)
        setFontTypeface(this@FeedInfoActivity.tv_aperture, FONT_TYPE_REGULAR)
        setFontTypeface(this@FeedInfoActivity.tv_iso, FONT_TYPE_REGULAR)
        setFontTypeface(this@FeedInfoActivity.tv_flash, FONT_TYPE_REGULAR)
        setFontTypeface(this@FeedInfoActivity.tv_white_balance, FONT_TYPE_REGULAR)
        setFontTypeface(this@FeedInfoActivity.tv_focal_length, FONT_TYPE_REGULAR)
    }

    private fun setExtraFont() {
        setFontTypeface(this@FeedInfoActivity.tv_like_count, FONT_TYPE_REGULAR)
        setFontTypeface(this@FeedInfoActivity.tv_like_phrase, FONT_TYPE_REGULAR)
        setFontTypeface(this@FeedInfoActivity.tv_comment_count, FONT_TYPE_REGULAR)
        setFontTypeface(this@FeedInfoActivity.tv_comment_phrase, FONT_TYPE_REGULAR)
        setFontTypeface(this@FeedInfoActivity.tv_information_portion_title, FONT_TYPE_MEDIUM)
        setFontTypeface(this@FeedInfoActivity.tv_information_title, FONT_TYPE_MEDIUM)
        setFontTypeface(this@FeedInfoActivity.tv_license_title, FONT_TYPE_MEDIUM)
        setFontTypeface(this@FeedInfoActivity.tv_size, FONT_TYPE_REGULAR)
        setFontTypeface(this@FeedInfoActivity.tv_size_price, FONT_TYPE_MEDIUM)
        setFontTypeface(this@FeedInfoActivity.tv_place_title, FONT_TYPE_MEDIUM)
        setFontTypeface(this@FeedInfoActivity.tv_recommend_title, FONT_TYPE_MEDIUM)
        setFontTypeface(this@FeedInfoActivity.btn_purchase, FONT_TYPE_MEDIUM)
    }

    private fun setActivityResult(calledFrom: Int) = when (calledFrom) {
        CALLED_FROM_FEED -> {
            val intent = Intent(this, HomeActivity::class.java)

            intent.putExtra(EXTRA_FEED_INFO_POSITION, feedPosition)
            setResult(SELECTED_FEED_ITEM_POSITION, intent)
        }

        CALLED_FROM_HOME_BEST_PICK_HOT_PHOTO -> {
            val intent = Intent(this, HomeActivity::class.java)

            intent.putExtra(EXTRA_FEED_INFO_POSITION, feedPosition)
            setResult(SELECTED_BEST_PICK_HOT_PHOTO_POSITION, intent)
        }

        CALLED_FROM_HOME_BEST_PICK_SEARPER_PHOTO -> {
            val intent = Intent(this, HomeActivity::class.java)

            intent.putExtra(EXTRA_FEED_INFO_POSITION, feedPosition)
            setResult(Caller.SELECTED_BEST_PICK_SEARPER_PHOTO_POSITION, intent)
        }
        else -> {}
    }

    private fun launchEXIF(listEXIF: List<EXIF>) = launchUIWork {
        showEXIF(listEXIF)
    }

    private suspend fun showEXIF(listEXIF: List<EXIF>) = withContext(Dispatchers.Main) {
        var result: List<EXIF> = ArrayList()

        withContext(uiScope.coroutineContext) {
            result = addEXIF(listEXIF)
        }

        setFoldingCellInvisible()
        if (exifHandler.getEXIFNoneItemCount() < CHECK_EXIF_NONE_ITEM_COUNT) {
            animateEXIF(result)
        }

        exifHandler.resetEXIFNoneItemCount()
    }

    private fun removePhotoCache(type: Int) = launchIOWork {
        when(type) {
            CACHE_USER_PROFILE_TYPE -> {
                userProfileViewModel.removePerson()
            }

            CACHE_PHOTO_EXIF_TYPE -> {
                exifViewModel.removeEXIF()
            }

            CACHE_PHOTO_LOCATION_TYPE -> {
                locationViewModel.removeLocation()
            }

            CACHE_PHOTO_COMMENTS_TYPE -> {
                commentViewModel.removeComments()
            }
        }
    }

    private fun initializePlayer(source: String) {
        val trackSelector = DefaultTrackSelector()
        trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSizeSd())

        player = ExoPlayerFactory.newSimpleInstance(this)
        player?.addListener(playBackStateListener)
        this.video_view_feed_info.player = player

        val uri = Uri.parse(source)
        val mediaSource = buildMediaSource(uri)

        player?.playWhenReady = false
        player?.seekTo(currentWindow, playbackPosition)
        player?.prepare(mediaSource, false, false)
    }

    private fun releasePlayer() {
        player?.let {
            playbackPosition = it.currentPosition
            currentWindow = it.currentWindowIndex

            it.removeListener(playBackStateListener)
            it.release()
            player = null
        }
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory = DefaultDataSourceFactory(this, "sample_test")

        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    /**
     * Helper function to call something doing function
     *
     * By marking `block` as `suspend` this creates a suspend lambda which can call suspend
     * functions.
     *
     * @param block lambda to actually do some work. It is called in the uiScope.
     *              lambda the some work will do
     */
    private inline fun launchUIWork(crossinline block: suspend () -> Unit): Job {
        return uiScope.launch {
            block()
        }
    }

    /**
     * Helper function to call something doing function
     *
     * By marking `block` as `suspend` this creates a suspend lambda which can call suspend
     * functions.
     *
     * @param block lambda to actually do some work. It is called in the uiScope.
     *              lambda the some work will do
     */
    private suspend inline fun asyncUIWork(crossinline block: suspend () -> Unit): Job {
        val async = uiScope.async {
            block()
        }

        async.await()

        return async
    }

    /**
     * Helper function to call something doing function
     *
     * By marking `block` as `suspend` this creates a suspend lambda which can call suspend
     * functions.
     *
     * @param block lambda to actually do some work. It is called in the ioScope.
     *              lambda the some work will do
     */
    private inline fun launchIOWork(crossinline block: suspend () -> Unit): Job {
        return ioScope.launch {
            block()
        }
    }

    inner class PlayBackStateListener: Player.EventListener {

        @SuppressLint("BinaryOperationInTimber")
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)
            val stateString: String

            when (playbackState) {
                ExoPlayer.STATE_IDLE -> stateString = "ExoPlayer.STATE_IDLE      -"
                ExoPlayer.STATE_BUFFERING -> stateString = "ExoPlayer.STATE_BUFFERING      -"
                ExoPlayer.STATE_READY -> {
                    stateString = "ExoPlayer.STATE_READY      -"
                }
                ExoPlayer.STATE_ENDED -> stateString = "ExoPlayer.STATE_ENDED      -"
                else -> stateString = "UNKNOWN_STATE      -"
            }

            Timber.d("changed state to " + stateString
                + " playWhenReady: " + playWhenReady)
        }
    }
}