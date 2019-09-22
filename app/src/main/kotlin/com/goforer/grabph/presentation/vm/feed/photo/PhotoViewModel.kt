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

package com.goforer.grabph.presentation.vm.feed.photo

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.repository.model.cache.data.entity.photog.PhotogQuery
import com.goforer.grabph.repository.model.cache.data.AbsentLiveData
import com.goforer.grabph.repository.network.response.Resource
import com.goforer.grabph.repository.interactor.remote.feed.photo.PhotoRepository
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class PhotoViewModel
@Inject
constructor(val interactor: PhotoRepository): BaseViewModel() {
    @VisibleForTesting
    private val liveData by lazy {
        MutableLiveData<PhotogQuery>()
    }

    internal val photogPhotos: LiveData<Resource>

    internal var calledFrom: Int = 0

    init {
        photogPhotos = Transformations.switchMap(liveData) { query ->
            query ?: AbsentLiveData.create<Resource>()
            liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                emitSource(interactor.load(this@PhotoViewModel, query?.userID!!, query.pages, loadType, boundType, calledFrom))
            }
        }
    }

    internal fun setQuery(query: PhotogQuery) {
        liveData.value = query

        val input = query.pages
        if (input == liveData.value?.pages) {
            return
        }

        liveData.value = query
    }
}