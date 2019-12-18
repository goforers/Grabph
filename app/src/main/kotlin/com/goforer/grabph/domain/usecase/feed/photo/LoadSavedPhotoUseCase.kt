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

package com.goforer.grabph.domain.usecase.feed.photo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.goforer.grabph.domain.usecase.BaseUseCase
import com.goforer.grabph.data.repository.local.LocalSavedPhotoRepository
import com.goforer.grabph.data.datasource.model.cache.data.AbsentLiveData
import com.goforer.grabph.data.datasource.model.cache.data.entity.savedphoto.LocalSavedPhoto
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadSavedPhotoUseCase
@Inject
constructor(private val repository: LocalSavedPhotoRepository):  BaseUseCase<String, LocalSavedPhoto>() {
    private val liveData by lazy { MutableLiveData<String>() }

    override fun execute(viewModelScope: CoroutineScope, parameters: String): LiveData<LocalSavedPhoto> {
        setFileName(parameters)

        return Transformations.switchMap(liveData) { filename ->
            filename ?: AbsentLiveData.create<LocalSavedPhoto>()
            repository.loadPhotoInfo(filename!!)
        }
    }

    private fun setFileName(filename: String) {
        liveData.value = filename

        val input = filename.trim { it <= ' ' }
        if (input == liveData.value) {
            return
        }

        liveData.value = input
    }

    internal fun loadPhotoFileNames() = repository.loadPhotoFileNames()

    internal fun loadPhotoIds() = repository.loadPhotoIds()

    internal fun loadPhotoItems() = repository.loadPhotoItems()

    internal suspend fun savePhotoInfo(filename: String, user: LocalSavedPhoto) = repository.savePhotoInfo(filename, user)

    internal suspend fun deletePhotoInfo(filename: String) = repository.deletePhotoInfo(filename)
}