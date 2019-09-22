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

package com.goforer.grabph.presentation.ui.pinnedlist

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
import com.goforer.grabph.presentation.caller.Caller.EXTRA_USER_ICONFARM
import com.goforer.grabph.presentation.caller.Caller.EXTRA_USER_ICONSERVER
import com.goforer.grabph.presentation.caller.Caller.EXTRA_USER_ID
import com.goforer.grabph.presentation.caller.Caller.EXTRA_USER_NAME
import com.goforer.grabph.presentation.common.effect.transition.TransitionCallback
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import com.goforer.grabph.presentation.ui.pinnedlist.fragment.PinnedFeedsFragment
import kotlinx.android.synthetic.main.activity_pinned_feed_list.*
import kotlinx.android.synthetic.main.activity_pinned_feed_list.iv_disconnect_pinned_feed_list
import kotlinx.android.synthetic.main.activity_pinned_feed_list.toolbar
import kotlinx.android.synthetic.main.activity_pinned_feed_list.tv_notice1_pinned_feed_list
import kotlinx.android.synthetic.main.recycler_view_container.recycler_view
import javax.inject.Inject

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class PinnedFeedsActivity: BaseActivity() {
    private lateinit var userName: String
    private lateinit var userID: String
    private lateinit var iconServer: String

    private var iconFarm: Int = -1

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
                userName = savedInstanceState.getString(EXTRA_USER_NAME, "")
                userID = savedInstanceState.getString(EXTRA_USER_ID, "")
                iconFarm = savedInstanceState.getInt(EXTRA_USER_ICONFARM, -1)
                iconServer = savedInstanceState.getString(EXTRA_USER_ICONSERVER, "")
            }

            displayUserPicture(iconServer, workHandler.getProfilePhotoURL(iconFarm, iconServer, userID))
            this@PinnedFeedsActivity.tv_user_name.text = String.format(getString(R.string.phrase_user_name_pinned_feeds), userName)
        } else {
            networkStatusVisible(false)
        }
    }

    override fun setContentView() {
        setContentView(R.layout.activity_pinned_feed_list)
    }

    @SuppressLint("RestrictedApi")
    override fun setActionBar() {
        setSupportActionBar(this@PinnedFeedsActivity.toolbar)

        val actionBar = supportActionBar

        actionBar?.let {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_USE_LOGO
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.elevation = 0f
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
        }

        this@PinnedFeedsActivity.toolbar.setNavigationOnClickListener{
            finishAfterTransition()
        }

        this@PinnedFeedsActivity.toolbar.hideOverflowMenu()
    }

    override fun setViews(savedInstanceState: Bundle?) {
        // In case of viewing an item in the list, the photo couldn't be downloaded(Concept get changed...)
        transactFragment(PinnedFeedsFragment::class.java, R.id.disconnect_container_pinned_feed_list, false)
    }

    override fun onActivityReenter(resultCode: Int, data: Intent) {
        super.onActivityReenter(resultCode, data)

        // Listener to reset shared element exit transition callbacks.
        window.sharedElementExitTransition.addListener(sharedExitListener)
        supportPostponeEnterTransition()
        doReenterFromPinnedFeed(data, resultCode)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(EXTRA_USER_NAME, userName)
        outState.putString(EXTRA_USER_ID, userID)
        outState.putInt(EXTRA_USER_ICONFARM, iconFarm)
        outState.putString(EXTRA_USER_ICONSERVER, iconServer)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        savedInstanceState ?: return
        userName = savedInstanceState.getString(EXTRA_USER_NAME, "")
        userID = savedInstanceState.getString(EXTRA_USER_ID, "")
        iconFarm = savedInstanceState.getInt(EXTRA_USER_ICONFARM)
        iconServer = savedInstanceState.getString(EXTRA_USER_ICONSERVER, "")

    }

    private fun networkStatusVisible(isVisible: Boolean) = if (isVisible) {
        this@PinnedFeedsActivity.iv_disconnect_pinned_feed_list.visibility = View.GONE
        this@PinnedFeedsActivity.tv_notice1_pinned_feed_list.visibility = View.GONE
        this@PinnedFeedsActivity.tv_notice1_pinned_feed_list.visibility = View.GONE
        this@PinnedFeedsActivity.appbar_layout_pinned_feed_list.visibility = View.VISIBLE
    } else {
        this@PinnedFeedsActivity.iv_disconnect_pinned_feed_list.visibility = View.VISIBLE
        this@PinnedFeedsActivity.tv_notice1_pinned_feed_list.visibility = View.VISIBLE
        this@PinnedFeedsActivity.tv_notice1_pinned_feed_list.visibility = View.VISIBLE
        this@PinnedFeedsActivity.appbar_layout_pinned_feed_list.visibility = View.GONE
    }

    private fun getIntentData() {
        userID = intent.getStringExtra(EXTRA_USER_ID)
        iconFarm =  intent.getIntExtra(EXTRA_USER_ICONFARM, -1)
        iconServer = intent.getStringExtra(EXTRA_USER_ICONSERVER)
        userName = intent.getStringExtra(EXTRA_USER_NAME)
    }

    private fun displayUserPicture(iconServer: String, picturePath: String) {
        if (iconServer == "0") {
            this@PinnedFeedsActivity.iv_user_pic.setImageDrawable(this.applicationContext!!
                    .getDrawable(R.drawable.ic_default_profile))
        } else {
            setImageDraw(this@PinnedFeedsActivity.iv_user_pic, picturePath)
        }
    }


    private fun doReenterFromPinnedFeed(intent: Intent?, resultCode: Int) {
        intent ?: return

        val position = when(resultCode) {
            Caller.SELECTED_PINNED_FEED_ITEM_POSITION -> {
                intent.getIntExtra(Caller.EXTRA_FEED_INFO_POSITION, 0)
            }
            Caller.SELECTED_PINNED_INFO_PHOTO_VIEW -> {
                intent.getIntExtra(Caller.EXTRA_PHOTO_POSITION, 0)
            }
            else -> {
                0
            }
        }

        this@PinnedFeedsActivity.recycler_view?.let { view ->
            betterSmoothScrollToPosition(view, position)
            view.viewTreeObserver?.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    view.viewTreeObserver.removeOnPreDrawListener(this)

                    return true
                }
            })

            supportStartPostponedEnterTransition()
        }
    }
}