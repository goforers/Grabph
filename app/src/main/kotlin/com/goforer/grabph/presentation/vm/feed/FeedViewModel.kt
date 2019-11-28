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

import androidx.lifecycle.*
import androidx.paging.PagedList
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.domain.usecase.feed.LoadFeedUseCase
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.feed.FeedItem
import com.goforer.grabph.data.datasource.network.response.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedViewModel
@Inject
constructor(private val useCase: LoadFeedUseCase): BaseViewModel<Parameters>() {
    internal lateinit var feed: LiveData<Resource>
    internal lateinit var homeFeed: LiveData<PagedList<FeedItem>>

    internal var calledFrom: Int = 0

    internal val pinnedup: LiveData<PagedList<FeedItem>> by lazy {
        useCase.loadPinnedFeed()
    }

    internal val feeds: List<FeedItem> by lazy {
        useCase.loadFeeds()
    }

    override fun setParameters(parameters: Parameters, type: Int) {
        feed = useCase.execute(viewModelScope, parameters)
    }

    private fun closeWork(viewModelScope: CoroutineScope?) = viewModelScope?.coroutineContext?.cancelChildren()

    internal fun getFeed(id: Long): LiveData<FeedItem>? = liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) { useCase.loadFeed(id)?.let {
        emitSource(it)
    } }

    internal fun updateFeedItem(feedItem: FeedItem) {
        viewModelScope.launch { useCase.updateFeedItem(feedItem) }
        closeWork(viewModelScope)
    }

    internal fun insertFeedItems(feedItems: List<FeedItem>) {
        viewModelScope.launch { useCase.insertFeedItems(feedItems) }
        closeWork(viewModelScope)
    }

    internal fun deleteLastSeenItems(size: Int) {
        viewModelScope.launch { useCase.deleteLastSeenItems(size) }
        closeWork(viewModelScope)
    }

    internal fun clearCache() {
        viewModelScope.launch { useCase.clearCache() }
        closeWork(viewModelScope)
    }
}
