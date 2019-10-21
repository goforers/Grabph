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
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.transition.Transition
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.SharedElementCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import com.elmargomez.typer.Font
import com.elmargomez.typer.Typer
import com.goforer.base.domain.common.GeneralFunctions
import com.goforer.base.presentation.utils.CommonUtils.convertTime
import com.goforer.base.presentation.utils.CommonUtils.showToastMessage
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.base.presentation.view.customs.listener.OnSwipeOutListener
import com.goforer.grabph.R
import com.goforer.grabph.domain.usecase.save.PhotoSaver
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_PHOTO_INFO
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
import com.goforer.grabph.presentation.common.view.SlidingDrawer
import com.goforer.grabph.presentation.ui.feed.common.SavePhoto
import com.goforer.grabph.presentation.ui.feed.photoinfo.sharedelementcallback.PhotoInfoItemCallback
import com.goforer.grabph.presentation.ui.photog.PhotogPhotoActivity
import com.goforer.grabph.presentation.ui.photoviewer.sharedelementcallback.PhotoViewerCallback
import com.goforer.grabph.presentation.vm.BaseViewModel.Companion.NONE_TYPE
import com.goforer.grabph.presentation.vm.feed.exif.EXIFViewModel
import com.goforer.grabph.presentation.vm.feed.exif.LocalEXIFViewModel
import com.goforer.grabph.presentation.vm.feed.location.LocalLocationViewModel
import com.goforer.grabph.presentation.vm.feed.location.LocationViewModel
import com.goforer.grabph.presentation.vm.people.person.PersonViewModel
import com.goforer.grabph.presentation.vm.feed.photo.LocalSavedPhotoViewModel
import com.goforer.grabph.presentation.vm.feed.photo.PhotoInfoViewModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.exif.EXIF
import com.goforer.grabph.data.datasource.model.cache.data.entity.location.Location
import com.goforer.grabph.data.datasource.model.cache.data.entity.photoinfo.Picture
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.Person
import com.goforer.grabph.data.datasource.network.response.Status
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_EXIF
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_GEO
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_PHOTO_INFO
import com.goforer.grabph.data.repository.remote.Repository.Companion.BOUND_FROM_BACKEND
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource.Companion.LOAD_PERSON
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import kotlinx.android.synthetic.main.activity_photo_info.*
import kotlinx.android.synthetic.main.cell_photo_info.*
import kotlinx.android.synthetic.main.cell_portion_photo_info.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.Long.parseLong
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class PhotoInfoActivity : BaseActivity() {
    private lateinit var sharedElementCallback: PhotoInfoItemCallback

    internal lateinit var photoId: String

    private lateinit var ownerId: String

    private var title: String? = null
    private var description: String? = null

    private var photoPosition: Int = 0
    private var offsetChange: Int = 0

    private var exifList: List<EXIF>? = null

    private var location: Location? = null

    private var searper: Person? = null

    private lateinit var slidingDrawer: SlidingDrawer

    private var isAppBarLayoutExpanded = false
    private var isAppBarLayoutCollapsed = false

    private val job = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + job)
    private val ioScope = CoroutineScope(Dispatchers.IO + job)

    @field:Inject
    lateinit var searperProfileViewModel: PersonViewModel
    @field:Inject
    lateinit var localEXIFViewModel: LocalEXIFViewModel
    @field:Inject
    lateinit var localLocationViewModel: LocalLocationViewModel
    @field:Inject
    lateinit var exifViewModel: EXIFViewModel
    @field:Inject
    lateinit var locationViewModel: LocationViewModel
    @field:Inject
    lateinit var photoInfoViewModel: PhotoInfoViewModel
    @field:Inject
    lateinit var localSavedPhotoViewModel: LocalSavedPhotoViewModel

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
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isNetworkAvailable) {
            offsetChange = 0
            networkStatusVisible(true)
            savedInstanceState?.let {
                photoId = savedInstanceState.getString(EXTRA_PHOTO_ID, "")
                ownerId = savedInstanceState.getString(EXTRA_OWNER_ID, "")
                photoPosition = savedInstanceState.getInt(EXTRA_PHOTO_INFO_SELECTED_ITEM_POSITION, 0)
            }

            val font = Typer.set(this.applicationContext).getFont(Font.ROBOTO_MEDIUM)
            this@PhotoInfoActivity.collapsing_layout.setCollapsedTitleTypeface(font)
            this@PhotoInfoActivity.collapsing_layout.setExpandedTitleTypeface(font)

            this@PhotoInfoActivity.appbar_layout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener {
                appBarLayout, verticalOffset ->
                this@PhotoInfoActivity.collapsing_layout.title = title

                when {
                    abs(verticalOffset) == appBarLayout.totalScrollRange -> {
                        isAppBarLayoutCollapsed = true
                        isAppBarLayoutExpanded = false
                    }
                    verticalOffset == 0 -> {
                        isAppBarLayoutExpanded = true
                        launchUIWork {
                            delay(DELAY_COLLAPSED_TIMER_INTERVAL.toLong())
                            isAppBarLayoutCollapsed = false
                        }
                    }
                    else -> {
                        isAppBarLayoutExpanded = false
                        isAppBarLayoutCollapsed = true
                    }
                }
            })

            this@PhotoInfoActivity.coordinator_photo_info_layout.setOnSwipeOutListener(this, object : OnSwipeOutListener {
                override fun onSwipeLeft(x: Float, y: Float) {
                    Timber.d("onSwipeLeft")

                    finishAfterTransition()
                }

                override fun onSwipeRight(x: Float, y: Float) {
                    Timber.d("onSwipeRight")

                }

                override fun onSwipeDown(x: Float, y: Float) {
                    Timber.d( "onSwipeDown")

                    if (!isAppBarLayoutCollapsed && isAppBarLayoutExpanded) {
                        finishAfterTransition()
                    }
                }

                override fun onSwipeUp(x: Float, y: Float) {
                    Timber.d("onSwipeUp")
                }

                override fun onSwipeDone() {
                    Timber.d("onSwipeDone")
                }
            })
        } else {
            networkStatusVisible(false)
        }
    }

    override fun setContentView() {
        setContentView(R.layout.activity_photo_info)
    }

    override fun setActionBar() {
        setSupportActionBar(this@PhotoInfoActivity.toolbar)
        val actionBar= supportActionBar
        actionBar?.let {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_USE_LOGO
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
        }

        this@PhotoInfoActivity.toolbar?.setNavigationOnClickListener{
            finishAfterTransition()
        }

        this@PhotoInfoActivity.toolbar.hideOverflowMenu()
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
        removePhotoCache(CACHE_SEARPLER_PROFILE_TYPE)
        searperProfileViewModel.loadType = LOAD_PERSON
        searperProfileViewModel.boundType = BOUND_FROM_BACKEND

        // The cache should be removed whenever App is started again and then
        // the data are fetched from the Back-end.
        // The Cache has to be light-
        removePhotoCache(CACHE_PHOTOG_INFO_TYPE)
        photoInfoViewModel.loadType = LOAD_PHOTO_INFO
        photoInfoViewModel.boundType = BOUND_FROM_BACKEND

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

        getData()
        window.sharedElementEnterTransition.addListener(sharedEnterListener)
        supportPostponeEnterTransition()
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)

        if (resultCode == SELECTED_PHOTO_INFO_PHOTO_VIEW) {
            // Listener to reset shared element exit transition callbacks.
            window.sharedElementExitTransition.addListener(sharedExitListener)
            supportPostponeEnterTransition()

            val callback = PhotoViewerCallback()

            callback.setViewBinding(this@PhotoInfoActivity.iv_photo_info_photo)
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
                            callShareToFacebook(this@PhotoInfoActivity.iv_photo_info_photo.drawable as BitmapDrawable,
                                    this.applicationContext.getString(R.string.phrase_title) + "\n\n"
                                            + this@PhotoInfoActivity.tv_photo_info_title.text + "\n\n"
                                            + this.applicationContext.getString(R.string.phrase_description) + "\n\n"
                                            + description)
                        R.id.menu_share_ect -> {
                            waterMarkHandler.putWatermark(this.applicationContext, workHandler,
                                    (iv_photo_info_photo.drawable as BitmapDrawable).bitmap, title, description)
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

    override fun finishAfterTransition() {
        this@PhotoInfoActivity.fab_like.hide()
        (this@PhotoInfoActivity.fab_like as View).visibility = View.GONE
        this@PhotoInfoActivity.collapsing_layout.title = ""
        supportPostponeEnterTransition()
        setViewBind()
        setActivityResult()

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

        var outStateBundle = outState

        outStateBundle.putString(EXTRA_PHOTO_ID, photoId)
        outStateBundle.putString(EXTRA_OWNER_ID, ownerId)
        outStateBundle.putInt(EXTRA_PHOTO_INFO_SELECTED_ITEM_POSITION, photoPosition)

        slidingDrawer.searperProfileDrawerForFeedViewDrawer?.let {
            outStateBundle = slidingDrawer.searperProfileDrawerForFeedViewDrawer?.saveInstanceState(outStateBundle)!!
            outStateBundle = slidingDrawer.drawerHeader?.saveInstanceState(outStateBundle)!!
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        photoId = savedInstanceState.getString(EXTRA_PHOTO_ID, "")
        ownerId = savedInstanceState.getString(EXTRA_OWNER_ID, "")
        photoPosition = savedInstanceState.getInt(EXTRA_PHOTO_INFO_SELECTED_ITEM_POSITION, 0)
    }

    override fun onBackPressed() {
        setActivityResult()

        super.onBackPressed()
    }

    private fun networkStatusVisible(isVisible: Boolean) = if (isVisible) {
        this@PhotoInfoActivity.disconnect_container_photo_info.visibility = View.GONE
        this@PhotoInfoActivity.iv_disconnect_photo_info.visibility = View.GONE
        this@PhotoInfoActivity.tv_notice1_photo_info.visibility = View.GONE
        this@PhotoInfoActivity.tv_notice2_photo_info.visibility = View.GONE
        this@PhotoInfoActivity.appbar_layout.visibility = View.VISIBLE
        this@PhotoInfoActivity.scroll_view.visibility = View.VISIBLE
    } else {
        this@PhotoInfoActivity.disconnect_container_photo_info.visibility = View.VISIBLE
        this@PhotoInfoActivity.iv_disconnect_photo_info.visibility = View.VISIBLE
        this@PhotoInfoActivity.tv_notice1_photo_info.visibility = View.VISIBLE
        this@PhotoInfoActivity.tv_notice2_photo_info.visibility = View.VISIBLE
        this@PhotoInfoActivity.appbar_layout.visibility = View.GONE
        this@PhotoInfoActivity.scroll_view.visibility = View.GONE
    }

    private fun getIntentData() {
        photoId = intent.getStringExtra(EXTRA_PHOTO_ID)
        ownerId = intent.getStringExtra(EXTRA_OWNER_ID)
        photoPosition = intent.getIntExtra(EXTRA_PHOTO_INFO_SELECTED_ITEM_POSITION, 0)
    }

    private fun getData() {
        getPhotoInfo(photoId)
        setPhotoInfo()
        getSearplerProfile(ownerId)
        setSearplerProfileObserver()
        getPhotoEXIF(photoId)
        setPhotoEXIFObserver()
        getPhotoLocation(photoId)
        setPhotoLocationObserver()
    }

    private fun setActivityResult() {
        val intent = Intent(this, PhotogPhotoActivity::class.java)

        intent.putExtra(EXTRA_PHOTO_INFO_SELECTED_ITEM_POSITION, photoPosition)
        setResult(SELECTED_PHOTO_INFO_ITEM_POSITION, intent)
    }

    private fun setViewBind() {
        sharedElementCallback = PhotoInfoItemCallback(intent)
        this@PhotoInfoActivity.iv_photo_info_photo.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + photoPosition
        this@PhotoInfoActivity.tv_photo_info_title.transitionName = TransitionObject.TRANSITION_NAME_FOR_TITLE + photoPosition

        sharedElementCallback.setViewBinding(this@PhotoInfoActivity.iv_photo_info_photo, this@PhotoInfoActivity.tv_photo_info_title)
    }

    private fun  getSearplerProfile(id: String) {
        searperProfileViewModel.setParameters(
            Parameters(
                id,
                -1,
                LOAD_PERSON,
                BOUND_FROM_BACKEND
            ), NONE_TYPE)
    }

    private fun setSearplerProfileObserver() = searperProfileViewModel.person.observe(this, Observer { resource ->
        when(resource?.getStatus()) {
            Status.SUCCESS -> {
                resource.getData()?.let { person ->
                    searper = person as? Person?
                    displaySearplerInfo(searper!!)
                    slidingDrawer.setHeaderBackground(GeneralFunctions.getHeaderBackgroundUrl())
                    slidingDrawer.setSearperProfileDrawer(searper,
                            SlidingDrawer.PROFILE_SEARPER_TYPE_FROM_FEED_VIEWER)
                }

                resource.getMessage()?.let {
                    showNetworkError(resource.errorCode)
                }
            }

            Status.LOADING -> {
            }

            Status.ERROR -> {
                showNetworkError(resource.errorCode)
            }

            else -> {
                showNetworkError(resource.errorCode)
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

    private fun setPhotoEXIFObserver() = exifViewModel.exif.observe(this, Observer { resource ->
        folding_exif_cell.visibility = View.GONE
        cell_portion_information_container.visibility = View.GONE
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
                    showNetworkError(resource.errorCode)
                }
            }

            Status.LOADING -> {
            }

            Status.ERROR -> {
                showNetworkError(resource.errorCode)
            }

            else -> {
                showNetworkError(resource.errorCode)
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
        location_container.visibility = View.GONE
        when(resource?.getStatus()) {
            Status.SUCCESS -> {
                val location = resource.getData() as? Location?

                location?.let {
                    this.location = location
                    displayLocation(location)
                }

                resource.getMessage()?.let {
                    showNetworkError(resource.errorCode)
                }
            }

            Status.LOADING -> {
            }

            Status.ERROR -> {
                showNetworkError(resource.errorCode)
            }

            else -> {
                showNetworkError(resource.errorCode)
            }
        }
    })

    private fun getPhotoInfo(photoId: String) {
        photoInfoViewModel.setParameters(
            Parameters(
                photoId,
                -1,
                LOAD_PHOTO_INFO,
                BOUND_FROM_BACKEND
            ), NONE_TYPE)
    }

    private fun setPhotoInfo() = photoInfoViewModel.photoInfo.observe(this, Observer { resource ->
            when(resource?.getStatus()) {
                Status.SUCCESS -> {
                    val picture = resource.getData() as? Picture?

                    picture?.let {
                        uiScope.launch {
                            if (photoId == picture.id) displayPhotoInfo(picture)
                        }
                    }

                    resource.getMessage()?.let {
                        showNetworkError(resource.errorCode)
                    }
                }

                Status.LOADING -> {
                }

                Status.ERROR -> {
                    showNetworkError(resource.errorCode)
                }

                else -> {
                    showNetworkError(resource.errorCode)
                }
            }
    })

    private fun showNetworkError(errorCode: Int) = when(errorCode) {
        in 400..499 -> {
            Snackbar.make(coordinator_photo_info_layout, getString(R.string.phrase_client_wrong_request), LENGTH_LONG).show()
        }

        in 500..599 -> {
            Snackbar.make(coordinator_photo_info_layout, getString(R.string.phrase_server_wrong_response), LENGTH_LONG).show()
        }

        else -> {}
    }

    private suspend fun displayPhotoInfo(picture: Picture) {
        val url = "https://farm" + picture.farm + ".staticflickr.com/" + picture.server +
                "/" + picture.id + "_" + picture.secret + ".jpg"

        val workPhoto = uiScope.async {
            loadPhoto(this@PhotoInfoActivity, url)
        }

        val workExtra = uiScope.async {
            fillExtraInfo(picture, ownerId)
        }

        workPhoto.await()
        workExtra.await()
    }

    private fun loadPhoto(activity: PhotoInfoActivity, url: String) {
        setFixedImageSize(0, 0)
        setImageDraw(this@PhotoInfoActivity.iv_photo_info_photo, this@PhotoInfoActivity.backdrop_container,
                     url, true)
        this@PhotoInfoActivity.iv_photo_info_photo.setOnClickListener {
            this@PhotoInfoActivity.iv_photo_info_photo.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + 0
            photo?.let { it1 ->
                Caller.callViewer(activity, this@PhotoInfoActivity.iv_photo_info_photo,
                        0, CALLED_FROM_PHOTO_INFO, it1, url, SELECTED_PHOTO_INFO_PHOTO_VIEW)
            }
        }

        this@PhotoInfoActivity.iv_photo_info_photo.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + photoPosition
        this@PhotoInfoActivity.tv_photo_info_title.transitionName = TransitionObject.TRANSITION_NAME_FOR_TITLE + photoPosition
        sharedElementCallback = PhotoInfoItemCallback(intent)
        sharedElementCallback.setViewBinding(this@PhotoInfoActivity.iv_photo_info_photo, this@PhotoInfoActivity.tv_photo_info_title)
        setEnterSharedElementCallback(sharedElementCallback)
        supportStartPostponedEnterTransition()
        this@PhotoInfoActivity.appbar_layout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener {
            _, verticalOffset ->
            if (this@PhotoInfoActivity.collapsing_layout.height + verticalOffset < 2
                    * ViewCompat.getMinimumHeight(this@PhotoInfoActivity.collapsing_layout)) {
                // collapsed
                this@PhotoInfoActivity.iv_photo_info_photo.animate().alpha(1.0f).duration = 600
            } else {
                // extended
                this@PhotoInfoActivity.iv_photo_info_photo.animate().alpha(1.0f).duration = 1000    // 1.0f means opaque
            }
        })
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun fillExtraInfo(picture: Picture, ownerId: String) {
        title = if (picture.title._content == " " || picture.title._content == "") {
            this.applicationContext.getString(R.string.no_title)
        } else {
            picture.title._content
        }

        this@PhotoInfoActivity.collapsing_layout.title = title

        this@PhotoInfoActivity.tv_views_text.text = picture.views

        this@PhotoInfoActivity.tv_comments_text.text = picture.comments._content
        this@PhotoInfoActivity.iv_comments_icon.setOnClickListener {
            callComment(picture.comments, picture.id)
        }

        this@PhotoInfoActivity.tv_comments_text.setOnClickListener {
            callComment(picture.comments, picture.id)
        }

        this@PhotoInfoActivity.tv_photo_info_title.text = if (title!!.isEmpty() || title == " ") {
            getString(R.string.no_title)
        } else {
            title
        }

        val fabIcon: Drawable = when (picture.like) {
            0 -> {
                ContextCompat.getDrawable(this.applicationContext, R.drawable.ic_fab_normal)!!
            }
            1 -> {
                ContextCompat.getDrawable(this.applicationContext, R.drawable.ic_fab_good)!!
            }
            2 -> {
                ContextCompat.getDrawable(this.applicationContext, R.drawable.ic_fab_bad)!!
            }
            else -> {
                ContextCompat.getDrawable(this.applicationContext, R.drawable.ic_fab_normal)!!
            }
        }

        this@PhotoInfoActivity.fab_like.setImageDrawable(fabIcon)
        this@PhotoInfoActivity.fab_like.backgroundTintList = when (picture.like) {
            0 -> {
                ColorStateList.valueOf(Color.WHITE)
            }
            1 -> {
                ColorStateList.valueOf(Color.RED)
            }
            2 -> {
                ColorStateList.valueOf(Color.BLUE)
            }
            else -> {
                ColorStateList.valueOf(Color.WHITE)
            }
        }

        this@PhotoInfoActivity.tv_date_taken.text = this.applicationContext.getString(R.string.phrase_taken) + " " + picture.dates.taken
        // Check it tomorrow
        val postedDate = parseLong(picture.dates.posted)

        description = picture.description._content
        showDescription(description)
        this@PhotoInfoActivity.tv_date_posted.text = (this.applicationContext.getString(R.string.phrase_posted) + " "
                + convertTime(postedDate))
        this@PhotoInfoActivity.ib_download.setOnClickListener { doDownload(ownerId, postedDate) }
        this@PhotoInfoActivity.ib_wallpaper.setOnClickListener {
            showToastMessage(this, this.applicationContext
                    .getString(R.string.phrase_wallpaper_implement), Toast.LENGTH_SHORT)
        }

        this@PhotoInfoActivity.fab_like.setOnClickListener { view -> doLike(view as FloatingActionButton, picture) }
        setTags(picture.tags)
    }

    private fun showDescription(description: String?) = this@PhotoInfoActivity.webview_description.loadDataWithBaseURL(null, description,
            "text/html; charset=UTF-8", null, null)

    private fun callComment(commentCount: Picture.CommentCount, id: String) {
        if (commentCount._content != "0") {
            Caller.callComment(this.applicationContext, id)
        }
    }

    @SuppressLint("CheckResult")
    fun displaySearplerInfo(searperInfo: Person) {
        val photoPath= workHandler.getProfilePhotoURL(searperInfo.iconfarm,
                searperInfo.iconserver, searperInfo.id)
        var name = searperInfo.realname?._content
        name = name ?: searperInfo.username?._content
        if (name == "") {
            this@PhotoInfoActivity.tv_name_text.text = searperInfo.username?._content
        } else {
            this@PhotoInfoActivity.tv_name_text.text = name
        }

        if (searperInfo.iconserver == "0") {
            this@PhotoInfoActivity.iv_searper_pic.setImageDrawable(this.applicationContext!!
                    .getDrawable(R.drawable.ic_default_profile))
            this@PhotoInfoActivity.iv_searper_pic.setOnClickListener{
                // slidingDrawer.searperProfileDrawerForFeedViewDrawer?.openDrawer()
                Caller.callOtherUserProfile(this, CALLED_FROM_PHOTO_INFO, ownerId, name!!, 3, photoPath)
            }
        } else {
            setImageDraw(this@PhotoInfoActivity.iv_searper_pic, photoPath)
            this@PhotoInfoActivity.iv_searper_pic.setOnClickListener{
                // slidingDrawer.searperProfileDrawerForFeedViewDrawer?.openDrawer()
                Caller.callOtherUserProfile(this, CALLED_FROM_PHOTO_INFO, ownerId, name!!, 3, photoPath)
            }
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

    private fun displayEXIF(listEXIF: List<EXIF>) = launchEXIF(listEXIF)

    private fun addEXIF(listEXIF: List<EXIF>): ArrayList<EXIF> {
        val usefulEXIF = ArrayList<EXIF>()
        val model = exifHandler.getEXIFByLabel(listEXIF, "Model")
                ?: exifHandler.putManualEXIF("Model")
        usefulEXIF.add(model)
        val make = exifHandler.getEXIFByLabel(listEXIF, "Make")
                ?: exifHandler.putManualEXIF("Make")
        usefulEXIF.add(make)
        val exifExposure = exifHandler.getEXIFByLabel(listEXIF, "Exposure")
                ?: exifHandler.putManualEXIF("Exposure")
        usefulEXIF.add(exifExposure)
        val exifAperture = exifHandler.getEXIFByLabel(listEXIF, "Aperture")
                ?: exifHandler.putManualEXIF("Aperture")
        usefulEXIF.add(exifAperture)
        val exifISO = exifHandler.getEXIFByLabel(listEXIF, "ISO Speed")
                ?: exifHandler.putManualEXIF("ISO Speed")
        usefulEXIF.add(exifISO)
        val exifFlash = exifHandler.getEXIFByLabel(listEXIF, "Flash")
                ?: exifHandler.putManualEXIF("Flash")
        usefulEXIF.add(exifFlash)
        val exifWhiteBalance = exifHandler.getEXIFByLabel(listEXIF, "White Balance")
                ?: exifHandler.putManualEXIF("White Balance")
        usefulEXIF.add(exifWhiteBalance)
        val exifFocalLength = exifHandler.getEXIFByLabel(listEXIF, "Focal Length")
                ?: exifHandler.putManualEXIF("Focal Length")
        usefulEXIF.add(exifFocalLength)

        return usefulEXIF
    }

    private fun displayLocation(location: Location) {
        setLocationInvisible()
        animateLocation(location)
    }

    private fun setFoldingCellInvisible() {
        if (!folding_exif_cell.isShown) {
            folding_exif_cell.visibility = View.GONE
            folding_exif_cell.visibility = View.GONE
            cell_portion_information_container.visibility = View.GONE
        }
    }

    private fun setLocationInvisible() {
        if (!location_container.isShown) {
            location_container.visibility = View.GONE
        }
    }

    private fun setTags(tags: Picture.Tags?) {
        tags?.tag ?: return

        val photoTags = StringBuilder()
        if (tags.tag!!.isNotEmpty()) {
            tags_holder.visibility = View.VISIBLE
            for (loop in 0 until tags.tag!!.size) {
                photoTags.append(tags.tag!![loop].authorname).append(" - ")
                        .append(tags.tag!![loop].raw).append(" ")
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                this@PhotoInfoActivity.tv_tags.text = Html.fromHtml(photoTags.toString(), Html.FROM_HTML_MODE_COMPACT)
            } else {
                this@PhotoInfoActivity.tv_tags.text = Html.fromHtml(photoTags.toString())
            }

            this@PhotoInfoActivity.tv_tags.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun doDownload(ownerId: String, publishedDate: Long?) {
        val savePhoto = SavePhoto(workHandler, saver)

        savePhoto.bitmapDrawable = BitmapDrawable(resources, photo)
        val photoURL = if (searper?.iconserver == "0") {
            "None"
        } else {
            workHandler.getProfilePhotoURL(searper?.iconfarm!!, searper?.iconserver!!, searper?.id!!)
        }

        savePhoto.bitmapDrawable = BitmapDrawable(resources, photo)
        savePhoto.fileName = String.format("%s.jpg", ownerId + "_" + publishedDate.toString())
        savePhoto.title = tv_photo_info_title.text.toString()
        savePhoto.exifItems = exifList as MutableList<EXIF>?
        savePhoto.location = location
        savePhoto.searperInfo = searper
        savePhoto.searperPhotoUrl = photoURL
        savePhoto.showSaveDialog(this, localEXIFViewModel, localLocationViewModel,
                localSavedPhotoViewModel, savePhoto)
    }

    private fun doLike(view: View, photoInfo: Picture) {
        val wrapper = ContextThemeWrapper(this, R.style.PopupMenu)
        val popup = PopupMenu(wrapper, view, Gravity.CENTER)

        popup.menuInflater.inflate(R.menu.menu_like_popup, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_like_good -> {
                    this@PhotoInfoActivity.fab_like.hide()
                    this@PhotoInfoActivity.fab_like.setImageResource(R.drawable.ic_fab_good)
                    this@PhotoInfoActivity.fab_like.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    photoInfo.like = 1
                    this@PhotoInfoActivity.fab_like.show()
                }
                R.id.menu_like_normal -> {
                    this@PhotoInfoActivity.fab_like.hide()
                    this@PhotoInfoActivity.fab_like.setImageResource(R.drawable.ic_fab_normal)
                    this@PhotoInfoActivity.fab_like.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                    photoInfo.like = 0
                    this@PhotoInfoActivity.fab_like.show()
                }
                R.id.menu_like_bad -> {
                    this@PhotoInfoActivity.fab_like.hide()
                    this@PhotoInfoActivity.fab_like.setImageResource(R.drawable.ic_fab_bad)
                    this@PhotoInfoActivity.fab_like.backgroundTintList = ColorStateList.valueOf(Color.BLUE)
                    photoInfo.like = 2
                    this@PhotoInfoActivity.fab_like.show()
                }
                else -> {
                }
            }

            true
        }

        MenuHandler().applyFontToMenuItem(popup, Typeface.createFromAsset(applicationContext?.assets, NOTO_SANS_KR_MEDIUM),
                resources.getColor(R.color.colorHomeQuestFavoriteKeyword, theme))

        val menuHelper: Any
        val argTypes: Array<Class<*>>
        try {
            val fMenuHelper= PopupMenu::class.java.getDeclaredField("mPopup")
            fMenuHelper.isAccessible = true
            menuHelper=fMenuHelper.get(popup)
            argTypes=arrayOf(Boolean::class.javaPrimitiveType!!)
            menuHelper.javaClass.getDeclaredMethod("setForceShowIcon", *argTypes).invoke(menuHelper, true)
        } catch (e: Exception) {
            // Possible exceptions are NoSuchMethodError and NoSuchFieldError
            //
            // In either case, an exception indicates something is wrong with the reflection code, or the
            // structure of the PopupMenu class or its dependencies has changed.
            //
            // These exceptions should never happen since we're shipping the AppCompat library in our own apk,
            // but in the case that they do, we simply can't force icons to display, so log the error and
            // show the menu normally.
        }

        popup.show()
    }

    private fun callShareToFacebook(drawable: BitmapDrawable, caption: String) {
        workHandler.shareToFacebook(drawable.bitmap, this)
    }

    private fun animateEXIF(lisEXIF: List<EXIF>) {
        this@PhotoInfoActivity.folding_exif_cell.visibility = View.VISIBLE
        if (!this@PhotoInfoActivity.folding_exif_cell.isUnfolded) {
            if (!this@PhotoInfoActivity.cell_portion_information_container.isShown) {
                this@PhotoInfoActivity.cell_portion_information_container.visibility = View.VISIBLE
                val animation = AnimationUtils.loadAnimation(
                        this.applicationContext, R.anim.scale_up_enter)
                animation.duration = 300
                this@PhotoInfoActivity.cell_portion_information_container.animation = animation
                this@PhotoInfoActivity.cell_portion_information_container.animate()
                animation.start()
            }

            this@PhotoInfoActivity.folding_exif_cell.setOnClickListener {
                setEXINFO(lisEXIF)
                this@PhotoInfoActivity.folding_exif_cell.toggle(false)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setEXINFO(items: List<EXIF>) {
        val none = "None"
        val existedAperture: Boolean

        val model = (items[CommonWorkHandler.EXIF_ITEM_INDEX_MAKE].raw._content + " "
                + items[CommonWorkHandler.EXIF_ITEM_INDEX_MODEL].raw._content)

        this@PhotoInfoActivity.tv_cam_model.text = model

        this@PhotoInfoActivity.tv_exposure.text = none
        this@PhotoInfoActivity.tv_exposure.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_EXPOSURE].raw._content

        this@PhotoInfoActivity.tv_aperture.text = none
        if (items[CommonWorkHandler.EXIF_ITEM_INDEX_APERTURE].label == "Aperture") {
            existedAperture = true
            this@PhotoInfoActivity.tv_aperture.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_APERTURE].raw._content
        } else {
            existedAperture = false
        }

        this@PhotoInfoActivity.tv_iso.text = none
        this@PhotoInfoActivity.tv_flash.text = none
        this@PhotoInfoActivity.tv_white_balance.text = none
        this@PhotoInfoActivity.tv_focal_length.text = none
        if (existedAperture) {
            this@PhotoInfoActivity.tv_iso.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_ISO_SPEED].raw._content
            this@PhotoInfoActivity.tv_flash.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_FLASH].raw._content
            this@PhotoInfoActivity.tv_white_balance.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_WHITE_BALANCE].raw._content
            if (items[CommonWorkHandler.EXIF_ITEM_INDEX_FOCAL_LENGTH].label == "Focal Length") {
                this@PhotoInfoActivity.tv_focal_length.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_FOCAL_LENGTH].raw._content
            }
        } else {
            this@PhotoInfoActivity.tv_iso.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_APERTURE].raw._content
            this@PhotoInfoActivity.tv_flash.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_ISO_SPEED].raw._content
            this@PhotoInfoActivity.tv_white_balance.text = items[CommonWorkHandler.EXIF_ITEM_INDEX_FLASH].raw._content
        }
    }

    private fun animateLocation(location: Location) {
        if (!this@PhotoInfoActivity.location_container.isShown) {
            this@PhotoInfoActivity.location_container.visibility = View.VISIBLE
            val animation = AnimationUtils.loadAnimation(
                    this.applicationContext, R.anim.scale_up_enter)
            animation.duration = 300
            this@PhotoInfoActivity.location_container.animation = animation
            this@PhotoInfoActivity.location_container.animate()
            animation.start()
        }

        this@PhotoInfoActivity.location_container.setOnClickListener {
            Caller.callViewMap(this.applicationContext,
                    this@PhotoInfoActivity.tv_photo_info_title.text.toString(),
                    java.lang.Double.parseDouble(location.latitude!!),
                    java.lang.Double.parseDouble(location.longitude!!),
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

    private fun launchEXIF(listEXIF: List<EXIF>) = launchUIWork {
        showEXIF(listEXIF)
    }

    private suspend fun showEXIF(listEXIF: List<EXIF>) = withContext(Dispatchers.Main) {
        var result: List<EXIF> = ArrayList()

        withContext(uiScope.coroutineContext) {
            result = addEXIF(listEXIF)
        }

        setFoldingCellInvisible()
        if (exifHandler.getEXIFNoneItemCount() < CHECK_EXIF_LIST_SIZE) {
            animateEXIF(result)
        }

        exifHandler.resetEXIFNoneItemCount()
    }

    private fun removePhotoCache(type: Int) = launchIOWork {
        when(type) {
            CACHE_SEARPLER_PROFILE_TYPE -> {
                searperProfileViewModel.removePerson()
            }

            CACHE_PHOTO_EXIF_TYPE -> {
                exifViewModel.removeEXIF()
            }

            CACHE_PHOTO_LOCATION_TYPE -> {
                locationViewModel.removeLocation()
            }

            CACHE_PHOTOG_INFO_TYPE -> {
                photoInfoViewModel.removePhotoInfo()
            }
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
}