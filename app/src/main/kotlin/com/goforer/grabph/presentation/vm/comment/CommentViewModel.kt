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

package com.goforer.grabph.presentation.vm.comment

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.repository.model.cache.data.AbsentLiveData
import com.goforer.grabph.repository.network.response.Resource
import com.goforer.grabph.repository.interactor.remote.comment.CommentRepository
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentViewModel
@Inject
constructor(val interactor: CommentRepository): BaseViewModel() {
    @VisibleForTesting
    private val liveData by lazy {
        MutableLiveData<String>()
    }

    internal val comments: LiveData<Resource>

    init {
        comments = Transformations.switchMap(liveData) { photoId ->
            photoId ?: AbsentLiveData.create<Resource>()
            liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                emitSource(interactor.load(this@CommentViewModel, photoId!!, -1, loadType, boundType, -1))
            }
        }
    }

    internal val commentList = liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) { emitSource(interactor.loadComments()) }

    internal fun setPhotoId(photoId: String) {
        liveData.value = photoId
    }
}
