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

package com.goforer.grabph.data.repository.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.annotation.WorkerThread
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.Owner
import com.goforer.grabph.data.datasource.model.dao.remote.people.owner.OwnerDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalOwnerInteractor
@Inject constructor(private val dao: OwnerDao) {
    private val result = MediatorLiveData<Owner>()

    @WorkerThread
    internal suspend fun saveOwnerInfo(owner: Owner): MediatorLiveData<Owner> {
        save(result, owner)

        return result
    }

    private fun loadOwnerInfo(): LiveData<Owner> {
        return dao.getOwner()
    }

    @WorkerThread
    internal suspend fun deleteOwnerInfo() {
        delete()
    }

    internal suspend fun delete() {
        withContext(Dispatchers.IO) {
            dao.clearAll()
        }
    }

    internal suspend fun save(result: MediatorLiveData<Owner>, owner: Owner) {
        val liveData = withContext(Dispatchers.IO) {
            dao.insert(owner)
            loadOwnerInfo()
        }

        result.addSource(liveData) {
            result.setValue(owner)
        }
    }
}