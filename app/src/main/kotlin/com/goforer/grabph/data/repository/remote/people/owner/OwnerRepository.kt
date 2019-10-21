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

package com.goforer.grabph.data.repository.remote.people.owner

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.goforer.base.annotation.MockData
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.data.repository.remote.Repository
import com.goforer.grabph.data.datasource.model.cache.data.entity.Query
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.Owner
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.OwnerProfile
import com.goforer.grabph.data.datasource.model.dao.remote.people.owner.OwnerDao
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource
import com.goforer.grabph.data.datasource.network.response.Resource

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OwnerRepository
@Inject
constructor(private val dao: OwnerDao): Repository<Query>() {
    companion object {
        const val METHOD = "flickr.owner.getinfo"
    }

    override suspend fun load(liveData: MutableLiveData<Query>, parameters: Parameters): LiveData<Resource> {
        return object: NetworkBoundResource<Owner, Owner, OwnerProfile>(parameters.loadType, parameters.boundType) {
            override suspend fun saveToCache(item: Owner) = dao.insert(item)

            // This function had been blocked at this time but it might be used in the future
            /*
            override fun shouldFetch(): Boolean {
                return true
            }
            */

            override suspend fun loadFromCache(isLatest: Boolean, itemCount: Int, pages: Int) = dao.getOwner()

            override suspend fun loadFromNetwork() = searpService.getOwnerProfile(KEY, parameters.query1 as String, METHOD, FORMAT_JSON, INDEX)

            override fun onNetworkError(errorMessage: String?, errorCode: Int) {}

            override fun onFetchFailed(failedMessage: String?) = repoRateLimit.reset(parameters.query1 as String)

            override suspend fun clearCache() = dao.clearAll()
        }.getAsLiveData()
    }

    internal fun loadOwner(): LiveData<Owner> = Transformations.map(dao.getOwner()) { it }

    @WorkerThread
    @MockData
    internal suspend fun setOwner(owner: Owner) =  insert(owner)

    internal suspend fun removeOwner() = delete()

    internal suspend fun insert(owner: Owner) = dao.insert(owner)

    internal suspend fun delete() = dao.clearAll()
}