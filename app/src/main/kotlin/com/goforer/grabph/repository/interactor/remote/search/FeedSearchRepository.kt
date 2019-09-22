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

package com.goforer.grabph.repository.interactor.remote.search

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.repository.interactor.remote.Repository
import com.goforer.grabph.repository.model.cache.data.entity.feed.FeedItem
import com.goforer.grabph.repository.model.cache.data.entity.feed.FlickrFeed
import com.goforer.grabph.repository.model.dao.remote.feed.FeedItemDao
import com.goforer.grabph.repository.network.response.Resource
import com.goforer.grabph.repository.network.resource.NetworkBoundResource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedSearchRepository
@Inject
constructor(private val dao: FeedItemDao): Repository() {
    override suspend fun load(viewModel: BaseViewModel, query1: String, query2: Int, loadType: Int,
                              boundType: Int, calledFrom: Int): LiveData<Resource> {
        return object: NetworkBoundResource<MutableList<FeedItem>, PagedList<FeedItem>, FlickrFeed>(loadType, boundType) {
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
                        .setPageSize(10)
                        .setPrefetchDistance(5)
                        .setEnablePlaceholders(true)
                        .build()

                return if (isLatest) {
                    LivePagedListBuilder(dao.getLatestFeedItems(itemCount),
                            /* PageList Config */ config).build()
                } else {
                    LivePagedListBuilder(dao.getFeedItems(),
                            /* PageList Config */ config).build()
                }
            }

            override suspend fun loadFromNetwork() = searpService.getFeed(query1, FORMAT_JSON, LANG_ENGLISH, LANG_KOREAN,
                    LANG_GERMAN, LANG_SPANISH, LANG_FRANCE, LANG_ITALY, INDEX)

            override fun onNetworkError(errorMessage: String?, errorCode: Int) {}

            override fun onFetchFailed(failedMessage: String?) = repoRateLimit.reset(query1)

            override suspend fun clearCache() =  dao.clearAll()
        }.getAsLiveData()
    }

    internal fun loadPinnedupFeed() = LivePagedListBuilder(dao.getPinnedupItems(),
            /* page size */ PER_PAGE).build()

    internal fun loadFeeds() = dao.getFeeds()
}