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
import androidx.annotation.WorkerThread
import com.goforer.grabph.repository.model.cache.data.entity.location.LocalLocation
import com.goforer.grabph.repository.model.dao.local.LocalLocationDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalLocationRepository @Inject constructor(private val dao: LocalLocationDao) {
    private val result = MediatorLiveData<LocalLocation>()

    @WorkerThread
    internal suspend fun saveLocationInfo(filename: String,
                         localLocation: LocalLocation): MediatorLiveData<LocalLocation> {
        save(result, filename, localLocation)

        return result
    }

    internal fun loadLocationInfo(filename: String): LiveData<LocalLocation> {
        return dao.getLocationInfo(filename)
    }

    @WorkerThread
    internal suspend fun deleteLocationInfo(filename: String) {
        delete(filename)
    }

    internal suspend fun save(result: MediatorLiveData<LocalLocation>, fileName: String, localLocation: LocalLocation) {
        val location = withContext(Dispatchers.IO) {
            dao.insert(localLocation)
            loadLocationInfo(fileName)
        }

        result.addSource(location) {
            result.setValue(localLocation)
        }
    }

    internal suspend fun delete(fileName: String) {
        withContext(Dispatchers.IO) {
            dao.deleteLocationInfo(fileName)
        }
    }
}