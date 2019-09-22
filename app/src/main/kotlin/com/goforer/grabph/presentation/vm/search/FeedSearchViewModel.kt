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

package com.goforer.grabph.presentation.vm.search

import androidx.paging.PagedList
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.repository.model.cache.data.AbsentLiveData
import com.goforer.grabph.repository.model.cache.data.entity.feed.FeedItem
import com.goforer.grabph.repository.network.response.Resource
import com.goforer.grabph.repository.interactor.remote.search.FeedSearchRepository
import kotlinx.coroutines.Dispatchers
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedSearchViewModel
@Inject
constructor(private val interactor: FeedSearchRepository): BaseViewModel() {
    @VisibleForTesting
    private val liveData by lazy {
        MutableLiveData<String>()
    }

    internal val feed: LiveData<Resource>

    internal var calledFrom: Int = 0

    init {
        feed = Transformations.switchMap(liveData) { query ->
            query ?: AbsentLiveData.create<Resource>()
            liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                emitSource(interactor.load(this@FeedSearchViewModel, query!!, -1, loadType, boundType, calledFrom))
            }
        }
    }

    internal val pinnedup: LiveData<PagedList<FeedItem>> by lazy {
        interactor.loadPinnedupFeed()
    }

    internal val feeds: List<FeedItem> by lazy {
        interactor.loadFeeds()
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