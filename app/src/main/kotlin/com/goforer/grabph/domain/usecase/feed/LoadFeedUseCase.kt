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

package com.goforer.grabph.domain.usecase.feed

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.goforer.grabph.domain.usecase.BaseUseCase
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.data.repository.remote.feed.FeedItemRepository
import com.goforer.grabph.data.datasource.model.cache.data.AbsentLiveData
import com.goforer.grabph.data.datasource.model.cache.data.entity.Query
import com.goforer.grabph.data.datasource.model.cache.data.entity.feed.FeedItem
import com.goforer.grabph.data.datasource.network.response.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadFeedUseCase
@Inject
constructor(private val repository: FeedItemRepository): BaseUseCase<Parameters, Resource>() {
    @VisibleForTesting
    private val liveData by lazy { MutableLiveData<Query>() }

    override fun execute(parameters: Parameters): LiveData<Resource> {
        setQuery(parameters, Query())

        return liveData.switchMap { query ->
            query ?: AbsentLiveData.create<Resource>()
            liveData {
                emitSource(repository.load(this@LoadFeedUseCase.liveData,
                    Parameters(
                        query.query,
                        liveData.value?.pages!!,
                        liveData.value?.loadType!!,
                        liveData.value?.boundType!!
                    )
                ))
            }
        }
    }

    private fun setQuery(parameters: Parameters, query: Query) {
        query.query = parameters.query1 as String
        query.pages = parameters.query2 as Int
        query.boundType = parameters.boundType
        query.loadType = parameters.loadType
        liveData.value = query

        val input = query.pages
        if (input == liveData.value?.pages) return

        liveData.value = query
    }

    internal fun loadFeed(id: Long): LiveData<FeedItem>? = repository.loadFeed(id)

    internal suspend fun updateFeedItem(feedItem: FeedItem) = repository.updateFeedItem(feedItem)

    internal suspend fun insertFeedItems(feedItems: List<FeedItem>) = repository.insert(feedItems)

    internal fun deleteLastSeenItems(size: Int) = repository.deleteLastSeenItems(size)

    internal fun loadPinnedFeed() = repository.loadPinnedupFeed()

    internal fun loadFeeds() = repository.loadFeeds()

    internal suspend fun clearFeed() = repository.clearCache()
}