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

package com.goforer.grabph.data.datasource.network.resource

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import com.goforer.base.presentation.model.BaseModel
import com.goforer.base.presentation.utils.CommonUtils
import com.goforer.grabph.data.datasource.model.cache.data.entity.comments.PhotoComments
import com.goforer.grabph.data.datasource.model.cache.data.entity.exif.PhotoEXIF
import com.goforer.grabph.data.datasource.model.cache.data.entity.feed.FlickrFeed
import com.goforer.grabph.data.datasource.model.cache.data.entity.home.Homeg
import com.goforer.grabph.data.datasource.model.cache.data.entity.location.PhotoGEO
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.info.QuestsInfog
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.Photog
import com.goforer.grabph.data.datasource.model.cache.data.entity.photoinfo.PhotoInfo
import com.goforer.grabph.data.datasource.model.cache.data.entity.category.Categoryg
import com.goforer.grabph.data.datasource.model.cache.data.entity.hottopic.HotTopicContentg
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.MyGalleryg
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.MyProfileHolder
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.People
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.SearperProfile
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.TopPortionQuestg
import com.goforer.grabph.data.datasource.model.cache.data.entity.quest.info.QuestInfog
import com.goforer.grabph.data.datasource.network.response.ApiResponse
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.data.repository.remote.Repository.Companion.PER_PAGE
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.Owner
import kotlinx.coroutines.*

/**
 * A generic class that can provide a resource backed by the network.
 * <p>
 * You can read more about it in the <a href="https://developer.android.com/arch">Architecture
 * Guide</a>.
 */
abstract class NetworkBoundResource<HandleType, ResultType, RequestType>
@MainThread constructor(val loadType: Int, var boundType: Int) {
    companion object {
        const val BOUND_TO_BACKEND = 0
        const val BOUND_FROM_BACKEND = 1
        const val BOUND_FROM_LOCAL = 2

        const val LOAD_HOME = 100
        const val LOAD_FEED = 101
        const val LOAD_FEED_LOCAL = 102
        const val LOAD_FEED_HAS_NEXT_PAGE = 103
        const val LOAD_FEED_UPDATE = 104
        const val LOAD_QUEST_TOP_PORTION = 105
        const val LOAD_FAVORITE_QUEST_INFO = 106
        const val LOAD_FAVORITE_QUESTS = 107
        const val LOAD_FAVORITE_QUESTS_HAS_NEXT_PAGE = 108
        const val LOAD_FEED_SEARCH = 109
        const val LOAD_FEED_SEARCH_HAS_NEXT_PAGE = 110
        const val LOAD_PERSON = 111
        const val LOAD_EXIF = 112
        const val LOAD_GEO = 113
        const val LOAD_PHOTOG_PHOTO = 114
        const val LOAD_PHOTOG_PHOTO_HAS_NEXT_PAGE = 115
        const val LOAD_COMMENTS = 116
        const val LOAD_PHOTO_INFO = 117
        const val LOAD_CPHOTOG_PHOTO = 118
        const val LOAD_CPHOTOG_PHOTO_HAS_NEXT_PAGE = 119
        const val LOAD_CPHOTO_UPDATE = 120
        const val LOAD_OWNER = 121
        const val LOAD_CATEGORY = 122
        const val LOAD_HOME_PROFILE = 123
        const val LOAD_HOT_TOPIC_CONTENT = 124
        const val LOAD_PEOPLE = 125
        const val LOAD_PEOPLE_HAS_NEXT_PAGE = 126
        const val LOAD_RANKING = 127
        const val LOAD_OTHERS_PROFILE = 128
        const val LOAD_SEARCH_KEYWORD = 129
        const val LOAD_MY_GALLERYG_PHOTO = 130
        const val LOAD_MY_GALLERYG_PHOTO_HAS_NEXT_PAGE = 131
        const val LOAD_MY_PROFILE = 132
        const val LOAD_PHOTO_SIZES = 133

        private const val NONE_ITEM_COUNT = 20
    }

    private val result = MediatorLiveData<Resource>()

    private val resource = Resource()

    init {
        resource.loading(null)
        //result.value = resource
        if (boundType == BOUND_FROM_BACKEND) {
            runBlocking {
                fetchFromNetwork(MediatorLiveData(), loadType)
            }

            // This function had been blocked at this time but it might be used in the future
            /*
            if (shouldFetch()) {
                runBlocking {
                    fetchFromNetwork(cacheSource, loadType)
                }
            } else {
                runBlocking {
                    result.addSource(cacheSource) { updatedData
                        -> result.postValue(resource.success(updatedData, 0)) }
                }
            }
            */
        } else if (boundType == BOUND_FROM_LOCAL) {
            runBlocking {
                fetchFromLocal()
            }
        }
    }

    private suspend fun fetchFromLocal() {
        fetchDataFromCache(this)
    }

    private suspend fun fetchFromNetwork(cacheSource: MediatorLiveData<HandleType>, loadType: Int) {
        fetchDataFromNetwork(this, cacheSource, loadType)
    }

    private suspend fun fetchDataFromCache(resource: NetworkBoundResource<HandleType, ResultType, RequestType>) {
        val cacheData = withContext(Dispatchers.IO) {
            resource.loadFromCache(false, NONE_ITEM_COUNT, 0)
        }

        resource.result.addSource(cacheData) {
            resource.result.removeSource(cacheData)
            resource.result.addSource(cacheData) { updatedData
                -> resource.result.postValue(resource.resource.success(updatedData, 0)) }
        }
    }

    private suspend fun fetchDataFromNetwork(resource: NetworkBoundResource<HandleType, ResultType, RequestType>,
        cacheSource: MediatorLiveData<HandleType>, loadType: Int) {
        val responseData = resource.loadFromNetwork()

        // we re-attach cacheSource as a new source, it will dispatch its latest value quickly
        resource.result.addSource(cacheSource) { newData ->
            resource.result.postValue(resource.resource.loading(newData))
        }

        resource.result.addSource(responseData) { response ->
            resource.result.removeSource(responseData)
            resource.result.removeSource(cacheSource)
            //no inspection ConstantConditions
            response?.let {
                if (response.isSuccessful) {
                    runBlocking {
                        resource.saveResult(response, loadType)
                    }
                } else {
                    failed(resource, response)
                }
            }

            response ?: failed(resource, response)
        }
    }

    @WorkerThread
    private suspend fun saveResult(response: ApiResponse<RequestType>, loadType: Int) {
        saveResultAndReInit(this, response, result, loadType)
    }

    private fun failed(resource: NetworkBoundResource<HandleType, ResultType, RequestType>,
        response: ApiResponse<RequestType>) {
        resource.result.postValue(resource.resource.error(response.errorMessage, response.code))
        resource.onNetworkError(response.errorMessage, response.code)
    }

    protected abstract fun onNetworkError(errorMessage: String?, errorCode: Int)

    protected abstract fun onFetchFailed(failedMessage: String?)

    fun getAsLiveData(): MediatorLiveData<Resource> {
        return result
    }

    @WorkerThread
    protected abstract suspend fun handleToCache(item: HandleType)

    /*
    @MainThread
    protected abstract fun shouldFetch(): Boolean
    */

    @WorkerThread
    protected abstract suspend fun loadFromCache(isLatest: Boolean, itemCount: Int, pages: Int): LiveData<ResultType>

    @WorkerThread
    protected abstract suspend fun loadFromNetwork(): LiveData<ApiResponse<RequestType>>

    @WorkerThread
    protected abstract suspend fun clearCache()

    @WorkerThread
    private suspend fun saveResultAndReInit(resource: NetworkBoundResource<HandleType, ResultType, RequestType>,
        response: ApiResponse<RequestType>, result: MediatorLiveData<Resource>, loadType: Int) {
        var hasNextPage = false
        var pages = 0

        @Suppress("UNCHECKED_CAST")
        val cacheData = withContext(Dispatchers.IO) {
            when (loadType) {
                LOAD_HOME -> {
                    if (listOf(response.body as Homeg, response.body.home).any { it == null }
                        || response.body.stat == "fail") {
                        resource.onFetchFailed("There is no information of the home.")
                    } else {
                        resource.handleToCache(response.body.home as HandleType)
                    }
                }

                LOAD_FEED, LOAD_FEED_UPDATE, LOAD_FEED_SEARCH, LOAD_FEED_HAS_NEXT_PAGE,
                LOAD_FEED_SEARCH_HAS_NEXT_PAGE -> {
                    hasNextPage = loadType == LOAD_FEED_HAS_NEXT_PAGE; LOAD_FEED_SEARCH_HAS_NEXT_PAGE
                    response.body ?: resource.onFetchFailed("There is no the feed data.")
                    response.body?.let {
                        // resource.handleToCache(item = (response.body as FlickrFeed).items as SaveType)

                        /* mock data from ....... home_feed.json */
                        val json = CommonUtils.getJson("mock/home_feed.json")
                        val flickrFeed = BaseModel.gson().fromJson(json, FlickrFeed::class.java)
                        resource.handleToCache(item = (flickrFeed.items as HandleType))
                    }
                }

                LOAD_PERSON -> {
                    if (listOf(response.body as SearperProfile, response.body.person).any { it == null }
                        || response.body.stat == "fail") {
                        resource.onFetchFailed("There is no the profile data of the searper.")
                    } else {
                        resource.handleToCache(response.body.person as HandleType)
                    }
                }

                LOAD_MY_PROFILE -> {
                    if (listOf(response.body as MyProfileHolder, response.body.person).any { it == null }
                        || response.body.stat == "fail") {
                        resource.onFetchFailed("There is no the profile data of the searper.")
                    } else {
                        resource.handleToCache(response.body.person as HandleType)
                    }
                }

                LOAD_EXIF -> {
                    if (listOf(response.body as PhotoEXIF, response.body.photo?.exif).any { it == null }
                        || (response.body).stat == "fail") {
                        resource.onFetchFailed("There are no any EXIF data in the photo.")
                    } else {
                        resource.handleToCache(response.body.photo?.exif as HandleType)
                    }
                }

                LOAD_GEO -> {
                    if (listOf(response.body as PhotoGEO, response.body.photo, response.body.photo?.location).any { it == null }
                        || response.body.stat == "fail") {
                        resource.onFetchFailed("There is no the geo data of the photo.")
                    } else {
                        resource.handleToCache(response.body.photo?.location as HandleType)
                    }
                }

                LOAD_PHOTOG_PHOTO, LOAD_PHOTOG_PHOTO_HAS_NEXT_PAGE -> {
                    hasNextPage = loadType == LOAD_PHOTOG_PHOTO_HAS_NEXT_PAGE
                    if (listOf(response.body as Photog, response.body.photos, response.body.photos?.photo).any { it == null }
                        || response.body.stat == "fail") {
                        resource.onFetchFailed("There is no any more photos.")
                    } else {
                        pages = response.body.photos?.pages ?: 0
                        resource.handleToCache(response.body.photos!!.photo as HandleType)
                    }
                }

                LOAD_MY_GALLERYG_PHOTO, LOAD_MY_GALLERYG_PHOTO_HAS_NEXT_PAGE -> {
                    hasNextPage = loadType == LOAD_MY_GALLERYG_PHOTO_HAS_NEXT_PAGE
                    if (listOf(response.body as MyGalleryg, response.body.photos, response.body.photos?.photo).any { it == null }
                        || response.body.stat == "fail") {
                        resource.onFetchFailed("There is no any more my gallery.")
                    } else {
                        pages = response.body.photos?.pages ?: 0
                        resource.handleToCache(response.body.photos!!.photo as HandleType)
                    }
                }

                LOAD_COMMENTS -> {
                    if (listOf(response.body as PhotoComments, response.body.comments,
                            response.body.comments?.comment).any { it == null }
                        || response.body.stat == "fail") {
                        resource.onFetchFailed("There is no any comment of the photo.")
                    } else {
                        resource.handleToCache(response.body.comments?.comment as HandleType)
                    }
                }

                LOAD_PHOTO_INFO -> {
                    if (listOf(response.body as PhotoInfo, response.body.photo).any { it == null }
                        || response.body.stat == "fail") {
                        resource.onFetchFailed("There is no information of the photo.")
                    } else {
                        resource.handleToCache(response.body.photo as HandleType)
                    }
                }

                LOAD_FAVORITE_QUEST_INFO -> {
                    if (listOf(response.body as QuestInfog, response.body.questInfo).any { it == null }
                        || response.body.stat == "fail") {
                        resource.onFetchFailed("There is no information of the questInfo.")
                    } else {
                        resource.handleToCache(response.body.questInfo as HandleType)
                    }
                }

                LOAD_QUEST_TOP_PORTION -> {
                    if (listOf(response.body as TopPortionQuestg, response.body.topPortionQuest).any { it == null }
                        || response.body.stat == "fail") {
                        resource.onFetchFailed("There is no information of the questInfo.")
                    } else {
                        resource.handleToCache(response.body.topPortionQuest as HandleType)
                    }
                }

                LOAD_FAVORITE_QUESTS, LOAD_FAVORITE_QUESTS_HAS_NEXT_PAGE -> {
                    hasNextPage = loadType == LOAD_FAVORITE_QUESTS_HAS_NEXT_PAGE
                    if (listOf(response.body as QuestsInfog, response.body.questG, response.body.questG?.quest).any { it == null }
                        || response.body.stat == "fail") {
                        resource.onFetchFailed("There is no any questInfo.")
                    } else {
                        pages = response.body.questG?.pages ?: 0
                        resource.handleToCache(response.body.questG?.quest as HandleType)
                    }
                }

                LOAD_CATEGORY -> {
                    if (listOf(response.body as Categoryg, response.body.categories).any { it == null }
                        || response.body.stat == "fail") {
                        resource.onFetchFailed("There is no information of the questInfo.")
                    } else {
                        resource.handleToCache(response.body.categories as HandleType)
                    }
                }

                LOAD_HOT_TOPIC_CONTENT -> {
                    if (listOf(response.body as HotTopicContentg, response.body.hotTopicContent).any { it == null }
                        || response.body.stat == "fail") {
                        resource.onFetchFailed("There is no information of the questInfo.")
                    } else {
                        resource.handleToCache(response.body.hotTopicContent as HandleType)
                    }
                }

                LOAD_HOME_PROFILE -> {
                    response.body ?: resource.onFetchFailed("There is no information of HomeProfile")
                    response.body?.let { resource.handleToCache(response.body as HandleType) }
                }

                LOAD_PEOPLE -> {
                    if (listOf(response.body as People, response.body.result).any { it == null}) {
                        resource.onFetchFailed("There is no information of People")
                    } else {
                        resource.handleToCache(response.body.result as HandleType)
                    }
                }

                LOAD_RANKING -> {
                    response.body ?: resource.onFetchFailed("There is no information of Ranking")
                }

                LOAD_OTHERS_PROFILE -> {
                    response.body ?: resource.onFetchFailed("There is no information of OwnerProfile")
                    response.body?.let { resource.handleToCache((response.body as Owner) as HandleType) }
                }

                LOAD_PHOTO_SIZES -> {
                    response.body ?: resource.onFetchFailed("There is no photo size data")
                }
            }

            if (loadType == LOAD_FEED_SEARCH) {
                resource.loadFromCache(true, PER_PAGE, pages)
            } else {
                resource.loadFromCache(false, PER_PAGE, pages)
            }
        }

        // Make sure the NetworkResource is not null before doing anything on it because the destroyed
        // NetworkResource could be null.
        // we specially request a new live data,
        // otherwise we will get immediately last cached value,
        // which may not be updated with latest results received from network.
        if (!hasNextPage) {
            result.addSource<ResultType>(cacheData) { newData ->
                result.postValue(resource.resource.success(newData, 0))
            }
        } else {
            result.addSource<ResultType>(cacheData) { newData ->
                if (response.links.isNotEmpty()) {
                    result.postValue(resource.resource.success(newData,
                        response.getNextPage!!))
                } else {
                    result.postValue(resource.resource.success(newData, 0))
                }
            }
        }
    }
}