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

package com.goforer.grabph.repository.interactor.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.annotation.WorkerThread
import com.goforer.grabph.repository.model.cache.data.entity.savedphoto.LocalSavedPhoto
import com.goforer.grabph.repository.model.dao.local.LocalSavedPhotoDao
import com.goforer.grabph.repository.interactor.remote.Repository.Companion.PER_PAGE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalSavedPhotoRepository @Inject constructor(private val dao: LocalSavedPhotoDao) {
    private val result = MediatorLiveData<LocalSavedPhoto>()

    @WorkerThread
    internal suspend fun savePhotoInfo(filename: String, user: LocalSavedPhoto): MediatorLiveData<LocalSavedPhoto> {
        save(result, filename, user)

        return result
    }

    internal fun loadPhotoInfo(filename: String): LiveData<LocalSavedPhoto> {
        return dao.getPhotoInfo(filename)
    }

    @WorkerThread
    internal suspend fun deletePhotoInfo(filename: String) {
        delete(filename)
    }

    internal fun loadPhotoItems(): List<LocalSavedPhoto> {
        return dao.getPhotoItems()
    }

    internal fun loadPhotoFileNames(): LiveData<PagedList<String>> {
        return LivePagedListBuilder(dao.getPhotoFilenames(),
                /* page size */ PER_PAGE).build()
    }

    internal fun loadPhotoIds(): LiveData<PagedList<String>> {
        return LivePagedListBuilder(dao.getPhotoIds(),
                /* page size */ PER_PAGE).build()
    }

    internal suspend fun delete(fileName: String) {
        withContext(Dispatchers.IO) {
            dao.deletePhotoInfo(fileName)
        }
    }

    internal suspend fun save(result: MediatorLiveData<LocalSavedPhoto>, fileName: String, localUser: LocalSavedPhoto) {
        val user = withContext(Dispatchers.IO) {
            dao.insert(localUser)
            loadPhotoInfo(fileName)
        }

        result.addSource(user) {
            result.setValue(localUser)
        }
    }
}