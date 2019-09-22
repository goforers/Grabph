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

package com.goforer.grabph.repository.interactor.remote.feed

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.goforer.base.annotation.MockData
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.repository.interactor.remote.Repository
import com.goforer.grabph.repository.model.cache.data.mock.entity.feed.FeedsContent
import com.goforer.grabph.repository.model.cache.data.mock.entity.feed.FeedsContentg
import com.goforer.grabph.repository.model.dao.remote.feed.FeedsContentDao
import com.goforer.grabph.repository.network.resource.NetworkBoundResource
import com.goforer.grabph.repository.network.response.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedsContentRepository
@Inject
constructor(private val dao: FeedsContentDao): Repository() {
    companion object {
        const val METHOD = "searp.home.getMain"
    }

    override suspend fun load(viewModel: BaseViewModel, query1: String, query2: Int, loadType: Int,
                              boundType: Int, calledFrom: Int): LiveData<Resource> {
        return object: NetworkBoundResource<FeedsContent, FeedsContent, FeedsContentg>(loadType, boundType) {
            override suspend fun saveToCache(item: FeedsContent) = dao.insert(item)

            // This function had been blocked at this time but it might be used in the future
            /*
            override fun shouldFetch(): Boolean {
                return true
            }
            */

            override suspend fun loadFromCache(isLatest: Boolean, itemCount: Int, pages: Int) = dao.getFeedsContent()

            override suspend fun loadFromNetwork() = searpService.getFeedsContent(KEY, query1, METHOD, FORMAT_JSON, INDEX)

            override fun onNetworkError(errorMessage: String?, errorCode: Int) {}

            override fun onFetchFailed(failedMessage: String?) =  repoRateLimit.reset(query1)

            override suspend fun clearCache() = dao.clearAll()
        }.getAsLiveData()
    }

    @MockData
    internal fun loadFeedsContent() = Transformations.map(dao.getFeedsContent()) { it }

    @WorkerThread
    @MockData
    internal suspend fun setFeedsContent(feedsContent: FeedsContent) = insert(feedsContent)

    @WorkerThread
    internal fun deleteFeedsContent() =  delete()

    @MockData
    internal suspend fun insert(feedsContent: FeedsContent) = dao.insert(feedsContent)

    internal fun delete() = dao.clearAll()
}