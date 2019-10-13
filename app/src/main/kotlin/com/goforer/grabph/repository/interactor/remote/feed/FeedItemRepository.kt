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

package com.goforer.grabph.repository.interactor.remote.feed

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.goforer.grabph.domain.usecase.Parameters
import com.goforer.grabph.repository.interactor.remote.Repository
import com.goforer.grabph.repository.model.cache.data.entity.feed.FeedItem
import com.goforer.grabph.repository.model.cache.data.entity.feed.FlickrFeed
import com.goforer.grabph.repository.model.dao.remote.feed.FeedItemDao
import com.goforer.grabph.repository.network.response.Resource
import com.goforer.grabph.repository.network.resource.NetworkBoundResource
import com.goforer.grabph.repository.interactor.remote.paging.boundarycallback.PageListFeedItemBoundaryCallback
import com.goforer.grabph.repository.model.cache.data.entity.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedItemRepository
@Inject
constructor(private val dao: FeedItemDao): Repository<Query>() {
    override suspend fun load(liveData: MutableLiveData<Query>, parameters: Parameters): LiveData<Resource> {
        return object: NetworkBoundResource<MutableList<FeedItem>, PagedList<FeedItem>, FlickrFeed>(parameters.loadType, parameters.boundType) {
            override suspend fun saveToCache(item: MutableList<FeedItem>) {
                for (feedItem in item) {
                    feedItem.id = "0"
                    feedItem.isPinned = false
                    feedItem.pinnedDate = -1
                }

                dao.insert(item)
            }

            // This function had been blocked at this time but it might be used in the future
            /*
            override fun shouldFetch(): Boolean {
                return true
            }
            */

            override suspend fun loadFromCache(isLatest: Boolean, itemCount: Int,
                                               pages: Int): LiveData<PagedList<FeedItem>> {
                val config = PagedList.Config.Builder()
                        .setInitialLoadSizeHint(20)
                        .setPageSize(itemCount)
                        .setPrefetchDistance(10)
                        .setEnablePlaceholders(true)
                        .build()

                return withContext(Dispatchers.IO) {
                    if (isLatest) {
                        LivePagedListBuilder(dao.getLatestFeedItems(itemCount), /* PageList Config */ config)
                            .setBoundaryCallback(PageListFeedItemBoundaryCallback<FeedItem>(
                                liveData, parameters.query1 as String, pages)).build()
                    } else {
                        LivePagedListBuilder(dao.getFeedItems(), /* PageList Config */ config)
                            .setBoundaryCallback(PageListFeedItemBoundaryCallback<FeedItem>(
                                liveData, parameters.query1 as String, pages)).build()
                    }
                }
            }

            override suspend fun loadFromNetwork() = searpService.getFeed(parameters.query1 as String, FORMAT_JSON, LANG_ENGLISH, LANG_KOREAN,
                    LANG_GERMAN, LANG_SPANISH, LANG_FRANCE, LANG_ITALY, INDEX)

            override fun onNetworkError(errorMessage: String?, errorCode: Int) {}

            override fun onFetchFailed(failedMessage: String?) = repoRateLimit.reset(parameters.query1 as String)

            override suspend fun clearCache() = dao.clearAll()
        }.getAsLiveData()
    }

    @WorkerThread
    internal suspend fun updateFeedItem(feedItem: FeedItem) = update(feedItem)

    @WorkerThread
    internal suspend fun insertFeedItems(feedItems: List<FeedItem>) = insert(feedItems)

    internal fun loadPinnedupFeed() = LivePagedListBuilder(dao.getPinnedupItems(),
            /* page size */ PER_PAGE).build()

    internal fun loadFeed(id: Long) = dao.getFeedItem(id)

    internal fun loadFeeds() = dao.getFeeds()

    @WorkerThread
    internal fun deleteLastSeenItems(size: Int) =  delete(size)

    internal suspend fun update(feedItem: FeedItem) = dao.update(feedItem)

    internal suspend fun insert(feedItems: List<FeedItem>) = dao.insert(feedItems as MutableList<FeedItem>)

    internal fun delete(size: Int) = dao.removeLastSeenItems(size)
}