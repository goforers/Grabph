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

package com.goforer.grabph.presentation.ui.searplegallery

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.transition.Transition
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.ActionBar
import androidx.core.app.SharedElementCallback
import com.goforer.base.presentation.utils.CommonUtils.betterSmoothScrollToPosition
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.caller.Caller.SELECTED_PINNED_FEED_ITEM_POSITION
import com.goforer.grabph.presentation.caller.Caller.SELECTED_PINNED_INFO_PHOTO_VIEW
import com.goforer.grabph.presentation.caller.Caller.SELECTED_SEARPLE_GALLERY_ITEM_POSITION
import com.goforer.grabph.presentation.common.effect.transition.TransitionCallback
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import com.goforer.grabph.presentation.ui.searplegallery.fragment.SearpleGalleryFragment
import kotlinx.android.synthetic.main.activity_searple_gallery.*
import kotlinx.android.synthetic.main.recycler_view_container.*
import javax.inject.Inject

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class SearpleGalleryActivity: BaseActivity() {
    private lateinit var userName: String
    private lateinit var userID: String
    private lateinit var iconServer: String

    private var iconFarm: Int = -1

    @field:Inject
    lateinit var workHandler: CommonWorkHandler

    companion object {
        internal const val VISIBLE_UPTO_ITEMS = 20
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isNetworkAvailable) {
            networkStatusVisible(true)
            savedInstanceState ?: getIntentData()
            savedInstanceState?.let {
                userName = savedInstanceState.getString(Caller.EXTRA_USER_NAME, "")
                userID = savedInstanceState.getString(Caller.EXTRA_USER_ID, "")
                iconFarm = savedInstanceState.getInt(Caller.EXTRA_USER_ICONFARM, -1)
                iconServer = savedInstanceState.getString(Caller.EXTRA_USER_ICONSERVER, "")
            }

            displayUserPicture(iconFarm, iconServer, userID)
            this@SearpleGalleryActivity.tv_user_name.text = String.format(getString(R.string.phrase_user_name_gallery), userName)
        } else {
            networkStatusVisible(false)
        }
    }

    override fun setContentView() {
        setContentView(R.layout.activity_searple_gallery)
    }

    @SuppressLint("RestrictedApi")
    override fun setActionBar() {
        setSupportActionBar(this@SearpleGalleryActivity.toolbar)

        val actionBar = supportActionBar

        actionBar?.let {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_USE_LOGO
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.elevation = 0f
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
        }

        this@SearpleGalleryActivity.toolbar.setNavigationOnClickListener {
            finishAfterTransition()
        }

        this@SearpleGalleryActivity.toolbar.hideOverflowMenu()
    }

    override fun setViews(savedInstanceState: Bundle?) {
        // In case of viewing an item in the list, the photo couldn't be downloaded(Concept get changed...)
        transactFragment(SearpleGalleryFragment::class.java, R.id.disconnect_container_searple_gallery, false)
    }

    override fun onActivityReenter(resultCode: Int, data: Intent) {
        super.onActivityReenter(resultCode, data)

        // Listener to reset shared element exit transition callbacks.
        window.sharedElementExitTransition.addListener(sharedExitListener)
        supportPostponeEnterTransition()
        doReenterFromViewer(data, resultCode)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(Caller.EXTRA_USER_NAME, userName)
        outState.putString(Caller.EXTRA_USER_ID, userID)
        outState.putInt(Caller.EXTRA_USER_ICONFARM, iconFarm)
        outState.putString(Caller.EXTRA_USER_ICONSERVER, iconServer)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        userName = savedInstanceState.getString(Caller.EXTRA_USER_NAME, "")
        userID = savedInstanceState.getString(Caller.EXTRA_USER_ID, "")
        iconFarm = savedInstanceState.getInt(Caller.EXTRA_USER_ICONFARM)
        iconServer = savedInstanceState.getString(Caller.EXTRA_USER_ICONSERVER, "")

    }

    private fun networkStatusVisible(isVisible: Boolean) = if (isVisible) {
        this@SearpleGalleryActivity.iv_disconnect_searple_gallery.visibility = View.GONE
        this@SearpleGalleryActivity.tv_notice1_searple_gallery.visibility = View.GONE
        this@SearpleGalleryActivity.tv_notice1_searple_gallery.visibility = View.GONE
        this@SearpleGalleryActivity.appbar_layout_searple_gallery.visibility = View.VISIBLE
    } else {
        this@SearpleGalleryActivity.iv_disconnect_searple_gallery.visibility = View.VISIBLE
        this@SearpleGalleryActivity.tv_notice1_searple_gallery.visibility = View.VISIBLE
        this@SearpleGalleryActivity.tv_notice1_searple_gallery.visibility = View.VISIBLE
        this@SearpleGalleryActivity.appbar_layout_searple_gallery.visibility = View.GONE
    }

    private fun getIntentData() {
        userID = intent.getStringExtra(Caller.EXTRA_USER_ID)
        iconFarm = intent.getIntExtra(Caller.EXTRA_USER_ICONFARM, -1)
        iconServer = intent.getStringExtra(Caller.EXTRA_USER_ICONSERVER)
        userName = intent.getStringExtra(Caller.EXTRA_USER_NAME)
    }

    private fun displayUserPicture(iconFarm: Int, iconServer: String, id: String) {
        if (iconServer == "0") {
            this@SearpleGalleryActivity.iv_user_pic.setImageDrawable(this.applicationContext!!
                    .getDrawable(R.drawable.ic_default_profile))
        } else {
            setImageDraw(this@SearpleGalleryActivity.iv_user_pic, workHandler.getProfilePhotoURL(iconFarm, iconServer, id))
        }
    }


    private fun doReenterFromViewer(intent: Intent?, resultCode: Int) {
        intent ?: return

        val position = when (resultCode) {
            SELECTED_PINNED_FEED_ITEM_POSITION -> {
                intent.getIntExtra(Caller.EXTRA_FEED_INFO_POSITION, 0)
            }

            SELECTED_PINNED_INFO_PHOTO_VIEW -> {
                intent.getIntExtra(Caller.EXTRA_PHOTO_POSITION, 0)
            }

            SELECTED_SEARPLE_GALLERY_ITEM_POSITION -> {
                intent.getIntExtra(Caller.EXTRA_SELECTED_ITEM_POSITION, 0)
            }

            else -> {
                0
            }
        }

        this@SearpleGalleryActivity.recycler_view?.let { view ->
            betterSmoothScrollToPosition(view, position)
            view.viewTreeObserver?.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    view.viewTreeObserver.removeOnPreDrawListener(this)
                    supportStartPostponedEnterTransition()

                    return true
                }
            })
        }
    }
}