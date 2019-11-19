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

package com.goforer.grabph.presentation.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.transition.Transition
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.app.SharedElementCallback
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.goforer.base.annotation.RunWithMockData
import com.goforer.base.presentation.utils.CommonUtils
import com.goforer.base.presentation.utils.CommonUtils.betterSmoothScrollToPosition
import com.goforer.base.presentation.utils.SharedPreference
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.base.presentation.view.helper.BottomNavigationViewHelper
import com.goforer.grabph.R
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.caller.Caller.EXTRA_CATEGORY_POSITION
import com.goforer.grabph.presentation.caller.Caller.EXTRA_FEED_INFO_POSITION
import com.goforer.grabph.presentation.caller.Caller.EXTRA_HOME_BOTTOM_MENU_ID
import com.goforer.grabph.presentation.caller.Caller.EXTRA_HOP_TOPIC_POSITION
import com.goforer.grabph.presentation.caller.Caller.EXTRA_QUEST_POSITION
import com.goforer.grabph.presentation.caller.Caller.SELECTED_BEST_PICK_CATEGORY_POSITION
import com.goforer.grabph.presentation.caller.Caller.SELECTED_BEST_PICK_HOT_PHOTO_POSITION
import com.goforer.grabph.presentation.caller.Caller.SELECTED_BEST_PICK_HOT_TOPIC_POSITION
import com.goforer.grabph.presentation.caller.Caller.SELECTED_BEST_PICK_QUEST_POSITION
import com.goforer.grabph.presentation.caller.Caller.SELECTED_BEST_PICK_SEARPER_PHOTO_POSITION
import com.goforer.grabph.presentation.caller.Caller.SELECTED_FEED_ITEM_POSITION
import com.goforer.grabph.presentation.caller.Caller.SELECTED_QUEST_INFO_ITEM_FROM_FAVORITE_QUEST_POSITION
import com.goforer.grabph.presentation.caller.Caller.SELECTED_QUEST_INFO_ITEM_FROM_HOME_MAIN_POSITION
import com.goforer.grabph.presentation.caller.Caller.SELECTED_QUEST_INFO_ITEM_FROM_HOT_QUEST_POSITION
import com.goforer.grabph.presentation.caller.Caller.SELECTED_QUEST_INFO_ITEM_POSITION
import com.goforer.grabph.presentation.common.effect.transition.TransitionCallback
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import com.goforer.grabph.presentation.ui.home.feed.fragment.HomeFeedFragment
import com.goforer.grabph.presentation.ui.home.main.fragment.HomeMainFragment
import com.goforer.grabph.presentation.ui.home.quest.fragment.HomeQuestFragment
import com.goforer.grabph.presentation.vm.home.HomeViewModel
import com.goforer.grabph.presentation.ui.home.profile.fragment.HomeProfileFragment
import com.goforer.grabph.presentation.vm.BaseViewModel.Companion.NONE_TYPE
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_HOME_MAIN
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton.LayoutParams
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home.appbar_layout
import kotlinx.android.synthetic.main.activity_home.toolbar
import kotlinx.android.synthetic.main.fragment_home_quest.*
import kotlinx.android.synthetic.main.layout_home_bottom_navigation.*
import kotlinx.android.synthetic.main.recycler_view_container.*
import kotlinx.android.synthetic.main.snap_main_item.*
import kotlinx.android.synthetic.main.snap_quest_item.*
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import kotlin.system.exitProcess

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@RunWithMockData(true)
class HomeActivity: BaseActivity() {
    private var resultCode: Int = 0
    private val requestCodeForImage = 100

    private lateinit var mainFragment: HomeMainFragment
    private lateinit var feedFragment: HomeFeedFragment
    private lateinit var questFragment: HomeQuestFragment
    private lateinit var profileFragment: HomeProfileFragment

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)
    private val ioScope = CoroutineScope(Dispatchers.IO + job)

    private lateinit var bottomMenuItem: MenuItem
    private lateinit var searchItem: MenuItem
    private lateinit var settingItem: MenuItem
    private var itemId = 0


    internal var searchView: SearchView? = null

    internal lateinit var floatingActionMenu: FloatingActionMenu

    @field:Inject
    internal lateinit var homeViewModel: HomeViewModel

    @field:Inject
    lateinit var workHandler: CommonWorkHandler

    companion object {
        internal const val VISIBLE_UPTO_ITEMS = 20
        private const val ID_HOME = 0
        private const val ID_FEED = 1
        private const val ID_UPLOAD = 2
        private const val ID_QUEST = 3
        private const val ID_PROFILE = 4
    }

    private val sharedExitListener = object : TransitionCallback() {
        override fun onTransitionEnd(transition: Transition) {
            removeCallback()
            if (resultCode == SELECTED_FEED_ITEM_POSITION
                || resultCode == Caller.SELECTED_SEARCH_ITEM_POSITION) {
                val fragment = getFragment(HomeFeedFragment::class.java) as HomeFeedFragment

                if (fragment.isFeedUpdated) {
                    fragment.feedViewModel.updateFeedItem(fragment.feedItem)
                    fragment.isFeedUpdated = false
                }
            }
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
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.TRANSPARENT
            networkStatusVisible(true)
            // createFloatingActionMenu(this@HomeActivity.fam_post_writing)
            savedInstanceState ?: transactMainFragment()
            savedInstanceState?.let {
                itemId = savedInstanceState.getInt(EXTRA_HOME_BOTTOM_MENU_ID, 0)
                bottomMenuItem = this@HomeActivity.bottom_navigation_view.menu.findItem(this@HomeActivity.bottom_navigation_view.selectedItemId)
                selectItem(itemId)
                setBottomNavigationBehavior(itemId)
            }

            this@HomeActivity.bottom_navigation_view.setOnNavigationItemSelectedListener {
                bottomMenuItem = it
                itemId = it.itemId
                selectItem(it, it.itemId)

                false
            }

            setBottomNavigationClickListener()
            BottomNavigationViewHelper.setupView(this@HomeActivity.bottom_navigation_view)
        } else {
            networkStatusVisible(false)
        }
    }

    @ExperimentalCoroutinesApi
    override fun onDestroy() {
        super.onDestroy()

        uiScope.cancel()
        ioScope.cancel()
        job.cancel()
    }

    override fun onBackPressed() {
        ActivityCompat.finishAffinity(this)
        exitProcess(0)
    }

    @SuppressLint("RestrictedApi")
    override fun setActionBar() {
        setSupportActionBar(this@HomeActivity.toolbar)

        val actionBar = supportActionBar

        actionBar?.let {
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.elevation = 0f
        }

        this@HomeActivity.toolbar.hideOverflowMenu()
    }

    override fun setViews(savedInstanceState: Bundle?) {
        launchIOWork {
            homeViewModel.deleteHome()
        }

        this@HomeActivity.appbar_layout.outlineProvider = null
    }

    override fun setContentView() {
        setContentView(R.layout.activity_home)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(EXTRA_HOME_BOTTOM_MENU_ID, itemId)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        itemId = savedInstanceState.getInt(EXTRA_HOME_BOTTOM_MENU_ID, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == requestCodeForImage) { // move to choose photo
                data?.let { intent ->
                    val imageUri: Uri? = intent.clipData?.getItemAt(0)?.uri ?: intent.data
                    if (imageUri == null) {
                        Timber.e("Invalid input image Uri.")
                        return
                    }
                    Caller.callUploadPhoto(this, imageUri.toString(), CALLED_FROM_HOME_MAIN)
                }
            }
        }
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)

        this.resultCode = resultCode

        // Listener to reset shared element exit transition callbacks.
        window.sharedElementExitTransition.addListener(sharedExitListener)
        supportPostponeEnterTransition()
        when (resultCode) {
            SELECTED_BEST_PICK_HOT_TOPIC_POSITION -> doReenterHotTopic(data)
            SELECTED_QUEST_INFO_ITEM_FROM_HOME_MAIN_POSITION -> doReenterMainQuest(data)
            SELECTED_QUEST_INFO_ITEM_FROM_HOT_QUEST_POSITION -> doReenterHotQuest(data)
            SELECTED_QUEST_INFO_ITEM_FROM_FAVORITE_QUEST_POSITION -> doReenterFavoriteMission(data)
            SELECTED_BEST_PICK_HOT_PHOTO_POSITION, SELECTED_BEST_PICK_SEARPER_PHOTO_POSITION -> doReenterHomeHotPhoto(data)
            SELECTED_BEST_PICK_QUEST_POSITION -> doReenterQuest(data)
            SELECTED_BEST_PICK_CATEGORY_POSITION -> doReenterHomeCategory(data)
            SELECTED_FEED_ITEM_POSITION  -> doReenterFromFeed(data)
            SELECTED_QUEST_INFO_ITEM_POSITION -> doReenterFromQuest(data)
            else -> doReenterQuest(data)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        searchItem = menu.findItem(R.id.action_call_search)
        settingItem = menu.findItem(R.id.action_call_setting)
        settingItem.isVisible = false
        searchItem.actionView?.let {
            searchView = searchItem.actionView as SearchView
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onPreparePanel(featureId: Int, view: View?, menu: Menu): Boolean {
        if (menu.javaClass.simpleName == "MenuBuilder") {
            try {
                @SuppressLint("PrivateApi")
                val method = menu.javaClass.getDeclaredMethod("setOptionalIconsVisible", java.lang.Boolean.TYPE)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_call_search -> {
            Caller.callFeedSearch(this)

            true
        }

        R.id.action_call_setting -> {
            Caller.callSetting(this)

            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private fun transactMainFragment() {
        itemId = 0
        mainFragment = transactFragment(HomeMainFragment::class.java, R.id.home_container) as HomeMainFragment
    }

    private fun networkStatusVisible(isVisible: Boolean) = if (isVisible) {
        this@HomeActivity.iv_disconnect_home.visibility = View.GONE
        this@HomeActivity.tv_notice1_home.visibility = View.GONE
        this@HomeActivity.tv_notice2_home.visibility = View.GONE
    } else {
        this@HomeActivity.iv_disconnect_home.visibility = View.VISIBLE
        this@HomeActivity.tv_notice1_home.visibility = View.VISIBLE
        this@HomeActivity.tv_notice2_home.visibility = View.VISIBLE
    }

    private fun createFloatingActionMenu(view: View) {
        val fabRadius = resources.getDimensionPixelSize(R.dimen.radius_84)
        val fasbSize = resources.getDimensionPixelSize(R.dimen.size_52)
        val fasbContentMargin = resources.getDimensionPixelSize(R.dimen.margin_14)
        val contentParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        val cameraView = AppCompatImageView(this)
        val galleryView = AppCompatImageView(this)

        contentParams.setMargins(fasbContentMargin, fasbContentMargin, fasbContentMargin, fasbContentMargin)

        val builder = SubActionButton.Builder(this)
            .setBackgroundDrawable(getDrawable(R.drawable.fab_selector))
            .setLayoutParams(contentParams)
            .setLayoutParams(LayoutParams(fasbSize, fasbSize))

        val sabCamera = builder.setContentView(cameraView, contentParams).build()
        val sabGallery = builder.setContentView(galleryView, contentParams).build()

        cameraView.setImageDrawable(resources.getDrawable(R.drawable.ic_upload_camera, null))
        galleryView.setImageDrawable(resources.getDrawable(R.drawable.ic_upload_album, null))

        floatingActionMenu = FloatingActionMenu.Builder(this)
            .addSubActionView(sabCamera)
            .addSubActionView(sabGallery)
            .setRadius(fabRadius)
            .setStartAngle(225)
            .setEndAngle(-45)
            .attachTo(view)
            .build()


        sabCamera.setOnClickListener {
            closeFab()
            CommonUtils.showToastMessage(this, getString(R.string.phrase_photo_upload_implement), Toast.LENGTH_SHORT)
        }

        sabGallery.setOnClickListener {
            closeFab()
            uploadPhotos()
        }
    }

    private fun uploadPhotos() {
        // if (isAuthValid()) choosePhotos() else getAuthorization()
        choosePhotos()
    }

    private fun isAuthValid(): Boolean = SharedPreference.hasAccessToken(this)

    private fun choosePhotos() {
        Caller.callPhotoGallery(this, CALLED_FROM_HOME_MAIN, requestCodeForImage)
    }

    private fun getAuthorization() {
        Caller.callAuthActivity(this)
    }

    private fun doReenterMainQuest(intent: Intent?) {
        intent ?: return

        val position = intent.getIntExtra(EXTRA_QUEST_POSITION, 0)

        this@HomeActivity.recycler_view_snap_quest?.let { view ->
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

    private fun doReenterQuest(intent: Intent?) {
        intent ?: return

        val position = intent.getIntExtra(EXTRA_QUEST_POSITION, 0)

        this@HomeActivity.recycler_view_snap_hottopic_pick?.let { view ->
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

    private fun doReenterHotTopic(intent: Intent?) {
        intent ?: return

        val position = intent.getIntExtra(EXTRA_HOP_TOPIC_POSITION, 0)

        this@HomeActivity.recycler_view_snap_hottopic_pick?.let { view ->
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

    private fun doReenterHotQuest(intent: Intent?) {
        intent ?: return

        val position = intent.getIntExtra(EXTRA_QUEST_POSITION, 0)

        this@HomeActivity.recycler_view_snap_hot_quest?.let { view ->
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

    private fun doReenterFavoriteMission(intent: Intent?) {
        intent ?: return

        val position = intent.getIntExtra(EXTRA_QUEST_POSITION, 0)

        this@HomeActivity.recycler_bottom_quest_view?.let { view ->
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

    private fun doReenterHomeHotPhoto(intent: Intent?) {
        intent ?: return

        val position = intent.getIntExtra(EXTRA_FEED_INFO_POSITION, 0)

        this@HomeActivity.recycler_view_snap_hottopic_pick?.let { view ->
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

    private fun doReenterHomeCategory(intent: Intent?) {
        intent ?: return

        val position = intent.getIntExtra(EXTRA_CATEGORY_POSITION, 0)

        this@HomeActivity.recycler_view_snap_category?.let { view ->
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

    private fun doReenterFromFeed(intent: Intent?) {
        intent ?: return

        val position = intent.getIntExtra(EXTRA_FEED_INFO_POSITION, 0)

        feedFragment.recycler_view?.let { view ->
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

    private fun doReenterFromQuest(intent: Intent?) {
        intent ?: return

        val position = intent.getIntExtra(EXTRA_QUEST_POSITION, 0)

        questFragment.recycler_view?.let { view ->
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


    private fun setBottomNavigationClickListener() {
        this.constraint_holder_navi_home.setOnClickListener { setBottomNavigationBehavior(ID_HOME) }
        this.ib_navigation_home.setOnClickListener{ setBottomNavigationBehavior(ID_HOME) }

        this.constraint_holder_navi_feed.setOnClickListener{ setBottomNavigationBehavior(ID_FEED) }
        this.ib_navigation_feed.setOnClickListener{ setBottomNavigationBehavior(ID_FEED) }

        this.iv_nav_background_center.setOnClickListener{  }
        this.ib_navigation_upload.setOnClickListener{ setBottomNavigationBehavior(ID_UPLOAD) }

        this.constraint_holder_navi_quest.setOnClickListener{ setBottomNavigationBehavior(ID_QUEST) }
        this.ib_navigation_quest.setOnClickListener{ setBottomNavigationBehavior(ID_QUEST) }

        this.constraint_holder_navi_profile.setOnClickListener{ setBottomNavigationBehavior(ID_PROFILE) }
        this.ib_navigation_profile.setOnClickListener{ setBottomNavigationBehavior(ID_PROFILE) }

        setBottomIconColor(itemId)
    }

    internal fun setBottomNavigationBehavior(id: Int) {
        when (id) {
            ID_HOME -> {
                itemId = ID_HOME
                ib_navigation_home.isSelected = true
                this@HomeActivity.toolbar.visibility = View.VISIBLE
                this@HomeActivity.toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.transparent))
                mainFragment = transactFragment(HomeMainFragment::class.java, R.id.home_container) as HomeMainFragment
                setFontTypeface(tv_home_title, FONT_TYPE_BOLD)
                tv_home_title.visibility = View.GONE
            }

            ID_FEED -> {
                itemId = ID_FEED
                this@HomeActivity.toolbar.visibility = View.VISIBLE
                this@HomeActivity.toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary))
                feedFragment = transactFragment(HomeFeedFragment::class.java, R.id.home_container) as HomeFeedFragment
                setFontTypeface(tv_home_title, FONT_TYPE_BOLD)
                tv_home_title.visibility = View.VISIBLE
                tv_home_title.text = getString(R.string.phrase_feed)
            }

            ID_UPLOAD -> { uploadPhotos()}

            ID_QUEST -> {
                itemId = ID_QUEST
                this@HomeActivity.toolbar.visibility = View.VISIBLE
                this@HomeActivity.toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary))
                questFragment = transactFragment(HomeQuestFragment::class.java, R.id.home_container) as HomeQuestFragment
                setFontTypeface(tv_home_title, FONT_TYPE_BOLD)
                tv_home_title.visibility = View.VISIBLE
                tv_home_title.text = getString(R.string.phrase_quest)
            }

            ID_PROFILE -> {
                itemId = ID_PROFILE
                this@HomeActivity.toolbar.visibility = View.GONE
                profileFragment = transactFragment(HomeProfileFragment::class.java, R.id.home_container) as HomeProfileFragment
                setFontTypeface(tv_home_title, FONT_TYPE_BOLD)
                tv_home_title.visibility = View.GONE
            }
        }

        setBottomIconColor(itemId)
    }

    private fun setBottomIconColor(id: Int) {
        this.ib_navigation_home.isSelected = id == ID_HOME
        this.ib_navigation_feed.isSelected = id == ID_FEED
        this.ib_navigation_quest.isSelected = id == ID_QUEST
        this.ib_navigation_profile.isSelected = id == ID_PROFILE
    }


    private fun selectItem(id: Int) {
        when (id) {
            R.id.navigation_home -> {
                this@HomeActivity.toolbar.visibility = View.VISIBLE
                this@HomeActivity.toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.transparent))
                mainFragment = transactFragment(HomeMainFragment::class.java, R.id.home_container) as HomeMainFragment
                setFontTypeface(tv_home_title, FONT_TYPE_BOLD)
                tv_home_title.visibility = View.GONE
            }

            R.id.navigation_feed -> {
                this@HomeActivity.toolbar.visibility = View.VISIBLE
                this@HomeActivity.toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary))
                feedFragment = transactFragment(HomeFeedFragment::class.java, R.id.home_container) as HomeFeedFragment
                setFontTypeface(tv_home_title, FONT_TYPE_BOLD)
                tv_home_title.visibility = View.VISIBLE
                tv_home_title.text = getString(R.string.phrase_feed)
            }

            R.id.navigation_upload -> uploadPhotos()

            R.id.navigation_quest -> {
                this@HomeActivity.toolbar.visibility = View.VISIBLE
                this@HomeActivity.toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary))
                questFragment = transactFragment(HomeQuestFragment::class.java, R.id.home_container) as HomeQuestFragment
                setFontTypeface(tv_home_title, FONT_TYPE_BOLD)
                tv_home_title.visibility = View.VISIBLE
                tv_home_title.text = getString(R.string.phrase_quest)
            }

            R.id.navigation_profile -> {
                this@HomeActivity.toolbar.visibility = View.GONE
                profileFragment = transactFragment(HomeProfileFragment::class.java, R.id.home_container) as HomeProfileFragment
                setFontTypeface(tv_home_title, FONT_TYPE_BOLD)
                tv_home_title.visibility = View.GONE
            }
        }
    }

    internal fun selectItem(menuItem: MenuItem?, id: Int) {
        when (id) {
            R.id.navigation_home -> {
                menuItem?.let {
                    it.isChecked = true
                    closeFab()
                    searchItem.isVisible = true
                    settingItem.isVisible = false
                }

                this@HomeActivity.toolbar.visibility = View.VISIBLE
                this@HomeActivity.toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.transparent))
                mainFragment = transactFragment(HomeMainFragment::class.java, R.id.home_container) as HomeMainFragment
                setFontTypeface(tv_home_title, FONT_TYPE_BOLD)
                tv_home_title.visibility = View.GONE
            }

            R.id.navigation_feed -> {
                menuItem?.let {
                    it.isChecked = true
                    closeFab()
                    searchItem.isVisible = true
                    settingItem.isVisible = false
                }

                this@HomeActivity.toolbar.visibility = View.VISIBLE
                this@HomeActivity.toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary))
                feedFragment = transactFragment(HomeFeedFragment::class.java, R.id.home_container) as HomeFeedFragment
                setFontTypeface(tv_home_title, FONT_TYPE_BOLD)
                tv_home_title.visibility = View.VISIBLE
                tv_home_title.text = getString(R.string.phrase_feed)
            }

            R.id.navigation_upload -> {
                uploadPhotos()
                // menuItem?.let {
                //     it.isChecked = true
                //     if (floatingActionMenu.isOpen) {
                //         floatingActionMenu.close(true)
                //     } else {
                //         floatingActionMenu.open(true)
                //     }
                // }
            }

            R.id.navigation_quest -> {
                menuItem?.let {
                    it.isChecked = true
                    closeFab()
                    searchItem.isVisible = false
                    settingItem.isVisible = false
                }

                this@HomeActivity.toolbar.visibility = View.VISIBLE
                this@HomeActivity.toolbar.background = ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary))
                questFragment = transactFragment(HomeQuestFragment::class.java, R.id.home_container) as HomeQuestFragment
                setFontTypeface(tv_home_title, FONT_TYPE_BOLD)
                tv_home_title.visibility = View.VISIBLE
                tv_home_title.text = getString(R.string.phrase_quest)
            }

            R.id.navigation_profile -> {
                menuItem?.let {
                    it.isChecked = true
                    closeFab()
                    searchItem.isVisible = false
                    settingItem.isVisible = false
                }
                this@HomeActivity.toolbar.visibility = View.GONE
                profileFragment = transactFragment(HomeProfileFragment::class.java, R.id.home_container) as HomeProfileFragment
                setFontTypeface(tv_home_title, FONT_TYPE_BOLD)
                tv_home_title.visibility = View.GONE
            }
        }
    }

    internal fun closeFab() {
        // if (floatingActionMenu.isOpen) {
        //     floatingActionMenu.close(true)
        // }
    }

    internal fun setHomeMainLoadParam(loadType: Int, boundType: Int, calledFrom: Int, id: String) {
        homeViewModel.loadType = loadType
        homeViewModel.boundType = boundType
        homeViewModel.calledFrom = calledFrom
        homeViewModel.setParameters(
            Parameters(
                id,
                -1,
                loadType,
                boundType
            ), NONE_TYPE)

    }

    internal fun showNetworkError(resource: Resource) {
        when(resource.errorCode) {
            in 400..499 -> {
                Snackbar.make(this@HomeActivity.bottom_navigation_view, getString(R.string.phrase_client_wrong_request), LENGTH_LONG).show()
            }

            in 500..599 -> {
                Snackbar.make(this@HomeActivity.bottom_navigation_view, getString(R.string.phrase_server_wrong_response), LENGTH_LONG).show()
            }

            else -> {
                Snackbar.make(this@HomeActivity.bottom_navigation_view, resource.getMessage().toString(), LENGTH_LONG).show()
            }
        }
    }

    internal fun closeFab(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> { }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        closeFab()
                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> {}
                    else -> { }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                closeFab()
            }
        })
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
    internal inline fun launchUIWork(crossinline block: suspend () -> Unit): Job {
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