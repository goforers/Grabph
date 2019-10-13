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

package com.goforer.grabph.presentation.ui.categoryphoto

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.transition.Transition
import android.view.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elmargomez.typer.Font
import com.elmargomez.typer.Typer
import com.goforer.base.annotation.RunWithMockData
import com.goforer.base.annotation.MockData
import com.goforer.base.presentation.utils.CommonUtils
import com.goforer.base.presentation.utils.CommonUtils.withDelay
import com.goforer.base.presentation.view.activity.BaseActivity
import com.goforer.base.presentation.view.customs.listener.OnSwipeOutListener
import com.goforer.base.presentation.view.decoration.GapItemDecoration
import com.goforer.grabph.R
import com.goforer.grabph.domain.usecase.Parameters
import com.goforer.grabph.presentation.caller.Caller
import com.goforer.grabph.presentation.common.effect.transition.TransitionCallback
import com.goforer.grabph.presentation.common.effect.transition.TransitionObject
import com.goforer.grabph.presentation.common.menu.MenuHandler
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import com.goforer.grabph.presentation.common.utils.handler.watermark.WatermarkHandler
import com.goforer.grabph.presentation.ui.categoryphoto.adapter.CategoryPhotoAdapter
import com.goforer.grabph.presentation.ui.categoryphoto.sharedElementCallback.CategoryPhotoListCallback
import com.goforer.grabph.presentation.vm.category.photo.CPhotoViewModel
import com.goforer.grabph.presentation.ui.home.HomeActivity
import com.goforer.grabph.repository.model.cache.data.entity.category.CPhoto
import com.goforer.grabph.repository.model.cache.data.mock.datasource.categoryphotos.PhotoCategoryListDataSource
import com.goforer.grabph.repository.network.resource.NetworkBoundResource
import com.goforer.grabph.repository.network.response.Resource
import com.goforer.grabph.repository.network.response.Status
import com.goforer.grabph.repository.interactor.remote.Repository
import com.goforer.grabph.repository.network.resource.NetworkBoundResource.Companion.BOUND_FROM_BACKEND
import com.goforer.grabph.repository.network.resource.NetworkBoundResource.Companion.LOAD_CPHOTO_UPDATE
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import kotlinx.android.synthetic.main.activity_category_photo.*
import kotlinx.android.synthetic.main.activity_category_photo.swipe_layout
import kotlinx.android.synthetic.main.activity_category_photo.toolbar
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.abs
import kotlin.reflect.full.findAnnotation

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@SuppressLint("Registered")
@RunWithMockData(true)
class CategoryPhotoActivity: BaseActivity() {
    private lateinit var sharedElementCallback: CategoryPhotoListCallback

    private lateinit var gridLayoutManager: StaggeredGridLayoutManager

    private lateinit var adapter: CategoryPhotoAdapter

    private val mock = this::class.findAnnotation<RunWithMockData>()?.mock!!

    private var isAppBarLayoutExpanded = false
    private var isAppBarLayoutCollapsed = false

    private var job = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + job)
    private val ioScope = CoroutineScope(Dispatchers.IO + job)

    private lateinit var categoryID: String
    private lateinit var categoryImage: String
    private lateinit var categoryTitle: String

    private var page: Int = 0
    private var position: Int = 0
    private var pages: Int = 0
    private var type: Int = 0

    @field:Inject
    lateinit var photoViewModel: CPhotoViewModel

    @field:Inject
    lateinit var workHandler: CommonWorkHandler

    @field:Inject
    lateinit var watermarkHandler: WatermarkHandler

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
        private const val DELAY_COLLAPSED_TIMER_INTERVAL = 300
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
        if (!isNetworkAvailable) {
            this@CategoryPhotoActivity.iv_category_photo_disconnect.visibility = View.VISIBLE
            this@CategoryPhotoActivity.tv_category_photo_notice1.visibility = View.VISIBLE
            this@CategoryPhotoActivity.tv_category_photo_notice2.visibility = View.VISIBLE
        }
        */

        val font = Typer.set(this.applicationContext).getFont(Font.ROBOTO_MEDIUM)
        this@CategoryPhotoActivity.collapsing_category_photo_layout.setCollapsedTitleTypeface(font)
        this@CategoryPhotoActivity.collapsing_category_photo_layout.setExpandedTitleTypeface(font)

        page = 1

        this@CategoryPhotoActivity.swipe_layout?.setOnRefreshListener {
            this@CategoryPhotoActivity.swipe_layout?.isRefreshing = false
            if (!isAppBarLayoutCollapsed && isAppBarLayoutExpanded) {
                this@CategoryPhotoActivity.swipe_layout?.isRefreshing = true
                updateData()
            }
        }

        this@CategoryPhotoActivity.category_recycler_view.setHasFixedSize(true)
        this@CategoryPhotoActivity.category_recycler_view.setHasFixedSize(true)
        this@CategoryPhotoActivity.category_recycler_view.setItemViewCacheSize(40)
        this@CategoryPhotoActivity.category_recycler_view.isVerticalScrollBarEnabled = false
        gridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        gridLayoutManager.isItemPrefetchEnabled = true

        this@CategoryPhotoActivity.category_recycler_view?.addItemDecoration(
                object : GapItemDecoration(VERTICAL_LIST, resources.getDimensionPixelSize(R.dimen.space_4)) {
                    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                                state: RecyclerView.State) {
                        outRect.left = 2
                        outRect.right = 2
                        outRect.bottom = gap

                        // Add top margin only for the first item to avoid double space between items
                        if (parent.getChildAdapterPosition(view) == 0
                                || parent.getChildAdapterPosition(view) == 1) {
                            outRect.top = gap
                        }
                    }
                }
        )

        adapter = CategoryPhotoAdapter(this)
        this@CategoryPhotoActivity.category_recycler_view.adapter = adapter
        this@CategoryPhotoActivity.category_recycler_view.layoutManager = gridLayoutManager
        getCategoryPhoto(categoryID, page, NetworkBoundResource.LOAD_CPHOTOG_PHOTO,
                Repository.BOUND_FROM_LOCAL, Caller.CALLED_FROM_CATEGORY_PHOTO)

        this@CategoryPhotoActivity.appbar_category_photo_layout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener {
            appBarLayout, verticalOffset ->
            this@CategoryPhotoActivity.collapsing_category_photo_layout.title = categoryTitle
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
                        this@CategoryPhotoActivity.swipe_layout?.isEnabled = true
                    }
                }
                else -> {
                    this@CategoryPhotoActivity.swipe_layout?.isEnabled = false
                    isAppBarLayoutExpanded = false
                    isAppBarLayoutCollapsed = true
                }
            }
        })

        this@CategoryPhotoActivity.coordinator_category_photo_layout.setOnSwipeOutListener(this, object : OnSwipeOutListener {
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
                    this@CategoryPhotoActivity.swipe_layout?.isEnabled = true
                }
            }

            override fun onSwipeUp(x: Float, y: Float) {
                Timber.d("onSwipeUp")
            }

            override fun onSwipeDone() {
                Timber.d("onSwipeDone")
            }
        })
    }

    override fun onBackPressed() {
        setActivityResult()

        super.onBackPressed()
    }

    override fun finishAfterTransition() {
        this@CategoryPhotoActivity.collapsing_category_photo_layout.title = ""
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

    override fun setContentView() {
        setContentView(R.layout.activity_category_photo)
    }

    @SuppressLint("RestrictedApi")
    override fun setActionBar() {
        setSupportActionBar(this@CategoryPhotoActivity.toolbar)

        val actionBar = supportActionBar

        actionBar?.let {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.displayOptions = ActionBar.DISPLAY_SHOW_HOME or ActionBar.DISPLAY_USE_LOGO
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.elevation = 0f
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
        }

        this@CategoryPhotoActivity.toolbar.setNavigationOnClickListener{
            finishAfterTransition()
        }

        this@CategoryPhotoActivity.toolbar.hideOverflowMenu()
    }

    override fun setViews(savedInstanceState: Bundle?) {
        savedInstanceState ?: getIntentData()
        /*
        if (!isNetworkAvailable) {
            this@CategoryPhotoActivity.iv_category_photo_disconnect.visibility = View.VISIBLE
            this@CategoryPhotoActivity.tv_category_photo_notice1.visibility = View.VISIBLE
            this@CategoryPhotoActivity.tv_category_photo_notice2.visibility = View.VISIBLE
        }
        */

        this@CategoryPhotoActivity.swipe_layout?.isRefreshing = false
        this@CategoryPhotoActivity.swipe_layout?.isEnabled = false

        // The cache should be removed whenever App is started again and then
        // the data are fetched from the Back-end.
        // The Cache has to be light-weight.
        launchIOWork { photoViewModel.removePhotos() }
    }

    override fun onActivityReenter(resultCode: Int, data: Intent) {
        super.onActivityReenter(resultCode, data)

        // Listener to reset shared element exit transition callbacks.
        window.sharedElementExitTransition.addListener(sharedExitListener)
        supportPostponeEnterTransition()
        doReenter(data)
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
                val sharePopup = PopupMenu(wrapper, findViewById(R.id.action_photo_share),
                        Gravity.CENTER)
                sharePopup.menuInflater.inflate(R.menu.menu_share_popup, sharePopup.menu)
                MenuHandler().applyFontToMenuItem(sharePopup, Typeface.createFromAsset(applicationContext?.assets, NOTO_SANS_KR_MEDIUM),
                        resources.getColor(R.color.colorHomeQuestFavoriteKeyword, theme))
                sharePopup.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_share_facebook ->
                            callShareToFacebook(this@CategoryPhotoActivity.iv_category_photo.drawable as BitmapDrawable,
                                    this.applicationContext.getString(R.string.phrase_title) + "\n\n"
                                            + categoryTitle)
                        R.id.menu_share_ect -> {
                            watermarkHandler.putWatermark(this.applicationContext, workHandler,
                                    (this@CategoryPhotoActivity.iv_category_photo.drawable as BitmapDrawable).bitmap, categoryTitle, "")
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

        outState.putString(Caller.EXTRA_HOME_CATEGORY_IMAGE, categoryImage)
        outState.putString(Caller.EXTRA_HOME_CATEGORY_ID, categoryID)
        outState.putString(Caller.EXTRA_HOME_CATEGORY_TITLE, categoryTitle)
        outState.putInt(Caller.EXTRA_PAGES, pages)
        outState.putInt(Caller.EXTRA_CATEGORY_PHOTO_TYPE, type)
        outState.putInt(Caller.EXTRA_CATEGORY_POSITION, position)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        categoryImage = savedInstanceState.getString(Caller.EXTRA_HOME_CATEGORY_IMAGE, "")
        categoryID = savedInstanceState.getString(Caller.EXTRA_HOME_CATEGORY_ID, "")
        categoryTitle = savedInstanceState.getString(Caller.EXTRA_HOME_CATEGORY_TITLE, "")
        pages = savedInstanceState.getInt(Caller.EXTRA_PAGES, -1)
        type = savedInstanceState.getInt(Caller.EXTRA_CATEGORY_PHOTO_TYPE, -1)
        position = savedInstanceState.getInt(Caller.EXTRA_CATEGORY_POSITION, -1)
    }

    private fun getIntentData() {
        categoryImage = intent.getStringExtra(Caller.EXTRA_HOME_CATEGORY_IMAGE)
        categoryID = intent.getStringExtra(Caller.EXTRA_HOME_CATEGORY_ID)
        categoryTitle = intent.getStringExtra(Caller.EXTRA_HOME_CATEGORY_TITLE)
        pages = intent.getIntExtra(Caller.EXTRA_PAGES, -1)
        type = intent.getIntExtra(Caller.EXTRA_CATEGORY_PHOTO_TYPE, -1)
        position = intent.getIntExtra(Caller.EXTRA_CATEGORY_POSITION, -1)
    }

    private fun doReenter(intent: Intent?) {
        intent ?: return
        this@CategoryPhotoActivity.category_recycler_view?.let { recyclerView ->
            val position = intent.getIntExtra(Caller.EXTRA_CATEGORY_PHOTO_INFO_SELECTED_ITEM_POSITION, 0)
            CommonUtils.betterSmoothScrollToPosition(recyclerView, position)

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

    private fun callShareToFacebook(drawable: BitmapDrawable, caption: String) {
        workHandler.shareToFacebook(drawable.bitmap, this)
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

    private fun updateData() {
        /*
         * Please put some module to update new data here, instead of doneRefreshing() method if
         * there is some data to be updated from the backend side.
         * I just put doneRefreshing() method because there is no data to be updated from
         * the backend side in this app-architecture project.
         */

        withDelay(500L) {
            this@CategoryPhotoActivity.swipe_layout?.let { swipe ->
                if (swipe.isRefreshing) {
                    swipe.isRefreshing=false
                }
            }
        }

        photoViewModel.setParameters(Parameters(categoryID, page, LOAD_CPHOTO_UPDATE, BOUND_FROM_BACKEND), -1)
        Timber.i("updateData")
    }

    private fun getCategoryPhoto(categoryID: String, page: Int, loadType: Int, boundType: Int, calledFrom: Int) {
        get(categoryID, page, loadType, boundType, calledFrom)
    }

    private operator fun get(categoryID: String, page: Int, loadType: Int, boundType: Int, calledFrom: Int) = when(mock) {
        @MockData
        true -> transactMockData()
        false -> transactRealData(categoryID, page, loadType, boundType, calledFrom)
    }

    @MockData
    private fun transactMockData() {
        val config = PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(10)
                .setPrefetchDistance(5)
                .build()

        LivePagedListBuilder(object : DataSource.Factory<Int, CPhoto>() {
            override fun create(): DataSource<Int, CPhoto> {
                return PhotoCategoryListDataSource(type)
            }
        }, config).build().observe(this, Observer {
            window.sharedElementEnterTransition.addListener(sharedEnterListener)
            supportPostponeEnterTransition()
            adapter.submitList(it!!)
            setCategoryImage(categoryImage)
            withDelay(500L) {
                this@CategoryPhotoActivity.swipe_layout?.let { swipe ->
                    if (swipe.isRefreshing) {
                        swipe.isRefreshing = false
                    }
                }
            }
        })
    }

    private fun transactRealData(categoryID: String, page: Int, loadType: Int, boundType: Int, calledFrom: Int) {
        photoViewModel.setParameters(Parameters(categoryID, page, loadType, boundType), -1)
        window.sharedElementEnterTransition.addListener(sharedEnterListener)
        supportPostponeEnterTransition()
        photoViewModel.categoryPhoto.observe(this, Observer { resource ->
            when(resource?.getStatus()) {
                Status.SUCCESS -> {
                    resource.getData() ?: return@Observer
                    //this@CategoryPhotoActivity.swipe_layout.visibility = View.VISIBLE
                    @Suppress("UNCHECKED_CAST")
                    val photos = resource.getData() as? PagedList<CPhoto>?
                    when {
                        photos?.size!! > 0 -> {
                            adapter.submitList(photos)
                            setCategoryImage(categoryImage)
                        }

                        resource.getMessage() != null -> {
                            withDelay(500L) {
                                this@CategoryPhotoActivity.swipe_layout?.let {
                                    if (it.isRefreshing) {
                                        it.isRefreshing = false
                                    }
                                }
                            }

                            stopCategoryPhoto(resource)
                        }
                    }
                }

                Status.LOADING -> {
                }

                Status.ERROR -> {
                    withDelay(500L) {
                        this@CategoryPhotoActivity.swipe_layout?.let {
                            if (it.isRefreshing) {
                                it.isRefreshing = false
                            }
                        }
                    }

                    stopCategoryPhoto(resource)
                }

                else -> {
                    withDelay(500L) {
                        this@CategoryPhotoActivity.swipe_layout?.let {
                            if (it.isRefreshing) {
                                it.isRefreshing = false
                            }
                        }
                    }

                    stopCategoryPhoto(resource)
                }
            }
        })
    }

    private fun setCategoryImage(image: String) {
        this@CategoryPhotoActivity.collapsing_category_photo_layout.title = categoryTitle
        setFixedImageSize(0, 0)
        setImageDraw(this@CategoryPhotoActivity.iv_category_photo, this@CategoryPhotoActivity.backdrop_category_photo_container,
                     image, true)
        withDelay(50L) {
            this@CategoryPhotoActivity.iv_category_photo.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + position
            sharedElementCallback = CategoryPhotoListCallback(intent)
            sharedElementCallback.setViewBinding(this@CategoryPhotoActivity.iv_category_photo)
            setEnterSharedElementCallback(sharedElementCallback)
            supportStartPostponedEnterTransition()
        }

        this@CategoryPhotoActivity.appbar_category_photo_layout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener {
            _, verticalOffset ->
            if (this@CategoryPhotoActivity.collapsing_category_photo_layout.height + verticalOffset < 2
                    * ViewCompat.getMinimumHeight(this@CategoryPhotoActivity.collapsing_category_photo_layout)) {
                // collapsed
                this@CategoryPhotoActivity.iv_category_photo.animate().alpha(1.0f).duration = 600
            } else {
                // extended
                this@CategoryPhotoActivity.iv_category_photo.animate().alpha(1.0f).duration = 1000    // 1.0f means opaque
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun stopCategoryPhoto(resource: Resource) = when(resource.errorCode) {
        in 400..499 -> {
            Snackbar.make(coordinator_category_photo_layout, getString(R.string.phrase_client_wrong_request), LENGTH_LONG).show()
        }

        in 500..599 -> {
            Snackbar.make(coordinator_category_photo_layout, getString(R.string.phrase_server_wrong_response), LENGTH_LONG).show()
        }

        else -> {
            Snackbar.make(coordinator_category_photo_layout, resource.getMessage().toString(), LENGTH_LONG).show()
        }
    }

    private fun setItemsClear() {
        val pagedList = adapter.currentList
        pagedList?.clear()

        adapter.notifyDataSetChanged()
    }

    private fun setActivityResult() {
        val intent = Intent(this, HomeActivity::class.java)

        intent.putExtra(Caller.EXTRA_CATEGORY_POSITION, position)
        setResult(Caller.SELECTED_BEST_PICK_CATEGORY_POSITION, intent)
    }

    private fun setViewBind() {
        this@CategoryPhotoActivity.iv_category_photo.transitionName = TransitionObject.TRANSITION_NAME_FOR_IMAGE + position
        sharedElementCallback = CategoryPhotoListCallback(intent)
        sharedElementCallback.setViewBinding(this@CategoryPhotoActivity.iv_category_photo)
    }
}