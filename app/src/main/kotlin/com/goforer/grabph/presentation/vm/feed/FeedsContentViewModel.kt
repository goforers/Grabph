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
import com.goforer.base.annotation.MockData
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.repository.model.cache.data.AbsentLiveData
import com.goforer.grabph.repository.model.cache.data.mock.entity.feed.FeedsContent
import com.goforer.grabph.repository.network.response.Resource
import com.goforer.grabph.repository.interactor.remote.feed.FeedsContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedsContentViewModel
@Inject
constructor(val repository: FeedsContentRepository): BaseViewModel() {
    @VisibleForTesting
    private val liveData by lazy {
        MutableLiveData<String>()
    }

    internal val feedsContent: LiveData<Resource>

    internal var calledFrom: Int = 0

    init {
        feedsContent = Transformations.switchMap(liveData) { hotTopicId ->
            hotTopicId ?: AbsentLiveData.create<Resource>()
            liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                emitSource(repository.load(this@FeedsContentViewModel, hotTopicId!!, -1, loadType, boundType, -1))
            }
        }
    }

    internal val loadFeedsContent = liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) { emitSource(repository.loadFeedsContent()) }

    internal fun setFeedsId(hotTopicId: String) {
        liveData.value = hotTopicId

        val input = hotTopicId.toLowerCase(Locale.getDefault()).trim { it <= ' ' }
        if (input == liveData.value) {
            return
        }

        liveData.value = input
    }

    @MockData
    internal fun setFeedsContent(feedsContent: FeedsContent) {
        viewModelScope.launch {
            repository.setFeedsContent(feedsContent)
        }
    }
}