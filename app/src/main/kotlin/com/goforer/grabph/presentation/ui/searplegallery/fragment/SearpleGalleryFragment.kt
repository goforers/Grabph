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

package com.goforer.grabph.presentation.ui.searplegallery.fragment

import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.goforer.base.presentation.utils.CommonUtils
import com.goforer.base.presentation.utils.SharedPreference
import com.goforer.base.presentation.view.decoration.GapItemDecoration
import com.goforer.base.presentation.view.fragment.RecyclerFragment
import com.goforer.base.presentation.view.helper.RecyclerItemTouchHelperCallback
import com.goforer.grabph.R
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.event.action.DeletePhotoAction
import com.goforer.grabph.presentation.event.action.UpdatePhotoListAction
import com.goforer.grabph.presentation.vm.feed.photo.LocalSavedPhotoViewModel
import com.goforer.grabph.presentation.ui.searplegallery.SearpleGalleryActivity
import com.goforer.grabph.presentation.ui.searplegallery.adapter.SearpleGalleryAdapter
import com.goforer.grabph.presentation.ui.searplegallery.SavedPhoto
import com.goforer.grabph.data.repository.remote.Repository
import kotlinx.android.synthetic.main.fragment_searple_gallery.*
import kotlinx.android.synthetic.main.recycler_view_container.*
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import javax.inject.Inject

class SearpleGalleryFragment: RecyclerFragment<String>() {
    private lateinit var gridLayoutManager: StaggeredGridLayoutManager

    internal val searpleGalleryActivity: SearpleGalleryActivity by lazy {
        activity as SearpleGalleryActivity
    }

    private val job = Job()

    private val defaultScope = CoroutineScope(Dispatchers.Default + job)

    @field:Inject
    lateinit var localSavedPhotoViewModel: LocalSavedPhotoViewModel

    private lateinit var adapter: SearpleGalleryAdapter
    private lateinit var acvAdapter: AutoClearedValue<SearpleGalleryAdapter>

    private var isUpdated = false
    private var isDeleted = false
    private var isOverFirst = false

    private lateinit var fileName: String

    private var deletedPosition: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        val acvView = AutoClearedValue(this,
                inflater.inflate(R.layout.fragment_searple_gallery, container, false))

        return acvView.get()?.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isUpdated = false
        isDeleted = false
        refresh(true)
        setItemHasFixedSize(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        EventBus.getDefault().unregister(this)
        job.cancel()
    }

    override fun createLayoutManager(): RecyclerView.LayoutManager {
        super.setOnProcessListener(object : OnProcessListener {
            override fun onScrolledToLast(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("onScrolledToLast")

                this@SearpleGalleryFragment.recycler_view.isNestedScrollingEnabled = false
            }

            override fun onScrolling(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("onScrolling")

                this@SearpleGalleryFragment.recycler_view.isNestedScrollingEnabled = true
            }

            override fun onScrollIdle(position: Int) {
                this@SearpleGalleryFragment.recycler_view.isNestedScrollingEnabled = false
                if (position >= SearpleGalleryActivity.VISIBLE_UPTO_ITEMS) {
                    isOverFirst = true
                    this@SearpleGalleryFragment.fam_searple_gallery_top.visibility = View.VISIBLE
                    this@SearpleGalleryFragment.fam_searple_gallery_top.show(true)
                }
            }

            override fun onScrollSetting() {

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("onScrolled")

                gridLayoutManager.invalidateSpanAssignments()
            }

            override fun onError(message: String) {
                CommonUtils.showToastMessage(baseActivity, message, Toast.LENGTH_SHORT)
            }
        })

        this@SearpleGalleryFragment.recycler_view.setHasFixedSize(true)
        this@SearpleGalleryFragment.recycler_view.setItemViewCacheSize(30)
        this@SearpleGalleryFragment.recycler_view.setItemViewCacheSize(Repository.PER_PAGE)
        this@SearpleGalleryFragment.recycler_view.isNestedScrollingEnabled = false
        gridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        gridLayoutManager.isItemPrefetchEnabled = true

        // Used StaggeredGridLayoutManager to show the photos instead GridLayoutManager
        /*
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mActivity, SPAN_COUNT,
                GridLayoutManager.VERTICAL, false);
        StaggeredGridLayoutManager.s.SpanSizeLookup spanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == mAdapter.getItems().size()) {
                    return 6;
                } else if (mFeedItems.get(position).getWidth() >= 240 &&
                        mFeedItems.get(position).getWidth() > mFeedItems.get(position).getHeight()) {
                    return 3;
                } else if (mFeedItems.get(position).getHeight() <= 200 &&
                        mFeedItems.get(position).getHeight() > mFeedItems.get(position).getWidth()) {
                    return 2;
                } else {
                    return 2;
                }
            }
        };

        spanSizeLookup.setSpanIndexCacheEnabled(true);
        gridLayoutManager.setSpanSizeLookup(spanSizeLookup);
        */
        return gridLayoutManager
    }

    override fun createItemDecoration(): RecyclerView.ItemDecoration {
        return object : GapItemDecoration(VERTICAL_LIST, resources.getDimensionPixelSize(R.dimen.space_1)) {
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

    override fun createAdapter(): RecyclerView.Adapter<*> {
        adapter = SearpleGalleryAdapter(this)
        acvAdapter = AutoClearedValue(this, adapter)
        this@SearpleGalleryFragment.recycler_view.adapter = adapter

        return adapter
    }

    override fun createItemTouchHelper(): ItemTouchHelper.Callback {
        return RecyclerItemTouchHelperCallback(this.context.applicationContext, adapter, Color.BLUE)
    }

    override fun isItemDecorationVisible(): Boolean {
        return true
    }

    override fun requestData(isNew: Boolean) {
        getPhotoId()
        getFileName()
    }

    override fun updateData() {
        stopLoading(STOP_REFRESHING_TIMEOUT)
    }

    override fun reachToLastPage() {}

    override fun onSorted(items: List<String>) {}

    override fun onFirstVisibleItem(position: Int) {}

    override fun onLastVisibleItem(position: Int) {}

    private fun getFileName() = localSavedPhotoViewModel.photoFileNames.observe(this, Observer {
        it ?: stopRefreshing(true)
        this@SearpleGalleryFragment.swipe_layout.visibility = View.VISIBLE
        acvAdapter.get()?.submitList(it)
        setPhotoFileNames(it)
        val itemPosition = SharedPreference.getSharedPreferenceItemPosition(
                this@SearpleGalleryFragment.searpleGalleryActivity.applicationContext)

        if (itemPosition > 0) {
            CommonUtils.betterSmoothScrollToPosition(this@SearpleGalleryFragment.recycler_view,
                    itemPosition)
        }
    })

    private fun getPhotoId() = localSavedPhotoViewModel.photoIds.observe(this, Observer {
        it ?: stopRefreshing(true)
        this@SearpleGalleryFragment.swipe_layout.visibility = View.VISIBLE
        setPhotoIds(it)
        val itemPosition = SharedPreference.getSharedPreferenceItemPosition(
                this@SearpleGalleryFragment.searpleGalleryActivity.applicationContext)

        if (itemPosition > 0) {
            CommonUtils.betterSmoothScrollToPosition(this@SearpleGalleryFragment.recycler_view,
                    itemPosition)
        }
    })

    private fun setPhotoFileNames(listSavedPhoto: PagedList<String>?) {
        launchWork {
            SavedPhoto.setPhotoFileNames(listSavedPhoto as List<String>)
        }
    }

    private fun setPhotoIds(listPhotoId: PagedList<String>?) {
        launchWork {
            SavedPhoto.setPhotoIds(listPhotoId as List<String>)
        }
    }

    private fun launchUpdatePhoto(action: UpdatePhotoListAction) = launchWork {
        updatePhoto(action)
    }

    private suspend fun updatePhoto(action: UpdatePhotoListAction) = withContext(Dispatchers.Default) {
        val context = context.applicationContext
        context?.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(action.pictureFile)))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(action.pictureFile)
            scanIntent.data = contentUri
            context?.sendBroadcast(scanIntent)
        } else {
            context?.sendBroadcast(Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())))
        }
    }

    private fun launchDeletePhoto(action: DeletePhotoAction) = launchWork {
        deletePhoto(action)
    }

    private suspend fun deletePhoto(action: DeletePhotoAction) = withContext(Dispatchers.Default) {
        val context = context.applicationContext
        MediaScannerConnection.scanFile(context, arrayOf(action.path), null) {
            path1, uri ->
            Timber.i("ExternalStorage", "Scanned $path1:")
            Timber.i("ExternalStorage", "-> uri=$uri")
        }
    }

    /**
     * Helper function to call something doing function
     *
     * By marking `block` as `suspend` this creates a suspend lambda which can call suspend
     * functions.
     *
     * @param block lambda to actually do some work. It is called in the defaultScope.
     *              lambda the some work will do
     */
    internal inline fun launchWork(crossinline block: suspend () -> Unit): Job {
        return defaultScope.launch {
            block()
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onAction(action: UpdatePhotoListAction) {
        isDeleted = true
        fileName = action.fileName
        launchUpdatePhoto(action)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onAction(action: DeletePhotoAction) {
        isDeleted = true
        deletedPosition = action.position
        launchDeletePhoto(action)
    }
}
