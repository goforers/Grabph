/*
 * Copyright (C) 2 018 Lukoh Nam, goForer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goforer.grabph.presentation.ui.feed.photoinfo

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.transition.Transition
import android.view.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.SharedElementCallback
import androidx.lifecycle.Observer
import com.goforer.base.annotation.MockData
import com.goforer.base.presentation.utils.CommonUtils
import com.goforer.base.presentation.utils.CommonUtils.getLocation
import com.goforer.base.presentation.utils.CommonUtils.withDelay
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.base.presentation.view.customs.listener.OnSwipeOutListener
import com.goforer.grabph.R
import com.goforer.grabph.domain.usecase.save.PhotoSaver
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.presentation.caller.Caller.EXTRA_OWNER_ID
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PHOTO_ID
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PHOTO_INFO_SELECTED_ITEM_POSITION
import com.goforer.grabph.presentation.caller.Caller.SELECTED_PHOTO_INFO_ITEM_POSITION
import com.goforer.grabph.presentation.caller.Caller.SELECTED_PHOTO_INFO_PHOTO_VIEW
import com.goforer.grabph.presentation.common.effect.transition.TransitionCallback
import com.goforer.grabph.presentation.common.effect.transition.TransitionObject
import com.goforer.grabph.presentation.common.menu.MenuHandler
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import com.goforer.grabph.presentation.common.utils.handler.exif.EXIFHandler
import com.goforer.grabph.presentation.common.utils.handler.watermark.WatermarkHandler
import com.goforer.grabph.presentation.ui.photog.PhotogPhotoActivity
import com.goforer.grabph.presentation.ui.photoviewer.sharedelementcallback.PhotoViewerCallback
import com.goforer.grabph.presentation.vm.BaseViewModel.Companion.NONE_TYPE
import com.goforer.grabph.presentation.vm.feed.photo.PhotoInfoViewModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.exif.EXIF
import com.goforer.grabph.data.datasource.model.cache.data.entity.location.Location
import com.goforer.grabph.data.datasource.model.cache.data.entity.photoinfo.Picture
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.LocalPin
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.Person
import com.goforer.grabph.data.datasource.network.response.Status
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_EXIF
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_GEO
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_PHOTO_INFO
import com.goforer.grabph.data.repository.remote.Repository.Companion.BOUND_FROM_BACKEND
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_PERSON
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_HOME_PROFILE_MY_PIN
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_PHOTO_INFO
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PHOTO_INFO_CALLED_FROM
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PHOTO_PATH
import com.goforer.grabph.presentation.ui.feed.feedinfo.sharedelementcallback.FeedInfoItemCallback
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import kotlinx.android.synthetic.main.activity_photo_info.*
import kotlinx.android.synthetic.main.activity_photo_info.backdrop_container
import kotlinx.android.synthetic.main.activity_photo_info.collapsing_layout
import kotlinx.android.synthetic.main.activity_photo_info.folding_cell_photo_item
import kotlinx.android.synthetic.main.activity_photo_info.progress_bar_bottom
import kotlinx.android.synthetic.main.cell_photo_info.*
import kotlinx.android.synthetic.main.cell_portion_photo_info.*
import kotlinx.android.synthetic.main.layout_disconnection.*
import kotlinx.android.synthetic.main.layout_selection_content_size.*
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.abs
import kotlin.random.Random

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class PhotoInfoActivity : BaseActivity() {
    @MockData val loggedId = "184804690@N02"

    private lateinit var sharedElementCallback: FeedInfoItemCallback
    private lateinit var playBackStateListener: PlayBackStateListener

    internal lateinit var photoId: String
    private lateinit var userId: String
    private lateinit var photoPath: String

    private var title: String? = null
    private var description: String? = null

    private var photoPosition: Int = 0
    private var offsetChange: Int = 0
    private var calledFrom: Int = 0

    private var exifList: List<EXIF>? = null
    private var location: Location? = null
    private var searper: Person? = null
    private var mediaType: String? = null
    private var videoUrl: String? = null
    private var userPhotoUrl: String? = null

    private var isPinned = false
    private var isPinClicked = false

    private var isAppBarLayoutExpanded = false
    private var isAppBarLayoutCollapsed = false

    private var player: SimpleExoPlayer? = null
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var selectedContentSize = 0

    private lateinit var behavior: AppBarLayout.Behavior
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var params: CoordinatorLayout.LayoutParams

    private val job = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + job)
    private val ioScope = CoroutineScope(Dispatchers.IO + job)

    @field:Inject
    lateinit var viewModel: PhotoInfoViewModel
    @field:Inject
    lateinit var workHandler: CommonWorkHandler
    @field:Inject
    lateinit var waterMarkHandler: WatermarkHandler
    @field:Inject
    lateinit var exifHandler: EXIFHandler
    @field:Inject
    lateinit var saver: PhotoSaver

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
        private const val CHECK_EXIF_LIST_SIZE = 6
        private const val DELAY_COLLAPSED_TIMER_INTERVAL = 300
        private const val OFFSET_CHANGE_COUNT = 20

        private const val CACHE_SEARPLER_PROFILE_TYPE = 0
        private const val CACHE_PHOTO_EXIF_TYPE = 1
        private const val CACHE_PHOTO_LOCATION_TYPE = 2
        private const val CACHE_PHOTOG_INFO_TYPE = 3

        private const val CONTENT_SIZE_S = 1
        private const val CONTENT_SIZE_M = 2
        private const val CONTENT_SIZE_L = 3
    }

    override fun setContentView() { setContentView(R.layout.activity_photo_info) }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isNetworkAvailable) {
            offsetChange = 0
            networkStatusVisible(true)
        } else {
            networkStatusVisible(false)
        }
    }

    override fun setViews(savedInstanceState: Bundle?) {
        window.sharedElementEnterTransition.addListener(sharedEnterListener)
        supportPostponeEnterTransition()
        playBackStateListener = PlayBackStateListener()

        Timber.plant(Timber.DebugTree())

        getIntentData()
        selectSizesButtonClickListener()
        removePhotoCache()
        getData()
        supportPostponeEnterTransition()
        initCoordinatorLayout()
        setLicenseCheckRandom()
    }

    override fun setActionBar() {
        setSupportActionBar(this.toolbar_photo_info)
        val actionBar= supportActionBar

        actionBar?.run {
            setHomeAsUpIndicator(R.drawable.ic_back)
            displayOptions = ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_USE_LOGO
            setDisplayShowTitleEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
        this.toolbar_photo_info.setNavigationOnClickListener{ finishAfterTransition() }
        this.toolbar_photo_info.hideOverflowMenu()
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)

        if (resultCode == SELECTED_PHOTO_INFO_PHOTO_VIEW) {
            // Listener to reset shared element exit transition callbacks.
            window.sharedElementExitTransition.addListener(sharedExitListener)
            supportPostponeEnterTransition()

            val callback = PhotoViewerCallback()
            callback.setViewBinding(this.iv_photo_info_photo)
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

                    true
                }

                sharePopup.show()

                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun finishAfterTransition() {
        this@PhotoInfoActivity.collapsing_layout.title = ""
        supportPostponeEnterTransition()
        setViewBind()
        setActivityResult()

        this.video_view_photo_info.visibility = View.GONE
        this.iv_photo_info_photo.visibility = View.VISIBLE
        this.iv_fullsize_photo_info.visibility = View.GONE

        super.finishAfterTransition()
    }

    @ExperimentalCoroutinesApi
    override fun onDestroy() {
        super.onDestroy()

        uiScope.cancel()
        ioScope.cancel()
        job.cancel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EXTRA_PHOTO_ID, photoId)
        outState.putString(EXTRA_OWNER_ID, userId)
        outState.putInt(EXTRA_PHOTO_INFO_SELECTED_ITEM_POSITION, photoPosition)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        photoId = savedInstanceState.getString(EXTRA_PHOTO_ID, "")
        userId = savedInstanceState.getString(EXTRA_OWNER_ID, "")
        photoPosition = savedInstanceState.getInt(EXTRA_PHOTO_INFO_SELECTED_ITEM_POSITION, 0)
    }

    override fun onBackPressed() {
        setActivityResult()
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        if (mediaType == getString(R.string.media_type_video) && Build.VERSION.SDK_INT < 24) {
            releasePlayer()
        }

        if (isPinClicked) {
            if (isPinned) {
                viewModel.savePin(LocalPin(photoId, userId, loggedId, photoPath, mediaType!!))
            } else {
                viewModel.deletePin(photoId)
            }
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

    private fun getIntentData() {
        photoId = intent.getStringExtra(EXTRA_PHOTO_ID)
        userId = intent.getStringExtra(EXTRA_OWNER_ID)
        photoPosition = intent.getIntExtra(EXTRA_PHOTO_INFO_SELECTED_ITEM_POSITION, 0)
        calledFrom = intent.getIntExtra(EXTRA_PHOTO_INFO_CALLED_FROM, -1)
        photoPath = intent.getStringExtra(EXTRA_PHOTO_PATH)
    }

    private fun getData() {
        setFixedImageSize(0, 0)
        setImageDraw(this.iv_photo_info_photo, this.backdrop_container, photoPath, true)

        withDelay(50L) {
            setViewBind()
            setEnterSharedElementCallback(sharedElementCallback)
            supportStartPostponedEnterTransition()
        }

        setBottomOnLoading()
        getPhotoInfo(photoId)
        setPhotoInfoObserver()
        getUserProfile(userId)
        setUserProfileObserver()
        getPhotoEXIF(photoId)
        setPhotoEXIFObserver()
        getPhotoLocation(photoId)
        setPhotoLocation()
        setPinView()
    }

    private fun getPhotoInfo(photoId: String) {
        viewModel.setParameters(
            Parameters(
                photoId,
                -1,
                LOAD_PHOTO_INFO,
                BOUND_FROM_BACKEND
            ), NONE_TYPE)
    }

    private fun setPhotoInfoObserver() = viewModel.photoInfo.observe(this, Observer { resource ->
        when(resource?.getStatus()) {
            Status.SUCCESS -> {
                val picture = resource.getData() as? Picture?

                picture?.let {
                    uiScope.launch {
                        if (picture.id == photoId) {
                            displayPhotoInfo(picture)
                            setBottomLoadingFinished()
                        }
                    }
                }

                resource.getMessage()?.let {
                    showNetworkError(resource)
                }
            }

            Status.LOADING -> {}

            Status.ERROR -> {
                showNetworkError(resource)
            }

            else -> {
                showNetworkError(resource)
            }
        }
    })

    private fun displayPhotoInfo(picture: Picture) {
        setPhotoInfo(picture)

        this.tv_photo_info_title.text = picture.title._content
        this.tv_description_photo_info.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(picture.description._content, Html.FROM_HTML_MODE_LEGACY)
        } else {
            picture.description._content
        }

        val url = CommonUtils.getFlickrPhotoUrlForViewer(picture.server, picture.id, picture.secret!!)

        this.iv_photo_info_photo.setOnClickListener {
            this.iv_photo_info_photo.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + 0
            Caller.callViewer(
                this,
                this.iv_photo_info_photo,
                0,
                CALLED_FROM_PHOTO_INFO,
                if (picture.media == getString(R.string.media_type_video)) photoPath else url,
                SELECTED_PHOTO_INFO_ITEM_POSITION,
                picture.title._content
            )
        }
    }

    private fun setPhotoInfo(picture: Picture) {
        this.mediaType = picture.media

        when (mediaType) {
            getString(R.string.media_type_photo) -> {
                this.iv_play_btn_photo_info.visibility = View.GONE
                this.video_view_photo_info.visibility = View.GONE
                this.iv_fullsize_photo_info.visibility = View.GONE
                this.iv_photo_info_photo.visibility = View.VISIBLE
            }

            getString(R.string.media_type_video) -> {
                getVideoSource(picture.id)

                this.video_view_photo_info.visibility = View.GONE
                this.iv_photo_info_photo.visibility = View.VISIBLE

                this.iv_play_btn_photo_info.setOnClickListener {
                    this.video_view_photo_info.visibility = View.VISIBLE
                    val params = this.video_view_photo_info.layoutParams as ConstraintLayout.LayoutParams
                    params.height = this.iv_photo_info_photo.height
                    this.video_view_photo_info.layoutParams = params
                    this.iv_photo_info_photo.visibility = View.INVISIBLE

                    player?.playWhenReady = true
                }

                this.iv_fullsize_photo_info.setOnClickListener {
                    videoUrl?.let { Caller.callFullSizePlayer(this, it) }
                }
            }

            else -> {}
        }
    }

    private fun getVideoSource(photoId: String) {
        viewModel.getPhotoSizes(photoId) // to get video source url

        viewModel.videoThumbnail.observe(this, Observer {
            photoPath = it
            setImageDraw(this.iv_photo_info_photo, this.backdrop_container, it, true)
        })

        viewModel.videoSource.observe(this, Observer {
            this.videoUrl = it
            initializePlayer(it)
        })

        viewModel.getSizeError.observe(this, Observer {
            Snackbar.make(coordinator_photo_info_layout, it, LENGTH_LONG).show()
        })
    }

    private fun  getUserProfile(id: String) {
        viewModel.setParametersForPerson(
            Parameters(
                id,
                -1,
                LOAD_PERSON,
                BOUND_FROM_BACKEND
            )
        )
    }

    private fun setUserProfileObserver() = viewModel.person.observe(this, Observer { resource ->
        when(resource?.getStatus()) {
            Status.SUCCESS -> {
                resource.getData()?.let { person ->
                    searper = person as? Person?
                    searper?.let { displayUserInfo(it) }
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

    private fun displayUserInfo(user: Person) {
        val name = user.realname?._content?.let {
            if (it.isEmpty()) user.username?._content else it
        } ?: user.username?._content
        this.tv_username_photo_info.text = name

        userPhotoUrl = workHandler.getProfilePhotoURL(user.iconfarm, user.iconserver, user.id)

        if (user.iconserver == "0" || userPhotoUrl == null) {
            this.iv_profile_photo_info.setImageDrawable(getDrawable(R.drawable.ic_default_profile))
        } else {
            setImageDraw(this.iv_profile_photo_info, userPhotoUrl!!)
        }

        /* Blocked not to open the same user's profile activity */
        if (calledFrom == CALLED_FROM_HOME_PROFILE_MY_PIN) {
            this.iv_profile_photo_info.setOnClickListener {
                Caller.callOtherUserProfile(this, CALLED_FROM_PHOTO_INFO, userId, name!!, 1, userPhotoUrl!!)
            }
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
        viewModel.exifInfo.observe(this, Observer { resource ->
            this.folding_cell_photo_item.visibility = View.GONE
            this.cell_portion_information_container.visibility = View.GONE
            this.cell_photo_info_container.visibility = View.GONE

            when(resource?.getStatus()) {
                Status.SUCCESS -> {
                    @Suppress("UNCHECKED_CAST")
                    val exifs = resource.getData() as? List<EXIF>?

                    exifs?.let {
                        if (exifs.size > CHECK_EXIF_LIST_SIZE) {
                            exifList = exifs
                            displayEXIF(exifs)
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

    private fun displayEXIF(listEXIF: List<EXIF>) {
        val result = addEXIF(listEXIF)
        setFoldingCellInvisible()

        if (exifHandler.getEXIFNoneItemCount() < CHECK_EXIF_LIST_SIZE) { animateEXIF(result) }
        exifHandler.resetEXIFNoneItemCount()
    }

    private fun animateEXIF(listEXIF: List<EXIF>) {
        this.folding_cell_photo_item.visibility = View.VISIBLE
        setEXIFInfo(listEXIF)
        this.folding_cell_photo_item.fold(true)

        if (!this.cell_portion_information_container.isShown) {
            this.cell_portion_information_container.visibility = View.VISIBLE
        }

        this.folding_cell_photo_item.setOnClickListener {
            this.folding_cell_photo_item.toggle(true)

            if (this.folding_cell_photo_item.isUnfolded) {
                withDelay(60L) { this.nested_scroll_view_photo_info.fullScroll(View.FOCUS_DOWN)}
            }
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

    private fun setPinView() {
        viewModel.checkPinStatus(loggedId, photoId)
        viewModel.isPinned.observe(this, Observer {
            this.iv_icon_bookmark_photo_info.isChecked = it
            isPinned = it
        })

        this.iv_icon_bookmark_photo_info.setOnClickListener {
            isPinned = this.iv_icon_bookmark_photo_info.isChecked
            isPinClicked = true
        }
    }

    private fun getPhotoLocation(photoId: String) {
        viewModel.setParametersForLocation(
            Parameters(
                photoId,
                -1,
                LOAD_GEO,
                BOUND_FROM_BACKEND
            )
        )
    }

    private fun setPhotoLocation() = viewModel.location.observe(this, Observer { resource ->

        when (resource?.getStatus()) {
            Status.SUCCESS -> {
                resource.getData()?.let { location ->
                    this.location = location as? Location?
                    this.location?.let { displayLocation(it) }
                }

                resource.getMessage()?.let { showNetworkError(resource) }
            }

            Status.LOADING -> {  }

            Status.ERROR -> { showNetworkError(resource) }

            else -> { showNetworkError(resource) }
        }
    })

    private fun displayLocation(location: Location) {
        this.tv_location_content.text = getLocation(location)
        this.divider0.visibility = View.VISIBLE
        this.constraint_holder_location.visibility = View.VISIBLE
    }


    private fun initializePlayer(source: String) {
        this.progress_bar_play_button.visibility = View.VISIBLE
        this.iv_play_btn_photo_info.visibility = View.GONE
        val trackSelector = DefaultTrackSelector()
        trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSizeSd())

        player = ExoPlayerFactory.newSimpleInstance(this)
        player?.addListener(playBackStateListener)
        this.video_view_photo_info.player = player

        val uri = Uri.parse(source)
        val mediaSource = buildMediaSource(uri)

        player?.playWhenReady = false
        player?.seekTo(currentWindow, playbackPosition)
        player?.prepare(mediaSource, false, false)
        player?.volume = 0f
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory = DefaultDataSourceFactory(this, "searp")

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

    private fun setFoldingCellInvisible() {
        if (!folding_cell_photo_item.isShown) {
            this.folding_cell_photo_item.visibility = View.GONE
            this.cell_portion_information_container.visibility = View.GONE
        }
    }

    private fun setBottomOnLoading() {
        this.progress_bar_bottom.visibility = View.VISIBLE
        this.cardview_holder_bottom_photo_info.visibility = View.GONE
        this.folding_cell_photo_item.visibility = View.GONE
    }

    private fun setBottomLoadingFinished() {
        this.progress_bar_bottom.visibility = View.GONE
        this.cardview_holder_bottom_photo_info.visibility = View.VISIBLE
        this.folding_cell_photo_item.visibility = View.VISIBLE
    }

    private fun removePhotoCache() = launchIOWork {
        viewModel.removePerson()
        viewModel.removeEXIF()
        viewModel.removePhotoInfo()
        viewModel.removeLocation()
    }

    private fun setActivityResult() {
        val intent = Intent(this, PhotogPhotoActivity::class.java)

        intent.putExtra(EXTRA_PHOTO_INFO_SELECTED_ITEM_POSITION, photoPosition)
        setResult(SELECTED_PHOTO_INFO_ITEM_POSITION, intent)
    }

    private fun initCoordinatorLayout() {
        setCoordinatorBehavior()
        setScrollBehavior()
        setCollapsingBehavior()
    }

    private fun setCoordinatorBehavior() {
        this.coordinator_photo_info_layout.setOnSwipeOutListener(this, object : OnSwipeOutListener {
            override fun onSwipeLeft(x: Float, y: Float) {
                finishAfterTransition()
            }

            override fun onSwipeRight(x: Float, y: Float) {
                finishAfterTransition()
            }

            override fun onSwipeDown(x: Float, y: Float) {
                if (!isAppBarLayoutCollapsed && isAppBarLayoutExpanded) {
                    finishAfterTransition()
                }

                if (isAppBarLayoutCollapsed && !isAppBarLayoutExpanded) {
                    // this@PhotoInfoActivity.appbar_layout_photo_info.setExpanded(false, true)
                }
            }

            override fun onSwipeUp(x: Float, y: Float) {
            }

            override fun onSwipeDone() {
            }
        })
    }

    private fun setScrollBehavior() {

    }

    private fun setCollapsingBehavior() {
        var currentOffset: Int
        var alpha: Int
        var offSetPercentage: Float

        this.appbar_layout_photo_info.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener {
                appBarLayout, verticalOffset ->
            currentOffset = abs(verticalOffset)
            offSetPercentage = (currentOffset.toFloat() / appBarLayout.totalScrollRange.toFloat())
            alpha = (255 * offSetPercentage).toInt()

            this.tv_photo_info_title.setTextColor(this.tv_photo_info_title.textColors.withAlpha(alpha))
            this.tv_photo_info_title.alpha = alpha.toFloat()

            when {
                abs(verticalOffset) == appBarLayout.totalScrollRange -> {
                    isAppBarLayoutExpanded = false
                    isAppBarLayoutCollapsed = true
                }

                verticalOffset == 0 -> {
                    isAppBarLayoutExpanded = true
                    isAppBarLayoutCollapsed = false
                }

                else -> {
                    isAppBarLayoutExpanded = false
                    isAppBarLayoutCollapsed = true
                }
            }
        })
    }

    private fun selectSizesButtonClickListener() {
        this.cb_feed_size_s.setOnClickListener { setCheckBoxState(CONTENT_SIZE_S) }
        this.cb_feed_size_m.setOnClickListener { setCheckBoxState(CONTENT_SIZE_M) }
        this.cb_feed_size_l.setOnClickListener { setCheckBoxState(CONTENT_SIZE_L) }
    }

    @MockData
    private fun setLicenseCheckRandom() {
        this.cb_photo_info_license1.isChecked = Random.nextBoolean()
        this.cb_photo_info_license2.isChecked = Random.nextBoolean()
        this.cb_photo_info_license3.isChecked = Random.nextBoolean()
        this.cb_photo_info_license4.isChecked = Random.nextBoolean()
        this.cb_photo_info_license5.isChecked = Random.nextBoolean()
        this.cb_photo_info_license1.isEnabled = false
        this.cb_photo_info_license2.isEnabled = false
        this.cb_photo_info_license3.isEnabled = false
        this.cb_photo_info_license4.isEnabled = false
        this.cb_photo_info_license5.isEnabled = false
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

    private fun enableAppBarDraggable(draggable: Boolean) {
        behavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() { // block dragging behavior on appBarLayout
            override fun canDrag(p0: AppBarLayout): Boolean { return draggable }
        })
    }

    private fun setViewBind() {
        sharedElementCallback = FeedInfoItemCallback()
        this.iv_photo_info_photo.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + photoPosition
        sharedElementCallback.setViewBinding(this.iv_photo_info_photo)
    }

    private fun networkStatusVisible(isVisible: Boolean) = if (isVisible) {
        this.disconnect_container_pinned.visibility = View.GONE
        this.appbar_layout_photo_info.visibility = View.VISIBLE
        this.nested_scroll_view_photo_info.visibility = View.VISIBLE
    } else {
        this.disconnect_container_pinned.visibility = View.VISIBLE
        this.appbar_layout_photo_info.visibility = View.GONE
        this.nested_scroll_view_photo_info.visibility = View.GONE
    }

    private fun showNetworkError(resource: Resource) = when(resource.errorCode) {
        in 400..499 -> {
            Snackbar.make(this.coordinator_photo_info_layout, getString(R.string.phrase_client_wrong_request), LENGTH_LONG).show()
        }

        in 500..599 -> {
            Snackbar.make(this.coordinator_photo_info_layout, getString(R.string.phrase_server_wrong_response), LENGTH_LONG).show()
        }

        else -> {
            Snackbar.make(this.coordinator_photo_info_layout, resource.getMessage().toString(), LENGTH_LONG).show()
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

            val stateString: String = when (playbackState) {
                ExoPlayer.STATE_IDLE -> {
                    setViewWhenTrouble()
                    "ExoPlayer.STATE_IDLE      -"
                }
                ExoPlayer.STATE_BUFFERING -> {
                    "ExoPlayer.STATE_BUFFERING -"
                }
                ExoPlayer.STATE_READY -> {
                    setViewWhenReady()
                    "ExoPlayer.STATE_READY     -"
                }
                ExoPlayer.STATE_ENDED -> {
                    "ExoPlayer.STATE_ENDED     -"
                }
                else -> {
                    "UNKNOWN_STATE             -"
                }
            }

            Timber.d("changed state to " + stateString
                + " playWhenReady: " + playWhenReady)
        }

        private fun setViewWhenReady() {
            this@PhotoInfoActivity.iv_play_btn_photo_info.visibility = View.VISIBLE
            this@PhotoInfoActivity.progress_bar_play_button.visibility = View.GONE
            this@PhotoInfoActivity.iv_fullsize_photo_info.visibility = View.VISIBLE
        }

        private fun setViewWhenTrouble() {
            this@PhotoInfoActivity.progress_bar_play_button.visibility = View.GONE
            Snackbar.make(this@PhotoInfoActivity.coordinator_photo_info_layout,
                "Sorry, something's wrong with the video source..",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
}