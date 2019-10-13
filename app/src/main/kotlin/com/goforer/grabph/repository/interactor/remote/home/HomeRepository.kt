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

package com.goforer.grabph.repository.interactor.remote.home

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.goforer.base.annotation.MockData
import com.goforer.grabph.domain.usecase.Parameters
import com.goforer.grabph.repository.interactor.remote.Repository
import com.goforer.grabph.repository.model.cache.data.entity.Query
import com.goforer.grabph.repository.model.cache.data.entity.home.Home
import com.goforer.grabph.repository.model.cache.data.entity.home.Homeg
import com.goforer.grabph.repository.model.dao.remote.home.HomeDao
import com.goforer.grabph.repository.network.resource.NetworkBoundResource
import com.goforer.grabph.repository.network.response.ApiResponse
import com.goforer.grabph.repository.network.response.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository
@Inject
constructor(private val dao: HomeDao): Repository<Query>() {
        companion object {
            const val METHOD = "searp.home.getMain"
        }

        override suspend fun load(liveData: MutableLiveData<Query>, parameters: Parameters): LiveData<Resource> {
            return object: NetworkBoundResource<Home, Home, Homeg>(parameters.loadType, parameters.boundType) {
                override suspend fun saveToCache(item: Home) = dao.insert(item)

                // This function had been blocked at this time but it might be used in the future
                /*
                override fun shouldFetch(): Boolean {
                    return true
                }
                */

                override suspend fun loadFromCache(isLatest: Boolean, itemCount: Int, pages: Int) = dao.getHome()

                override suspend fun loadFromNetwork(): LiveData<ApiResponse<Homeg>> = searpService.getHome(KEY, METHOD, FORMAT_JSON, INDEX)

                override fun onNetworkError(errorMessage: String?, errorCode: Int) {}

                override fun onFetchFailed(failedMessage: String?) = repoRateLimit.reset(parameters.query1 as String)

                override suspend fun clearCache() = dao.clearAll()
            }.getAsLiveData()
        }

        @MockData
        internal fun loadHome() = Transformations.map(dao.getHome()) { it }

        @WorkerThread
        @MockData
        internal suspend fun setHome(home: Home) = insert(home)

        @WorkerThread
        internal suspend fun updateHome(home: Home) = update(home)

        @WorkerThread
        internal suspend fun deleteHome() = delete()

        @MockData
        internal suspend fun insert(home: Home) = dao.insert(home)

        internal suspend fun update(home: Home) = dao.update(home)

        internal suspend fun delete() = dao.clearAll()
}