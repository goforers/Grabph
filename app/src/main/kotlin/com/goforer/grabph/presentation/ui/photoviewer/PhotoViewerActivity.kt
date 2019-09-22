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

@file:Suppress("DEPRECATION", "NAME_SHADOWING")

package com.goforer.grabph.presentation.ui.photoviewer

import android.annotation.SuppressLint
import androidx.lifecycle.Observer
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.Bundle
import android.transition.Transition
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.SharedElementCallback
import com.goforer.base.domain.common.GeneralFunctions
import com.goforer.base.presentation.model.event.ActivityStackClearEvent
import com.goforer.base.presentation.utils.CommonUtils.getImagePath
import com.goforer.base.presentation.utils.CommonUtils.showToastMessage
import com.goforer.base.presentation.utils.CommonUtils.withDelay
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.grabph.R
import com.goforer.grabph.domain.erase.PhotoEraser
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_SEARPLE_GALLERY_PHOTO
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_FEED_INFO
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_PHOTOG_PHOTO
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_PHOTO_INFO
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PHOTO_FILE_ID_LIST
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PHOTO_FILE_NAME_LIST
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PHOTO_POSITION
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PHOTO_URL
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PHOTO_VIEWER_CALLED_FROM
import com.goforer.grabph.presentation.caller.Caller.EXTRA_SAVED_PHOTO_ERASED
import com.goforer.grabph.presentation.caller.Caller.EXTRA_SEARPLE_GALLERY_PHOTO_POSITION
import com.goforer.grabph.presentation.caller.Caller.EXTRA_SELECTED_ITEM_POSITION
import com.goforer.grabph.presentation.caller.Caller.SELECTED_FEED_INFO_PHOTO_VIEW
import com.goforer.grabph.presentation.caller.Caller.SELECTED_PHOTOG_PHOTO_VIEW
import com.goforer.grabph.presentation.caller.Caller.SELECTED_PHOTO_INFO_PHOTO_VIEW
import com.goforer.grabph.presentation.caller.Caller.SELECTED_SEARPLE_GALLERY_ITEM_POSITION
import com.goforer.grabph.presentation.common.effect.transition.TransitionCallback
import com.goforer.grabph.presentation.common.effect.transition.TransitionObject
import com.goforer.grabph.presentation.common.menu.MenuHandler
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import com.goforer.grabph.presentation.common.utils.handler.watermark.WatermarkHandler
import com.goforer.grabph.presentation.common.view.SlidingDrawer
import com.goforer.grabph.presentation.ui.feed.feedinfo.FeedInfoActivity
import com.goforer.grabph.presentation.ui.feed.photoinfo.PhotoInfoActivity
import com.goforer.grabph.presentation.vm.feed.exif.LocalEXIFViewModel
import com.goforer.grabph.presentation.vm.feed.location.LocalLocationViewModel
import com.goforer.grabph.presentation.vm.feed.photo.LocalSavedPhotoViewModel
import com.goforer.grabph.presentation.vm.people.person.PersonViewModel
import com.goforer.grabph.presentation.ui.photog.PhotogPhotoActivity
import com.goforer.grabph.presentation.ui.photoviewer.fragment.PhotoViewerFragment
import com.goforer.grabph.presentation.ui.photoviewer.sharedelementcallback.SearpleGalleryPhotoCallback
import com.goforer.grabph.presentation.ui.searplegallery.SearpleGalleryActivity
import com.goforer.grabph.repository.model.cache.data.entity.exif.LocalEXIF
import com.goforer.grabph.repository.model.cache.data.entity.location.LocalLocation
import com.goforer.grabph.repository.model.cache.data.entity.profile.Person
import com.goforer.grabph.repository.network.resource.NetworkBoundResource
import com.goforer.grabph.repository.network.response.Resource
import com.goforer.grabph.repository.network.response.Status
import com.goforer.grabph.repository.interactor.remote.people.person.PersonRepository
import com.goforer.grabph.repository.interactor.remote.Repository
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_photo_viewer.*
import kotlinx.android.synthetic.main.view_searple_gallery_photo.*
import kotlinx.coroutines.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.ArrayList
import javax.inject.Inject

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@SuppressLint("Registered")
class PhotoViewerActivity: BaseActivity() {
    internal lateinit var sharedElementCallback: SharedElementCallback

    private var imagePosition: Int = 0
    private var initialPosition: Int = 0

    private val job = Job()

    private val ioScope = CoroutineScope(Dispatchers.IO + job)

    private var localEXIF: LocalEXIF? = null

    private var localLocation: LocalLocation? = null

    private var menuEXIFItem: MenuItem? = null
    private var menuLocationItem: MenuItem? = null

    private var isCalledMapActivity: Boolean = false
    private var isErasedPhoto: Boolean = false

    private lateinit var photoIdList: List<String>

    internal var calledFrom: Int = 0

    internal lateinit var photoUrl: String

    internal lateinit var photoPathList: List<String>

    internal lateinit var bitmap: Bitmap

    internal lateinit var slidingDrawer: SlidingDrawer

    internal lateinit var user: Person

    @field:Inject
    lateinit var localEXIFViewModel: LocalEXIFViewModel
    @field:Inject
    lateinit var localLocationViewModel: LocalLocationViewModel
    @field:Inject
    lateinit var localSavedPhotoViewModel: LocalSavedPhotoViewModel
    @field:Inject
    lateinit var searperProfileViewModel: PersonViewModel

    @field:Inject
    lateinit var workHandler: CommonWorkHandler

    @field:Inject
    lateinit var waterMarkHandler: WatermarkHandler

    @field:Inject
    lateinit var eraser: PhotoEraser

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

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isNetworkAvailable) {
            networkStatusVisible(true)
            savedInstanceState ?: getIntentData()
            savedInstanceState?.let {
                if (calledFrom == CALLED_FROM_SEARPLE_GALLERY_PHOTO) {
                    imagePosition = savedInstanceState.getInt(EXTRA_SEARPLE_GALLERY_PHOTO_POSITION, 0)
                    photoPathList = savedInstanceState.getStringArrayList(EXTRA_PHOTO_FILE_NAME_LIST) as List<String>
                    photoIdList = savedInstanceState.getStringArrayList(EXTRA_PHOTO_FILE_ID_LIST) as List<String>
                } else {
                    imagePosition = savedInstanceState.getInt(EXTRA_SELECTED_ITEM_POSITION, 0)
                }
            }

            transactFragment(PhotoViewerFragment::class.java, R.id.disconnect_container_photo_viewer, false)
        } else {
            networkStatusVisible(false)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()

        if (!isCalledMapActivity) {
            isCalledMapActivity = false
        }
    }

    @ExperimentalCoroutinesApi
    override fun onDestroy() {
        super.onDestroy()

        ioScope.cancel()
        job.cancel()
    }

    override fun setContentView() {
        setContentView(R.layout.activity_photo_viewer)
    }

    override fun setActionBar() {
        setSupportActionBar(this@PhotoViewerActivity.toolbar)
        val actionBar= supportActionBar
        actionBar?.let {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_USE_LOGO
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
        }

        this@PhotoViewerActivity.toolbar?.setNavigationOnClickListener{
            finishAfterTransition()
        }

        this@PhotoViewerActivity.toolbar.hideOverflowMenu()
    }

    override fun setViews(savedInstanceState: Bundle?) {
        window.sharedElementEnterTransition.addListener(sharedEnterListener)
        supportPostponeEnterTransition()

        slidingDrawer = SlidingDrawer.SlidingDrawerBuilder()
                .setActivity(this)
                .setRootView(R.id.coordinator_layout)
                .setBundle(savedInstanceState)
                .setType(SlidingDrawer.DRAWER_SEARPER_PROFILE_TYPE)
                .setWorkHandler(workHandler)
                .build()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_photo_viewer, menu)
        menuEXIFItem = menu.getItem(1)
        menuLocationItem = menu.getItem(2)
        when (calledFrom) {
            CALLED_FROM_SEARPLE_GALLERY_PHOTO -> {
                menu.getItem(1).isVisible = localEXIF != null
                menu.getItem(2).isVisible = localLocation != null
            }
            else -> {
                menu.getItem(0).isVisible = false
                menu.getItem(1).isVisible = false
                menu.getItem(2).isVisible = false
                menu.getItem(3).isVisible = false
            }
        }

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
            R.id.action_show_location -> {
                isCalledMapActivity = true
                Caller.callViewMap(this, localLocation?.title!!,
                                    java.lang.Double.parseDouble(localLocation?.latitude!!),
                                    java.lang.Double.parseDouble(localLocation?.longitude!!),
                                    localLocation?.address!!)

                return true
            }
            R.id.action_show_exif -> {
                val wrapper = ContextThemeWrapper(this, R.style.PopupMenu)
                val exifPopup = PopupMenu(wrapper, findViewById(R.id.action_show_exif), Gravity.CENTER)

                exifPopup.menuInflater.inflate(R.menu.menu_exif_popup, exifPopup.menu)
                MenuHandler().applyFontToMenuItem(exifPopup, Typeface.createFromAsset(applicationContext?.assets, NOTO_SANS_KR_MEDIUM),
                        resources.getColor(R.color.colorHomeQuestFavoriteKeyword, theme))
                exifPopup.menu.getItem(0).title = localEXIF?.model
                exifPopup.menu.getItem(1).title = localEXIF?.aperture
                exifPopup.menu.getItem(2).title = localEXIF?.exposure
                exifPopup.menu.getItem(3).title = localEXIF?.iso
                exifPopup.menu.getItem(4).title = localEXIF?.flash
                exifPopup.menu.getItem(5).title = localEXIF?.whitebalance
                exifPopup.menu.getItem(6).title = localEXIF?.focallength

                val menuHelper: Any
                val argTypes: Array<Class<*>>

                try {
                    val fMenuHelper= PopupMenu::class.java.getDeclaredField("mPopup")
                    fMenuHelper.isAccessible = true
                    menuHelper=fMenuHelper.get(exifPopup)
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

                exifPopup.show()

                return true
            }
            R.id.action_share -> {
                val wrapper = ContextThemeWrapper(this, R.style.PopupMenu)
                val sharePopup = PopupMenu(wrapper, findViewById(R.id.action_share), Gravity.CENTER)

                sharePopup.menuInflater.inflate(R.menu.menu_share_popup, sharePopup.menu)
                MenuHandler().applyFontToMenuItem(sharePopup, Typeface.createFromAsset(applicationContext?.assets, NOTO_SANS_KR_MEDIUM),
                        resources.getColor(R.color.colorHomeQuestFavoriteKeyword, theme))
                sharePopup.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_share_facebook ->
                            workHandler.shareToFacebook(BitmapFactory.decodeFile(
                                            getImagePath(this) + File.separator
                                                    + photoPathList[imagePosition]), this)
                        R.id.menu_share_ect -> {
                            waterMarkHandler.putWatermark(this.applicationContext, workHandler,
                                                    BitmapFactory.decodeFile(
                                            getImagePath(this) + File.separator
                                                    + photoPathList[imagePosition]),
                                    getString(R.string.menu_share), getString(R.string.phrase_view_photo))
                        }
                        else -> {
                        }
                    }

                    true
                }

                sharePopup.show()

                return true
            }
            R.id.action_delete -> {
                showDeleteDialog(getString(R.string.phrase_delete),
                        R.drawable.ic_dialog_delete, imagePosition, photoPathList[imagePosition])

                return true
            }
            android.R.id.home -> {
                onBackPressed()

                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var outState = outState

        outState.putInt(EXTRA_SELECTED_ITEM_POSITION, imagePosition)
        outState.putStringArrayList(EXTRA_PHOTO_FILE_NAME_LIST, photoPathList as ArrayList<String>)
        outState.putStringArrayList(EXTRA_PHOTO_FILE_ID_LIST, photoIdList as ArrayList<String>)
        //add the values which need to be saved from the drawer to the bundle
        slidingDrawer.searperProfileDrawerForDownloadViewDrawer?.let {
            outState = slidingDrawer.searperProfileDrawerForDownloadViewDrawer!!.saveInstanceState(outState)
            outState = slidingDrawer.drawerHeader!!.saveInstanceState(outState)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        savedInstanceState ?: return
        imagePosition = savedInstanceState.getInt(EXTRA_SELECTED_ITEM_POSITION, 0)
        photoPathList = savedInstanceState.getStringArrayList(EXTRA_PHOTO_FILE_NAME_LIST) as List<String>
        photoIdList = savedInstanceState.getStringArrayList(EXTRA_PHOTO_FILE_ID_LIST) as List<String>
    }

    override fun finishAfterTransition() {
        supportPostponeEnterTransition()
        if (calledFrom == CALLED_FROM_SEARPLE_GALLERY_PHOTO) {
            setViewBind(imagePosition)
        }

        setActivityResult()

        super.finishAfterTransition()
    }

    private fun getIntentData() {
        calledFrom = intent.getIntExtra(EXTRA_PHOTO_VIEWER_CALLED_FROM, -1)
        if (calledFrom == CALLED_FROM_SEARPLE_GALLERY_PHOTO) {
            photoPathList = intent.getStringArrayListExtra(EXTRA_PHOTO_FILE_NAME_LIST)
            photoIdList = intent.getStringArrayListExtra(EXTRA_PHOTO_FILE_ID_LIST)
            initialPosition = intent.getIntExtra(EXTRA_SEARPLE_GALLERY_PHOTO_POSITION, -1)
            imagePosition = initialPosition
        } else {
            photoUrl = intent.getStringExtra(EXTRA_PHOTO_URL)
            initialPosition = intent.getIntExtra(EXTRA_PHOTO_POSITION, -1)
            imagePosition = initialPosition
        }
    }

    private fun networkStatusVisible(isVisible: Boolean) {
        if (isVisible) {
            this@PhotoViewerActivity.iv_disconnect_photo_viewer.visibility = View.GONE
            this@PhotoViewerActivity.tv_notice1_photo_viewer.visibility = View.GONE
            this@PhotoViewerActivity.tv_notice2_photo_viewer.visibility = View.GONE
            this@PhotoViewerActivity.appbar_layout_photo_viewer.visibility = View.VISIBLE
        } else {
            this@PhotoViewerActivity.iv_disconnect_photo_viewer.visibility = View.VISIBLE
            this@PhotoViewerActivity.tv_notice1_photo_viewer.visibility = View.VISIBLE
            this@PhotoViewerActivity.tv_notice2_photo_viewer.visibility = View.VISIBLE
            this@PhotoViewerActivity.appbar_layout_photo_viewer.visibility = View.GONE
        }
    }

    private fun getSearperInfo(position: Int) {
        val isExist= booleanArrayOf(false)
        val path = StringBuilder(photoPathList[position])

        localSavedPhotoViewModel.setFileName(path.substring(path.lastIndexOf("/") + 1, path.length))
        localSavedPhotoViewModel.photo.observe(this, Observer { photo ->
            photo?.let {
                isExist[0] = true
                val adapter = (getFragment(PhotoViewerFragment::class.java) as PhotoViewerFragment).adapter
                adapter.launchUserInfo(adapter, photo, position)
            }

            isExist[0]=false
        })
    }

    private fun getSearperProfile(id: String) {
        removePersonCache(searperProfileViewModel.interactor)
        searperProfileViewModel.loadType = NetworkBoundResource.LOAD_PERSON
        searperProfileViewModel.boundType = NetworkBoundResource.BOUND_FROM_BACKEND
        searperProfileViewModel.setSearperId(id)
        searperProfileViewModel.person.observe(this, Observer { resource ->
            when(resource?.getStatus()) {
                Status.SUCCESS -> {
                    resource.getData()?.let { person ->
                        user = person as Person
                        slidingDrawer.setHeaderBackground(GeneralFunctions.getHeaderBackgroundUrl())
                        slidingDrawer.setSearperProfileDrawer(
                                user, SlidingDrawer.PROFILE_SEARPER_TYPE_FROM_DOWNLOAD_VIEWER)
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
    }

    private fun showNetworkError(resource: Resource) = when(resource.errorCode) {
        in 400..499 -> {
            Snackbar.make(coordinator_photo_viewer_layout, getString(R.string.phrase_client_wrong_request), Snackbar.LENGTH_LONG).show()
        }

        in 500..599 -> {
            Snackbar.make(coordinator_photo_viewer_layout, getString(R.string.phrase_server_wrong_response), Snackbar.LENGTH_LONG).show()
        }

        else -> {
            Snackbar.make(coordinator_photo_viewer_layout, resource.getMessage().toString(), Snackbar.LENGTH_LONG).show()
        }
    }

    private fun getEXIFInfo(position: Int) {
        val path = StringBuilder(photoPathList[position])

        localEXIFViewModel.setFileName(path.substring(path.lastIndexOf("/") + 1, path.length))

        val liveData= localEXIFViewModel.exifInfo

        liveData.observe(this, Observer {
            it?.let { exif ->
                localEXIF = exif
                menuEXIFItem?.isVisible = true
            }

            it ?: exifInvisible()
        })

        menuEXIFItem?.isVisible = false
    }

    private fun getLocationInfo(position: Int) {
        val path = StringBuilder(photoPathList[position])

        localLocationViewModel.setFileName(path.substring(path.lastIndexOf("/") + 1, path.length))
        val liveData=localLocationViewModel.locationInfo
        liveData.observe(this, Observer {
            it?.let { location ->
                localLocation = location
                menuLocationItem?.isVisible = true
            }

            it ?: locationInvisible()


        })

        menuLocationItem?.isVisible = false
    }

    internal fun getCurrentImagePosition(): Int {
        return imagePosition
    }

    internal fun setCurrentImagePosition(position: Int) {
        imagePosition = position
    }

    private fun setViewBind(position: Int) {
        (pv_photo as AppCompatImageView).transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + position
        sharedElementCallback = SearpleGalleryPhotoCallback()
        (sharedElementCallback as SearpleGalleryPhotoCallback).setViewBinding(pv_photo as AppCompatImageView)
    }

    private fun removePersonCache(repository: Repository) = launchIOWork {
        (repository as PersonRepository).removePerson()
    }

    private fun exifInvisible() {
        menuEXIFItem?.isVisible = false
    }

    private fun locationInvisible() {
        menuLocationItem?.isVisible = false
    }

    private fun deleteLocalInfo(filename: String) {
        localEXIFViewModel.deleteEXIFInfo(filename)
        localLocationViewModel.deleteLocationInfo(filename)
        localSavedPhotoViewModel.deleteSavedPhotoInfo(filename)

    }

    private fun setActivityResult() = when(calledFrom) {
        CALLED_FROM_SEARPLE_GALLERY_PHOTO -> {
            val intent = Intent(this, SearpleGalleryActivity::class.java)
            intent.putExtra(EXTRA_SELECTED_ITEM_POSITION, imagePosition)
            intent.putExtra(EXTRA_SAVED_PHOTO_ERASED, isErasedPhoto)
            setResult(SELECTED_SEARPLE_GALLERY_ITEM_POSITION, intent)
        }

        CALLED_FROM_FEED_INFO -> {
            val intent = Intent(this, FeedInfoActivity::class.java)
            intent.putExtra(EXTRA_PHOTO_POSITION, initialPosition)
            setResult(SELECTED_FEED_INFO_PHOTO_VIEW, intent)
        }

        CALLED_FROM_PHOTO_INFO -> {
            val intent = Intent(this, PhotoInfoActivity::class.java)
            intent.putExtra(EXTRA_PHOTO_POSITION, initialPosition)
            setResult(SELECTED_PHOTO_INFO_PHOTO_VIEW, intent)
        }

        CALLED_FROM_PHOTOG_PHOTO -> {
            val intent = Intent(this, PhotogPhotoActivity::class.java)
            intent.putExtra(EXTRA_PHOTO_POSITION, initialPosition)
            setResult(SELECTED_PHOTOG_PHOTO_VIEW, intent)
        }

        else -> {}
    }

    private fun showDeleteDialog(text: String, iconId: Int,
                                 position: Int, path: String) {
        val alertDialogBuilder=AlertDialog.Builder(this)

        alertDialogBuilder.setTitle(getString(R.string.phrase_dialog_title_delete))
        alertDialogBuilder
                .setMessage(text)
                .setCancelable(false)
                .setIcon(iconId)
                .setPositiveButton(getString(R.string.dialog_button_yes)) { _, _ -> deletePhoto(path, position) }
                .setNegativeButton(getString(R.string.dialog_button_no)) { dialog, _ ->
                    // if this button is clicked, just close
                    // the dialog box and do nothing
                    dialog.cancel()
                }

        val alertDialog=alertDialogBuilder.create()

        alertDialog.show()
    }

    private fun deletePhoto(path: String, position: Int) {
        if (workHandler.deletePhoto(eraser, getImagePath(this) + File.separator + path, position)) {
            isErasedPhoto = true
            deleteLocalInfo(photoPathList[position])
            showToastMessage(this, getString(R.string.phrase_delete_photo), Toast.LENGTH_SHORT)
        } else {
            showToastMessage(this, getString(R.string.phrase_fail_delete_photo), Toast.LENGTH_SHORT)
        }

        withDelay(500L) {
            finishAfterTransition()
        }
    }

    internal fun getData(position: Int) {
        getSearperInfo(position)
        getSearperProfile(photoIdList[position])
        getEXIFInfo(position)
        getLocationInfo(position)
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

    @Subscribe(threadMode=ThreadMode.MAIN)
    override fun onEvent(event: ActivityStackClearEvent) {
        this@PhotoViewerActivity.finish()
    }
}