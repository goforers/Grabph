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

@file:Suppress("DEPRECATION")

package com.goforer.grabph.presentation.ui.photog.fragment

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import android.graphics.Color
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.goforer.base.presentation.utils.CommonUtils.showToastMessage
import com.goforer.base.presentation.view.decoration.RemoverItemDecoration
import com.goforer.base.presentation.view.fragment.RecyclerFragment
import com.goforer.base.presentation.view.helper.RecyclerItemTouchHelperCallback
import com.goforer.grabph.R
import com.goforer.grabph.domain.usecase.Parameters
import com.goforer.grabph.presentation.caller.Caller.CALLED_FROM_PHOTOG_PHOTO
import com.goforer.grabph.presentation.caller.Caller.PHOTOG_PHOTO_FAVORITE_TYPE
import com.goforer.grabph.presentation.caller.Caller.PHOTOG_PHOTO_GENERAL_TYPE
import com.goforer.grabph.presentation.caller.Caller.PHOTOG_PHOTO_POPULAR_TYPE
import com.goforer.grabph.presentation.common.utils.AutoClearedValue
import com.goforer.grabph.presentation.common.utils.handler.CommonWorkHandler
import com.goforer.grabph.presentation.common.utils.handler.watermark.WatermarkHandler
import com.goforer.grabph.presentation.ui.photog.PhotogPhotoActivity
import com.goforer.grabph.presentation.ui.photog.adapter.PhotogPhotoAdapter
import com.goforer.grabph.presentation.vm.BaseViewModel.Companion.NONE_TYPE
import com.goforer.grabph.presentation.vm.feed.photo.FavoritePhotoViewModel
import com.goforer.grabph.presentation.vm.feed.photo.PhotoViewModel
import com.goforer.grabph.presentation.vm.feed.photo.PopularPhotoViewModel
import com.goforer.grabph.repository.model.cache.data.entity.photog.Photo
import com.goforer.grabph.repository.network.response.Resource
import com.goforer.grabph.repository.network.response.Status
import com.goforer.grabph.repository.network.resource.NetworkBoundResource.Companion.BOUND_FROM_LOCAL
import com.goforer.grabph.repository.network.resource.NetworkBoundResource.Companion.LOAD_PHOTOG_PHOTO
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import kotlinx.android.synthetic.main.activity_photog_photo.*
import kotlinx.android.synthetic.main.fragment_photog_photo.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import javax.inject.Inject

class PhotogPhotoFragment: RecyclerFragment<Photo>() {
    internal val photogPhotoActivity: PhotogPhotoActivity by lazy {
        activity as PhotogPhotoActivity
    }

    private lateinit var adapter: PhotogPhotoAdapter

    private lateinit var acvAdapter: AutoClearedValue<PhotogPhotoAdapter>

    private lateinit var userID: String

    private var pages: Int = 0
    private var page: Int = 0
    private var tryingCount: Int = 0

    private val job = Job()

    private val ioScope = CoroutineScope(Dispatchers.IO + job)

    @field:Inject
    lateinit var photoViewModel: PhotoViewModel
    @field:Inject
    lateinit var popularPhotoViewModel: PopularPhotoViewModel
    @field:Inject
    lateinit var favoritePhotoViewModel: FavoritePhotoViewModel

    @field:Inject
    lateinit var workHandler: CommonWorkHandler
    @field:Inject
    lateinit var waterMarkHandler: WatermarkHandler

    companion object {
        private lateinit var photogPhotos: List<Photo>
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val acvView = AutoClearedValue(this,
                inflater.inflate(R.layout.fragment_photog_photo, container, false))

        return acvView.get()?.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userID = photogPhotoActivity.searperID
        pages = photogPhotoActivity.pages

        page = 1

        when(photogPhotoActivity.type) {
            PHOTOG_PHOTO_GENERAL_TYPE -> {
                // The cache should be removed whenever App is started again and then
                // the data are fetched from the Back-end.
                // The Cache has to be light-weight.
                removePhotoCache(photogPhotoActivity.type)
            }

            PHOTOG_PHOTO_POPULAR_TYPE -> {
                // The cache should be removed whenever App is started again and then
                // the data are fetched from the Back-end.
                // The Cache has to be light-weight.
                removePhotoCache(photogPhotoActivity.type)
            }

            PHOTOG_PHOTO_FAVORITE_TYPE -> {
                // The cache should be removed whenever App is started again and then
                // the data are fetched from the Back-end.
                // The Cache has to be light-weight.
                removePhotoCache(photogPhotoActivity.type)
            }
        }

        refresh(true)
        setItemHasFixedSize(true)

        totalPage = pages
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
            }

            override fun onScrolling(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("onScrolling")
            }

            override fun onScrollIdle(position: Int) {}

            override fun onScrollSetting() {

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Timber.i("onScrolled")
            }

            override fun onError(message: String) {
                showToastMessage(baseActivity, message, Toast.LENGTH_SHORT)
            }
        })

        this@PhotogPhotoFragment.recycler_view.setHasFixedSize(true)
        this@PhotogPhotoFragment.recycler_view.setItemViewCacheSize(20)
        this@PhotogPhotoFragment.recycler_view.isDrawingCacheEnabled = true
        this@PhotogPhotoFragment.recycler_view.isNestedScrollingEnabled = false
        this@PhotogPhotoFragment.recycler_view.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        this@PhotogPhotoFragment.recycler_view.isVerticalScrollBarEnabled = false

        return LinearLayoutManager(this.context.applicationContext, RecyclerView.VERTICAL, false)
    }

    override fun createAdapter(): RecyclerView.Adapter<*> {
        adapter = PhotogPhotoAdapter(this, waterMarkHandler, workHandler)
        acvAdapter = AutoClearedValue(this, adapter)
        this@PhotogPhotoFragment.recycler_view.adapter = acvAdapter.get()

        return adapter
    }

    override fun createItemDecoration(): RecyclerView.ItemDecoration {
        return RemoverItemDecoration(Color.TRANSPARENT)
    }

    override fun createItemTouchHelper(): ItemTouchHelper.Callback {
        return RecyclerItemTouchHelperCallback(this.context.applicationContext, adapter, Color.BLUE)
    }

    override fun isItemDecorationVisible(): Boolean {
        return true
    }

    override fun requestData(isNew: Boolean) {
        getPhotos(userID, page, LOAD_PHOTOG_PHOTO, BOUND_FROM_LOCAL,
                CALLED_FROM_PHOTOG_PHOTO, photogPhotoActivity.type)

        Timber.i("requestData")
    }

    override fun updateData() {
        stopLoading(STOP_REFRESHING_PROMPT_TIMEOUT)

        Timber.i("updateData")
    }

    override fun reachToLastPage() {}

    override fun onSorted(items: List<Photo>) {

    }

    override fun onFirstVisibleItem(position: Int) {
        // To Do::Implement playing the video file with position in case of video item
        Timber.i("onFirstVisibleItem : $position")
    }

    override fun onLastVisibleItem(position: Int) {
        // To Do::Implement playing the video file with position in case of video item
        Timber.i("onLastVisibleItem : $position")
    }

    private fun getPhotos(userID: String, page: Int, loadType: Int, boundType: Int, calledFrom: Int, type: Int) {
        get(userID, page, loadType, boundType, calledFrom, type)
    }

    private operator fun get(userID: String, page: Int, loadType: Int, boundType: Int,
                             calledFrom: Int, type: Int) {
        setLoadParam(loadType, boundType, userID, page, calledFrom, type)

        val liveData: LiveData<Resource>? = when (photogPhotoActivity.type) {
            PHOTOG_PHOTO_GENERAL_TYPE -> {
                photoViewModel.photos
            }
            PHOTOG_PHOTO_POPULAR_TYPE -> {
                popularPhotoViewModel.photos
            }
            PHOTOG_PHOTO_FAVORITE_TYPE -> {
                favoritePhotoViewModel.photos
            }
            else -> {
                photoViewModel.photos
            }
        }

        liveData?.observe(this, Observer { resource ->
            when(resource?.getStatus()) {
                Status.SUCCESS -> {
                    resource.getData()?.let { list ->
                        this@PhotogPhotoFragment.swipe_layout.visibility = View.VISIBLE
                        @Suppress("UNCHECKED_CAST")
                        val photos = list as? PagedList<Photo>?
                        if (photos!!.size > 0) {
                            acvAdapter.get()?.submitList(photos)
                            photogPhotos = photos
                        } else {
                            if (tryingCount >= 2) {
                                showMessage(type)
                                tryingCount = 0
                                liveData.removeObservers(this)

                            } else {
                                getPhotos(userID, pages, LOAD_PHOTOG_PHOTO, BOUND_FROM_LOCAL, CALLED_FROM_PHOTOG_PHOTO, type)
                                tryingCount++
                            }
                        }

                        resource.getMessage()?.let {
                            stopPhoto(resource)
                        }
                    }
                }

                Status.LOADING -> {
                }

                Status.ERROR -> {
                    stopPhoto(resource)
                }

                else -> {
                    stopPhoto(resource)
                }
            }
        })
    }

    private fun showMessage(type: Int) {
        when(type) {
            PHOTOG_PHOTO_GENERAL_TYPE -> {
                showToastMessage(photogPhotoActivity, getString(R.string.phrase_no_general_photo), Toast.LENGTH_SHORT)
            }
            PHOTOG_PHOTO_POPULAR_TYPE -> {
                showToastMessage(photogPhotoActivity, getString(R.string.phrase_no_popular_photo), Toast.LENGTH_SHORT)
            }
            PHOTOG_PHOTO_FAVORITE_TYPE -> {
                showToastMessage(photogPhotoActivity, getString(R.string.phrase_no_favorite_photo), Toast.LENGTH_SHORT)
            }
        }

        stopMoreLoading(STOP_REFRESHING_TIMEOUT)
        this@PhotogPhotoFragment.swipe_layout.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun stopPhoto(resource: Resource) {
        stopMoreLoading(STOP_REFRESHING_TIMEOUT)
        when(resource.errorCode) {
            in 400..499 -> {
                Snackbar.make(photogPhotoActivity.coordinator_photog_photo_layout, getString(R.string.phrase_client_wrong_request), LENGTH_LONG).show()
            }

            in 500..599 -> {
                Snackbar.make(photogPhotoActivity.coordinator_photog_photo_layout, getString(R.string.phrase_server_wrong_response), LENGTH_LONG).show()
            }

            else -> {
                Snackbar.make(photogPhotoActivity.coordinator_photog_photo_layout, resource.getMessage().toString(), LENGTH_LONG).show()
            }
        }
    }

    private fun setLoadParam(loadType: Int, boundType: Int, userID: String, pages: Int, calledFrom: Int, type: Int) {
        when (type) {
            PHOTOG_PHOTO_GENERAL_TYPE -> {
                photoViewModel.setParameters(Parameters(userID, pages, loadType, boundType), NONE_TYPE)
            }
            PHOTOG_PHOTO_POPULAR_TYPE -> {
                popularPhotoViewModel.setParameters(Parameters(userID, pages, loadType, boundType), NONE_TYPE)
            }
            PHOTOG_PHOTO_FAVORITE_TYPE -> {
                favoritePhotoViewModel.setParameters(Parameters(userID, pages, loadType, boundType), NONE_TYPE)
            }
        }
    }

    private fun removePhotoCache(type: Int) = launchWork {
        when(type) {
            PHOTOG_PHOTO_GENERAL_TYPE -> {
                photoViewModel.removePhotos()
            }

            PHOTOG_PHOTO_POPULAR_TYPE -> {
                popularPhotoViewModel.removePhotos()
            }

            PHOTOG_PHOTO_FAVORITE_TYPE -> {
                favoritePhotoViewModel.removePhotos()
            }
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
    internal inline fun launchWork(crossinline block: suspend () -> Unit): Job {
        return ioScope.launch {
            block()
        }
    }
}
