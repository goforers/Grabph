package com.goforer.grabph.presentation.ui.feed.feedinfo

import android.annotation.SuppressLint
import android.app.SharedElementCallback
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.transition.Transition
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import com.goforer.base.annotation.MockData
import com.goforer.base.presentation.utils.CommonUtils.withDelay
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.base.presentation.view.customs.listener.OnSwipeOutListener
import com.goforer.grabph.R
import com.goforer.grabph.data.datasource.model.cache.data.entity.exif.EXIF
import com.goforer.grabph.data.datasource.model.cache.data.entity.feed.FeedItem
import com.goforer.grabph.data.datasource.model.cache.data.entity.location.Location
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.Person
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_EXIF
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_PERSON
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.data.datasource.network.response.Status
import com.goforer.grabph.data.repository.remote.Repository.Companion.BOUND_FROM_BACKEND
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_FEED
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_FEED_INFO
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_HOME_BEST_PICK_HOT_PHOTO
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_HOME_BEST_PICK_SEARPER_PHOTO
import com.goforer.grabph.presentation.caller.Caller.SELECTED_FEED_INFO_PHOTO_VIEW
import com.goforer.grabph.presentation.common.effect.transition.TransitionCallback
import com.goforer.grabph.presentation.common.effect.transition.TransitionObject
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import com.goforer.grabph.presentation.common.utils.handler.exif.EXIFHandler
import com.goforer.grabph.presentation.ui.feed.feedinfo.sharedelementcallback.FeedInfoItemCallback
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.presentation.ui.photoviewer.sharedelementcallback.PhotoViewerCallback
import com.goforer.grabph.presentation.vm.feed.FeedViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_feed_item.*
import kotlinx.android.synthetic.main.activity_feed_item.backdrop_container
import kotlinx.android.synthetic.main.activity_feed_item.progress_bar_bottom
import kotlinx.android.synthetic.main.cell_photo_info.*
import kotlinx.android.synthetic.main.cell_portion_photo_info.*
import kotlinx.android.synthetic.main.layout_disconnection.*
import kotlinx.android.synthetic.main.layout_selection_content_size.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class FeedItemActivity : BaseActivity() {
    private lateinit var sharedElementCallback: FeedInfoItemCallback

    private var feedItem: FeedItem? = null

    internal lateinit var photoId: String
    private lateinit var userId: String
    private lateinit var photoPath: String

    private lateinit var playBackStateListener: PlayBackStateListener

    private var feedTitle: String? = null
    private var description: String? = null
    private var searperPhotoUrl: String? = null
    private var videoUrl: String? = null
    private var mediaType: String? = null
    private var exifList: List<EXIF>? = null
    private var location: Location? = null

    private var player: SimpleExoPlayer? = null
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var selectedContentSize = 0

    private var feedIdx: Long = 0

    private var feedPosition = 0
    private var feedInfoCalledFrom = 0
    private var offsetChange = 0
    private var isScrollOnTop = true

    private var searper: Person? = null

    private val job = Job()
    private val ioScope = CoroutineScope(Dispatchers.IO + job)

    @field:Inject
    lateinit var workHandler: CommonWorkHandler

    @field:Inject
    lateinit var exifHandler: EXIFHandler

    @field:Inject
    lateinit var viewModel: FeedViewModel

    private val sharedEnterListener = object : TransitionCallback() {
        override fun onTransitionEnd(transition: Transition) {
            super.onTransitionEnd(transition)
            removeCallback()
        }

        override fun onTransitionCancel(transition: Transition) {
            super.onTransitionCancel(transition)
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
            setExitSharedElementCallback(null as androidx.core.app.SharedElementCallback?)
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

        private const val CONTENT_SIZE_S = 1
        private const val CONTENT_SIZE_M = 2
        private const val CONTENT_SIZE_L = 3
    }

    override fun setContentView() { setContentView(R.layout.activity_feed_item) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isNetworkAvailable) {
            window.statusBarColor = Color.TRANSPARENT
            offsetChange = 0
            networkStatusVisible(true)
        } else {
            networkStatusVisible(false)
        }

        Timber.plant(Timber.DebugTree())
    }

    override fun setViews(savedInstanceState: Bundle?) {
        super.setViews(savedInstanceState)
        window.sharedElementEnterTransition.addListener(sharedEnterListener)
        supportPostponeEnterTransition()
        playBackStateListener = PlayBackStateListener()

        getIntentData()
        selectSizesButtonClickListener()
        removeCache()
        getData()
        initCoordinatorLayout()
        setLicenseRandomCheck()
    }

    override fun setActionBar() {
        setSupportActionBar(this.toolbar_feed_item)
        val actionBar = supportActionBar

        actionBar?.run {
            setHomeAsUpIndicator(R.drawable.ic_back)
            displayOptions = ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_USE_LOGO
            setDisplayShowTitleEnabled(true)
            elevation = 0f
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
        this.toolbar_feed_item.setNavigationOnClickListener { finishAfterTransition() }
        this.toolbar_feed_item.hideOverflowMenu()
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)

        if (resultCode == SELECTED_FEED_INFO_PHOTO_VIEW) {
            window.sharedElementExitTransition.addListener(sharedExitListener)
            supportPostponeEnterTransition()

            val callback = PhotoViewerCallback()
            callback.setViewBinding(this.iv_feed_item_photo)
            setEnterSharedElementCallback(callback)
            supportStartPostponedEnterTransition()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // outState.putString(Caller.EXTRA_QUEST_OWNER_NAME, ownerName)
        // outState.putString(Caller.EXTRA_QUEST_OWNER_LOGO, ownerLogo)
        // outState.putString(Caller.EXTRA_QUEST_OWNER_IMAGE, ownerImage)
        // outState.putString(Caller.EXTRA_QUEST_TITLE, title)
        // outState.putString(Caller.EXTRA_QUEST_DESCRIPTION, description)
        // outState.putString(Caller.EXTRA_QUEST_STATE, state)
        // outState.putString(Caller.EXTRA_QUEST_REWARD, reward)
        // outState.putInt(Caller.EXTRA_QUEST_DURATION, duration)
        // outState.putInt(Caller.EXTRA_QUEST_POSITION, position)
    }

    private fun getIntentData() {
        feedIdx = intent.getLongExtra(Caller.EXTRA_FEED_IDX, -1)
        photoId = intent.getStringExtra(Caller.EXTRA_PHOTO_ID)
        userId = intent.getStringExtra(Caller.EXTRA_SEARPER_ID)
        feedPosition = intent.getIntExtra(Caller.EXTRA_FEED_INFO_POSITION, -1)
        feedInfoCalledFrom = intent.getIntExtra(Caller.EXTRA_FEED_INFO_CALLED_FROM, -1)
        photoPath = intent.getStringExtra(Caller.EXTRA_PHOTO_PATH)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAfterTransition()
    }

    override fun finishAfterTransition() {
        supportPostponeEnterTransition()
        setViewBind()
        setActivityResult(feedInfoCalledFrom)
        this.video_view_feed_item.visibility = View.GONE
        this.iv_feed_item_photo.visibility = View.VISIBLE
        this.iv_fullsize_feed_item.visibility = View.GONE

        super.finishAfterTransition()
    }

    override fun onPause() {
        super.onPause()
        if (mediaType == getString(R.string.media_type_video) && Build.VERSION.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (mediaType == getString(R.string.media_type_video) && Build.VERSION.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        setScreenOrientationStable()
        if (mediaType == getString(R.string.media_type_video) && Build.VERSION.SDK_INT < 24) {
            videoUrl?.let { initializePlayer(it) }
        }
    }

    override fun onStart() {
        super.onStart()
        if (mediaType == getString(R.string.media_type_video) && Build.VERSION.SDK_INT >= 24) {
            videoUrl?.let { initializePlayer(it) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ioScope.cancel()
        job.cancel()
    }

    private fun getData() {
        setFixedImageSize(0, 0)
        setImageDraw(this.iv_feed_item_photo, this.backdrop_container, photoPath, true)

        withDelay(50L) {
            setViewBind()
            setEnterSharedElementCallback(sharedElementCallback)
            supportStartPostponedEnterTransition()
        }

        when (feedInfoCalledFrom) {
            CALLED_FROM_FEED -> {
                val liveData = viewModel.getFeed(feedIdx)
                liveData?.observe(this, Observer {
                    this.feedItem = it
                    this.mediaType = it.mediaType

                    setFeedItemData(it)
                    liveData.removeObservers(this)
                })
            }
        }

        setBottomOnLoading()
        getUserProfile(userId)
        setUserProfileObserver()
        getPhotoEXIF(photoId)
        setPhotoEXIFObserver()
    }

    private fun setFeedItemData(feedItem: FeedItem) {
        setFeedItemView(feedItem)

        this.tv_feed_item_title.text = feedItem.title
        this.tv_description_feed_item.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(feedItem.description, Html.FROM_HTML_MODE_LEGACY)
        } else {
            feedItem.description
        }

        val url = feedItem.media.m
        val photoPath = if (feedItem.mediaType == getString(R.string.media_type_photo)) url?.substring(0, url.indexOf("_m")) + ".jpg"
        else url!!

        this.iv_feed_item_photo.setOnClickListener {
            this.iv_feed_item_photo.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + 0
            Caller.callViewer(this, this.iv_feed_item_photo, 0, CALLED_FROM_FEED_INFO,
                photoPath, SELECTED_FEED_INFO_PHOTO_VIEW)
        }
    }

    private fun setFeedItemView(feedItem: FeedItem) {
        when (feedItem.mediaType) {
            getString(R.string.media_type_photo) -> {
                this.iv_play_btn_feed_item.visibility = View.GONE
                this.iv_feed_item_photo.visibility = View.VISIBLE
                this.video_view_feed_item.visibility = View.GONE
                this.iv_fullsize_feed_item.visibility = View.GONE
            }

            getString(R.string.media_type_video) -> {
                this.iv_feed_item_photo.visibility = View.VISIBLE
                this.video_view_feed_item.visibility = View.GONE
                this.iv_fullsize_feed_item.visibility = View.VISIBLE
                this.videoUrl = feedItem.videoSource!!
                initializePlayer(feedItem.videoSource)

                this.iv_play_btn_feed_item.setOnClickListener {
                    this.video_view_feed_item.visibility = View.VISIBLE
                    val params = this.video_view_feed_item.layoutParams as ConstraintLayout.LayoutParams
                    params.height = this.iv_feed_item_photo.height
                    this.video_view_feed_item.layoutParams = params
                    this.iv_feed_item_photo.visibility = View.INVISIBLE

                    player?.playWhenReady = true
                }

                this.iv_fullsize_feed_item.setOnClickListener {
                    videoUrl?.let { Caller.callFullSizePlayer(this, it) }
                }
            }

            else -> {}
        }
    }

    private fun getUserProfile(id: String) {
        viewModel.setParametersForPerson(
            Parameters(
                id,
                -1,
                LOAD_PERSON,
                BOUND_FROM_BACKEND
            )
        )
    }

    private fun setUserProfileObserver() {
        viewModel.person.observe(this, Observer { resource ->
            when (resource?.getStatus()) {
                Status.SUCCESS -> {
                    resource.getData()?.let { person ->
                        searper = person as? Person?
                        searper?.let {
                            displayUserInfo(it)
                            setBottomLoadingFinished()
                        }
                    }

                    resource.getMessage()?.let { showNetworkError(resource) }
                }

                Status.LOADING -> { }

                Status.ERROR -> { showNetworkError(resource) }

                else -> { showNetworkError(resource) }
            }
        })
    }

    private fun displayUserInfo(user: Person) {
        val name = user.realname?._content ?: user.username?._content
        this.tv_username_feed_item.text = if (name == "") "unknown user" else name

        searperPhotoUrl = workHandler.getProfilePhotoURL(user.iconfarm, user.iconserver, user.id)

        if (user.iconserver == "0" || searperPhotoUrl == null) {
            this.iv_profile_feed_item.setImageDrawable(getDrawable(R.drawable.ic_default_profile))
        } else {
            setImageDraw(this.iv_profile_feed_item, searperPhotoUrl!!)
        }

        this.iv_profile_feed_item.setOnClickListener {
            Caller.callOtherUserProfile(this, CALLED_FROM_FEED_INFO, userId, name!!, 3, searperPhotoUrl!!)
        }
    }

    private fun getPhotoEXIF(photoId: String) {
        viewModel.setParametersForEXIF(
            Parameters(
                photoId,
                -1,
                LOAD_EXIF,
                BOUND_FROM_BACKEND
            )
        )
    }

    private fun setPhotoEXIFObserver() {
        viewModel.exif.observe(this, Observer { resource ->
            this.folding_cell_feed_item.visibility = View.GONE
            this.cell_portion_information_container.visibility = View.GONE
            this.cell_photo_info_container.visibility = View.GONE

            when(resource?.getStatus()) {
                Status.SUCCESS -> {

                    @Suppress("UNCHECKED_CAST")
                    val exifs = resource.getData() as? List<EXIF>?

                    exifs?.let {
                        if (it.size > CHECK_EXIF_LIST_SIZE) {
                            exifList = it
                            showEXIF(it)
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
    }

    private fun showEXIF(listEXIF: List<EXIF>) {
        val result = addEXIF(listEXIF)
        setFoldingCellInvisible()

        if (exifHandler.getEXIFNoneItemCount() < CHECK_EXIF_LIST_SIZE) { animateEXIF(result) }
        exifHandler.resetEXIFNoneItemCount()
    }

    private fun animateEXIF(listEXIF: List<EXIF>) {
        this.folding_cell_feed_item.visibility = View.VISIBLE
        setEXIFInfo(listEXIF)
        this.folding_cell_feed_item.fold(true)

        if (!this.cell_portion_information_container.isShown) {
            this.cell_portion_information_container.visibility = View.VISIBLE
        }

        this.folding_cell_feed_item.setOnClickListener {
            this.folding_cell_feed_item.toggle(true)

            if (this.folding_cell_feed_item.isUnfolded) {
                withDelay(60L) { this.nested_scroll_view_feed_item.fullScroll(View.FOCUS_DOWN) }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setEXIFInfo(items: List<EXIF>) {
        val none = "None"
        val existedAperture: Boolean

        val model = (items[CommonWorkHandler.EXIF_ITEM_INDEX_MAKE].raw._content + " "
            + items[CommonWorkHandler.EXIF_ITEM_INDEX_MODEL].raw._content)

        this.tv_cam_model.text = model
        this.tv_exposure.text = none
        this.tv_exposure.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_EXPOSURE].raw._content
        this.tv_aperture.text = none

        if (items[CommonWorkHandler.EXIF_ITEM_INDEX_APERTURE].label == "Aperture") {
            existedAperture = true
            this.tv_aperture.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_APERTURE].raw._content
        } else {
            existedAperture = false
        }

        if (existedAperture) {
            this.tv_iso.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_ISO_SPEED].raw._content
            this.tv_flash.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_FLASH].raw._content
            this.tv_white_balance.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_WHITE_BALANCE].raw._content
            if (items[CommonWorkHandler.EXIF_ITEM_INDEX_FOCAL_LENGTH].label == "Focal Length") {
                this.tv_focal_length.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_FOCAL_LENGTH].raw._content
            }
        } else {
            this.tv_iso.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_APERTURE].raw._content
            this.tv_flash.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_ISO_SPEED].raw._content
            this.tv_white_balance.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_FLASH].raw._content
            this.tv_focal_length.text = none

        }
    }

    private fun addEXIF(exifs: List<EXIF>): ArrayList<EXIF> {
        val list = ArrayList<EXIF>()
        val model = exifHandler.getEXIFByLabel(exifs, "Model")
            ?: exifHandler.putManualEXIF("Model")
        list.add(model)
        val make = exifHandler.getEXIFByLabel(exifs, "Make")
            ?: exifHandler.putManualEXIF("Make")
        list.add(make)
        val exifExposure = exifHandler.getEXIFByLabel(exifs, "Exposure")
            ?: exifHandler.putManualEXIF("Exposure")
        list.add(exifExposure)
        val exifAperture = exifHandler.getEXIFByLabel(exifs, "Aperture")
            ?: exifHandler.putManualEXIF("Aperture")
        list.add(exifAperture)
        val exifISO = exifHandler.getEXIFByLabel(exifs, "ISO Speed")
            ?: exifHandler.putManualEXIF("ISO Speed")
        list.add(exifISO)
        val exifFlash = exifHandler.getEXIFByLabel(exifs, "Flash")
            ?: exifHandler.putManualEXIF("Flash")
        list.add(exifFlash)
        val exifWhiteBalance = exifHandler.getEXIFByLabel(exifs, "White Balance")
            ?: exifHandler.putManualEXIF("White Balance")
        list.add(exifWhiteBalance)
        val exifFocalLength = exifHandler.getEXIFByLabel(exifs, "Focal Length")
            ?: exifHandler.putManualEXIF("Focal Length")
        list.add(exifFocalLength)

        return list
    }

    private fun initializePlayer(source: String) {
        this.progress_bar_feed_item.visibility = View.VISIBLE
        this.iv_play_btn_feed_item.visibility = View.GONE
        val trackSelector = DefaultTrackSelector()
        trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSizeSd())

        player = ExoPlayerFactory.newSimpleInstance(this)
        player?.addListener(playBackStateListener)
        this.video_view_feed_item.player = player

        val uri = Uri.parse(source)
        val mediaSource = buildMediaSource(uri)

        player?.playWhenReady = false
        player?.seekTo(currentWindow, playbackPosition)
        player?.prepare(mediaSource, false, false)
        player?.volume = 0f
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory = DefaultDataSourceFactory(this, "sample_test")

        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
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

    private fun selectSizesButtonClickListener() {
        this.cb_feed_size_s.setOnClickListener { setCheckBoxState(CONTENT_SIZE_S) }
        this.cb_feed_size_m.setOnClickListener { setCheckBoxState(CONTENT_SIZE_M) }
        this.cb_feed_size_l.setOnClickListener { setCheckBoxState(CONTENT_SIZE_L) }
    }

    @MockData
    private fun setLicenseRandomCheck() {
        this.cb_feed_item_license1.isChecked = Random.nextBoolean()
        this.cb_feed_item_license2.isChecked = Random.nextBoolean()
        this.cb_feed_item_license3.isChecked = Random.nextBoolean()
        this.cb_feed_item_license4.isChecked = Random.nextBoolean()
        this.cb_feed_item_license5.isChecked = Random.nextBoolean()
        this.cb_feed_item_license1.isEnabled = false; this.cb_feed_item_license2.isEnabled = false
        this.cb_feed_item_license3.isEnabled = false; this.cb_feed_item_license4.isEnabled = false
        this.cb_feed_item_license5.isEnabled = false
    }

    private fun setCheckBoxState(selectedSize: Int) {
        this.cb_feed_size_s.isChecked = false
        this.cb_feed_size_m.isChecked = false
        this.cb_feed_size_l.isChecked = false
        this.selectedContentSize = selectedSize

        when (selectedSize) {
            CONTENT_SIZE_S -> this.cb_feed_size_s.isChecked = true
            CONTENT_SIZE_M -> this.cb_feed_size_m.isChecked = true
            CONTENT_SIZE_L -> this.cb_feed_size_l.isChecked = true
        }
    }

    private fun setFoldingCellInvisible() {
        if (!folding_cell_feed_item.isShown) {
            folding_cell_feed_item.visibility = View.GONE
            cell_portion_information_container.visibility = View.GONE
        }
    }

    private fun setBottomOnLoading() {
        this.progress_bar_bottom.visibility = View.VISIBLE
        this.cardview_holder_bottom_feed_item.visibility = View.GONE
        this.folding_cell_feed_item.visibility = View.GONE
    }

    private fun setBottomLoadingFinished() {
        this.progress_bar_bottom.visibility = View.GONE
        this.cardview_holder_bottom_feed_item.visibility = View.VISIBLE
        this.folding_cell_feed_item.visibility = View.VISIBLE
    }

    private fun removeCache() = launchIOWork {
        viewModel.removePerson()
        viewModel.removeEXIF()
    }

    private fun initCoordinatorLayout() {
        this.coordinator_feed_item_layout.setOnSwipeOutListener(this, object : OnSwipeOutListener {
            override fun onSwipeLeft(x: Float, y: Float) {
                finishAfterTransition()
            }

            override fun onSwipeRight(x: Float, y: Float) {
                finishAfterTransition()
            }

            override fun onSwipeDown(x: Float, y: Float) {
                if (isScrollOnTop) finishAfterTransition()
            }

            override fun onSwipeUp(x: Float, y: Float) {
            }

            override fun onSwipeDone() {
            }
        })

        this.nested_scroll_view_feed_item.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener {
                v, scrollX, scrollY, oldScrollX, oldScrollY ->
            isScrollOnTop = scrollY == 0
        })
    }

    private fun setViewBind() {
        sharedElementCallback = FeedInfoItemCallback()

        this.iv_feed_item_photo.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + feedPosition
        sharedElementCallback.setViewBinding(this.iv_feed_item_photo)
    }

    private fun networkStatusVisible(isVisible: Boolean) = if (isVisible) {
        this.disconnect_container_pinned.visibility = View.GONE
        this.appbar_layout_feed_item.visibility = View.VISIBLE
        this.nested_scroll_view_feed_item.visibility = View.VISIBLE
    } else {
        this.disconnect_container_pinned.visibility = View.VISIBLE
        this.appbar_layout_feed_item.visibility = View.GONE
        this.nested_scroll_view_feed_item.visibility = View.GONE
    }

    private fun showNetworkError(resource: Resource) = when(resource.errorCode) {
        in 400..499 -> {
            Snackbar.make(this.coordinator_feed_item_layout,
                getString(R.string.phrase_client_wrong_request),
                Snackbar.LENGTH_LONG
            ).show()
        }

        in 500..599 -> {
            Snackbar.make(this.coordinator_feed_item_layout,
                getString(R.string.phrase_server_wrong_response),
                Snackbar.LENGTH_LONG
            ).show()
        }

        else -> {
            Snackbar.make(this.coordinator_feed_item_layout, resource.getMessage().toString(),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun setActivityResult(calledFrom: Int) = when (calledFrom) {
        CALLED_FROM_FEED -> {
            val intent = Intent(this, HomeActivity::class.java)

            intent.putExtra(Caller.EXTRA_FEED_INFO_POSITION, feedPosition)
            setResult(Caller.SELECTED_FEED_ITEM_POSITION, intent)
        }

        CALLED_FROM_HOME_BEST_PICK_HOT_PHOTO -> {
            val intent = Intent(this, HomeActivity::class.java)

            intent.putExtra(Caller.EXTRA_FEED_INFO_POSITION, feedPosition)
            setResult(Caller.SELECTED_BEST_PICK_HOT_PHOTO_POSITION, intent)
        }

        CALLED_FROM_HOME_BEST_PICK_SEARPER_PHOTO -> {
            val intent = Intent(this, HomeActivity::class.java)

            intent.putExtra(Caller.EXTRA_FEED_INFO_POSITION, feedPosition)
            setResult(Caller.SELECTED_BEST_PICK_SEARPER_PHOTO_POSITION, intent)
        }
        else -> {}
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

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setScreenOrientationStable() {
        val currentOrientation = resources.configuration.orientation
        if (currentOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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
                    this@FeedItemActivity.iv_play_btn_feed_item.visibility = View.VISIBLE
                    this@FeedItemActivity.progress_bar_feed_item.visibility = View.GONE
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
