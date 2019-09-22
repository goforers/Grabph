/*
 * Copyright (C) 2018 Lukoh Nam, goForer
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

@file:Suppress("DEPRECATION")

package com.goforer.grabph.presentation.ui.photog

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.transition.Transition
import androidx.core.app.SharedElementCallback
import androidx.appcompat.app.ActionBar
import android.view.View
import android.view.ViewTreeObserver
import androidx.lifecycle.Observer
import com.goforer.base.domain.common.GeneralFunctions
import com.goforer.base.presentation.utils.CommonUtils.betterSmoothScrollToPosition
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller.EXTRA_SEARPER_ICONFARM
import com.goforer.grabph.presentation.caller.Caller.EXTRA_SEARPER_ICONSERVER
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PAGES
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PHOTOG_PHOTO_TYPE
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PHOTO_INFO_SELECTED_ITEM_POSITION
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PHOTO_POSITION
import com.goforer.grabph.presentation.caller.Caller.EXTRA_SEARPER_ID
import com.goforer.grabph.presentation.caller.Caller.EXTRA_SEARPER_NAME
import com.goforer.grabph.presentation.caller.Caller.SELECTED_PHOTO_INFO_ITEM_POSITION
import com.goforer.grabph.presentation.common.effect.transition.TransitionCallback
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import com.goforer.grabph.presentation.common.view.SlidingDrawer
import com.goforer.grabph.presentation.vm.people.person.PersonViewModel
import com.goforer.grabph.presentation.ui.photog.fragment.PhotogPhotoFragment
import com.goforer.grabph.repository.model.cache.data.entity.profile.Person
import com.goforer.grabph.repository.network.response.Status
import com.goforer.grabph.repository.network.resource.NetworkBoundResource
import com.goforer.grabph.repository.network.response.Resource
import com.goforer.grabph.repository.interactor.remote.people.person.PersonRepository
import com.goforer.grabph.repository.interactor.remote.Repository
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import kotlinx.android.synthetic.main.activity_photog_photo.*
import kotlinx.android.synthetic.main.activity_photog_photo.toolbar
import kotlinx.android.synthetic.main.fragment_photog_photo.*
import kotlinx.coroutines.*
import javax.inject.Inject

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class PhotogPhotoActivity: BaseActivity() {
    internal lateinit var searperID: String

    private var iconFarm: Int = -1

    private lateinit var iconServer: String

    internal var searperPhotoPath: String? = null
    internal lateinit var searperName: String

    internal var pages: Int = 0
    internal var type: Int = 0

    private val job = Job()

    private val ioScope = CoroutineScope(Dispatchers.IO + job)

    internal lateinit var slidingDrawer: SlidingDrawer

    @field:Inject
    lateinit var searperProfileViewModel: PersonViewModel

    @field:Inject
    lateinit var workHandler: CommonWorkHandler

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isNetworkAvailable) {
            networkStatusVisible(true)
            savedInstanceState ?: getIntentData()
            savedInstanceState?.let {
                searperID = savedInstanceState.getString(EXTRA_SEARPER_ID, "")
                iconFarm =  savedInstanceState.getInt(EXTRA_SEARPER_ICONFARM, -1)
                iconServer = savedInstanceState.getString(EXTRA_SEARPER_ICONSERVER, "")
                searperName = savedInstanceState.getString(EXTRA_SEARPER_NAME, "")
                pages = savedInstanceState.getInt(EXTRA_PAGES, -1)
                type = savedInstanceState.getInt(EXTRA_PHOTOG_PHOTO_TYPE, -1)
            }

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
            removeCache(searperProfileViewModel.interactor)
            searperProfileViewModel.loadType = NetworkBoundResource.LOAD_PERSON
            searperProfileViewModel.boundType = Repository.BOUND_FROM_BACKEND

            displaySearperPicture(iconFarm, iconServer, searperID, searperName)
        } else {
            networkStatusVisible(false)
        }
    }

    @ExperimentalCoroutinesApi
    override fun onDestroy() {
        super.onDestroy()

        ioScope.cancel()
        job.cancel()
    }

    override fun setContentView() {
        setContentView(R.layout.activity_photog_photo)
    }

    @SuppressLint("RestrictedApi")
    override fun setActionBar() {
        setSupportActionBar(this@PhotogPhotoActivity.toolbar)

        val actionBar = supportActionBar

        actionBar?.let {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_USE_LOGO
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.elevation = 0f
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
        }

        this@PhotogPhotoActivity.toolbar.setNavigationOnClickListener{
            finishAfterTransition()
        }

        this@PhotogPhotoActivity.toolbar.hideOverflowMenu()
    }

    override fun setViews(savedInstanceState: Bundle?) {
        if (!isNetworkAvailable) {
            this@PhotogPhotoActivity.iv_disconnect_photog_photo.visibility = View.VISIBLE
            this@PhotogPhotoActivity.tv_notice1_photog_photo.visibility = View.VISIBLE
            this@PhotogPhotoActivity.tv_notice1_photog_photo.visibility = View.VISIBLE
        }

        // In case of viewing an item in the list, the photo couldn't be downloaded(Concept get changed...)
        transactFragment(PhotogPhotoFragment::class.java, R.id.disconnect_container_photog_photo, false)
    }

    override fun onActivityReenter(resultCode: Int, data: Intent) {
        super.onActivityReenter(resultCode, data)

        // Listener to reset shared element exit transition callbacks.
        window.sharedElementExitTransition.addListener(sharedExitListener)
        supportPostponeEnterTransition()
        doReenterFromPhoto(resultCode, data)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var outStateBundle = outState

        outStateBundle.putString(EXTRA_SEARPER_ID, searperID)
        outStateBundle.putInt(EXTRA_SEARPER_ICONFARM, iconFarm)
        outStateBundle.putString(EXTRA_SEARPER_ICONSERVER, iconServer)
        outStateBundle.putString(EXTRA_SEARPER_NAME, searperName)
        outStateBundle.putInt(EXTRA_PAGES, pages)
        outStateBundle.putInt(EXTRA_PHOTOG_PHOTO_TYPE, type)

        slidingDrawer.searperProfileDrawerForFeedViewDrawer?.let {
            outStateBundle = slidingDrawer.searperProfileDrawerForFeedViewDrawer?.saveInstanceState(outState)!!
            outStateBundle = slidingDrawer.drawerHeader?.saveInstanceState(outState)!!
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        savedInstanceState ?: return
        searperID = savedInstanceState.getString(EXTRA_SEARPER_ID, "")
        iconFarm = savedInstanceState.getInt(EXTRA_SEARPER_ICONFARM, -1)
        iconServer = savedInstanceState.getString(EXTRA_SEARPER_ICONSERVER, "")
        searperName = savedInstanceState.getString(EXTRA_SEARPER_NAME, "")
        pages = savedInstanceState.getInt(EXTRA_PAGES, -1)
        type = savedInstanceState.getInt(EXTRA_PHOTOG_PHOTO_TYPE, -1)
    }

    private fun networkStatusVisible(isVisible: Boolean) = if (isVisible) {
        this@PhotogPhotoActivity.iv_disconnect_photog_photo.visibility = View.GONE
        this@PhotogPhotoActivity.tv_notice1_photog_photo.visibility = View.GONE
        this@PhotogPhotoActivity.tv_notice2_photog_photo.visibility = View.GONE
        this@PhotogPhotoActivity.appbar_layout_photog_photo.visibility = View.VISIBLE
    } else {
        this@PhotogPhotoActivity.iv_disconnect_photog_photo.visibility = View.VISIBLE
        this@PhotogPhotoActivity.tv_notice1_photog_photo.visibility = View.VISIBLE
        this@PhotogPhotoActivity.tv_notice2_photog_photo.visibility = View.VISIBLE
        this@PhotogPhotoActivity.appbar_layout_photog_photo.visibility = View.GONE
    }

    private fun getIntentData() {
        searperID = intent.getStringExtra(EXTRA_SEARPER_ID)!!
        iconFarm = intent.getIntExtra(EXTRA_SEARPER_ICONFARM, -1)
        iconServer = intent.getStringExtra(EXTRA_SEARPER_ICONSERVER)!!
        searperName = intent.getStringExtra(EXTRA_SEARPER_NAME)!!
        pages = intent.getIntExtra(EXTRA_PAGES, -1)
        type = intent.getIntExtra(EXTRA_PHOTOG_PHOTO_TYPE, -1)
    }

    private fun  getSearperProfile() = searperProfileViewModel.person.observe(this, Observer { resource ->
        when(resource?.getStatus()) {
            Status.SUCCESS -> {
                resource.getData()?.let { person ->
                    slidingDrawer.setHeaderBackground(GeneralFunctions.getHeaderBackgroundUrl())
                    slidingDrawer.setSearperProfileDrawer(person as? Person?,
                            SlidingDrawer.PROFILE_SEARPER_TYPE_FROM_FEED_VIEWER)
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

    private fun showNetworkError(resource: Resource) =  when(resource.errorCode) {
        in 400..499 -> {
            Snackbar.make(this@PhotogPhotoActivity.coordinator_photog_photo_layout, getString(R.string.phrase_client_wrong_request), LENGTH_LONG).show()
        }

        in 500..599 -> {
            Snackbar.make(this@PhotogPhotoActivity.coordinator_photog_photo_layout, getString(R.string.phrase_server_wrong_response), LENGTH_LONG).show()
        }

        else -> {
            Snackbar.make(this@PhotogPhotoActivity.coordinator_photog_photo_layout, resource.getMessage().toString(), LENGTH_LONG).show()
        }
    }

    @SuppressLint("CheckResult")
    private fun displaySearperPicture(iconFarm: Int, iconServer: String, id: String, searperName: String) {
        searperProfileViewModel.setSearperId(id)
        getSearperProfile()

        searperPhotoPath = workHandler.getProfilePhotoURL(iconFarm, iconServer, id)
        if (iconServer == "0") {
            this@PhotogPhotoActivity.iv_searper_pic.setImageDrawable(this.applicationContext!!
                    .getDrawable(R.drawable.ic_default_profile))
        } else {
            setImageDraw(this@PhotogPhotoActivity.iv_searper_pic, searperPhotoPath!!)
            this@PhotogPhotoActivity.iv_searper_pic.setOnClickListener {
                slidingDrawer.searperProfileDrawerForFeedViewDrawer?.openDrawer()
            }
        }

        this@PhotogPhotoActivity.tv_searper_name.text = searperName
    }

    private fun doReenterFromPhoto(resultCode: Int, intent: Intent?) {
        intent ?: return
        this@PhotogPhotoActivity.recycler_view?.let { recyclerView ->
            val position: Int

            if (resultCode == SELECTED_PHOTO_INFO_ITEM_POSITION) {
                position = intent.getIntExtra(EXTRA_PHOTO_INFO_SELECTED_ITEM_POSITION, 0)
                betterSmoothScrollToPosition(recyclerView, position)
            } else {
                position = intent.getIntExtra(EXTRA_PHOTO_POSITION, 0)
            }

            betterSmoothScrollToPosition(recyclerView, position)
            // Start the postponed transition when the recycler view is ready to be drawn.
            recyclerView.viewTreeObserver?.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    recyclerView.viewTreeObserver.removeOnPreDrawListener(this)

                    return true
                }
            })

            supportStartPostponedEnterTransition()
        }
    }

    private fun removeCache(repository: Repository) = launchWork {
        (repository as PersonRepository).removePerson()
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
    private inline fun launchWork(crossinline block: suspend () -> Unit): Job {
        return ioScope.launch {
            block()
        }
    }
}
