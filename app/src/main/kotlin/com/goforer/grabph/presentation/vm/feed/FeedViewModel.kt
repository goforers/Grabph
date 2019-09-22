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

package com.goforer.grabph.presentation.vm.feed

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import androidx.paging.PagedList
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.repository.model.cache.data.AbsentLiveData
import com.goforer.grabph.repository.model.cache.data.entity.feed.FeedItem
import com.goforer.grabph.repository.network.response.Resource
import com.goforer.grabph.repository.interactor.remote.feed.FeedItemRepository
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedViewModel
@Inject
constructor(private val interactor: FeedItemRepository): BaseViewModel() {
    @VisibleForTesting
    private val liveData by lazy {
        MutableLiveData<String>()
    }

    internal val feed: LiveData<Resource>

    internal var calledFrom: Int = 0

    internal val pinnedup: LiveData<PagedList<FeedItem>> by lazy {
        interactor.loadPinnedupFeed()
    }

    internal val feeds: List<FeedItem> by lazy {
        interactor.loadFeeds()
    }

    init {
        feed = liveData.switchMap { query ->
            query ?: AbsentLiveData.create<Resource>()

            liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                emitSource(interactor.load(this@FeedViewModel, query!!, -1, loadType, boundType, calledFrom))
            }
        }
    }

    private fun closeWork(viewModelScope: CoroutineScope?) {
        viewModelScope?.coroutineContext?.cancelChildren()
    }

    internal fun getFeed(id: Long): LiveData<FeedItem>? = liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) { emitSource(interactor.loadFeed(id)) }

    internal fun updateFeedItem(feedItem: FeedItem) {
        viewModelScope.launch {
            interactor.updateFeedItem(feedItem)
        }

        closeWork(viewModelScope)
    }

    internal fun insertFeedItems(feedItems: List<FeedItem>) {
        viewModelScope.launch {
            interactor.insert(feedItems)
        }

        closeWork(viewModelScope)
    }

    internal fun deleteLastSeenItems(size: Int) {
        viewModelScope.launch {
            interactor.deleteLastSeenItems(size)
        }

        closeWork(viewModelScope)
    }

    internal fun setKeyword(keyword: String) {
        liveData.value = keyword

        val input = keyword.toLowerCase(Locale.getDefault()).trim { it <= ' ' }

        if (input == liveData.value) {
            return
        }

        liveData.value = input
    }
}
