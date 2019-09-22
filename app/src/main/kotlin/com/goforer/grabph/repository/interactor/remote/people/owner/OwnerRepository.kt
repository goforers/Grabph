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

package com.goforer.grabph.repository.interactor.remote.people.owner

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.goforer.base.annotation.MockData
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.repository.interactor.remote.Repository
import com.goforer.grabph.repository.model.cache.data.entity.profile.Owner
import com.goforer.grabph.repository.model.cache.data.entity.profile.OwnerProfile
import com.goforer.grabph.repository.model.dao.remote.people.owner.OwnerDao
import com.goforer.grabph.repository.network.resource.NetworkBoundResource
import com.goforer.grabph.repository.network.response.Resource

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OwnerRepository
@Inject
constructor(private val dao: OwnerDao): Repository() {
    companion object {
        const val METHOD = "flickr.owner.getinfo"
    }

    override suspend fun load(viewModel: BaseViewModel, query1: String, query2: Int, loadType: Int,
                              boundType: Int, calledFrom: Int): LiveData<Resource> {
        return object: NetworkBoundResource<Owner, Owner, OwnerProfile>(loadType, boundType) {
            override suspend fun saveToCache(item: Owner) = dao.insert(item)

            // This function had been blocked at this time but it might be used in the future
            /*
            override fun shouldFetch(): Boolean {
                return true
            }
            */

            override suspend fun loadFromCache(isLatest: Boolean, itemCount: Int, pages: Int) = dao.getOwner()

            override suspend fun loadFromNetwork() = searpService.getOwnerProfile(KEY, query1, METHOD, FORMAT_JSON, INDEX)

            override fun onNetworkError(errorMessage: String?, errorCode: Int) {}

            override fun onFetchFailed(failedMessage: String?) = repoRateLimit.reset(query1)

            override suspend fun clearCache() = dao.clearAll()
        }.getAsLiveData()
    }

    internal fun loadOwner(): LiveData<Owner> = Transformations.map(dao.getOwner()) { it }

    @WorkerThread
    @MockData
    internal suspend fun setOwner(owner: Owner) =  insert(owner)

    internal fun removeOwner() = delete()

    internal suspend fun insert(owner: Owner) = dao.insert(owner)

    internal fun delete() = dao.clearAll()
}