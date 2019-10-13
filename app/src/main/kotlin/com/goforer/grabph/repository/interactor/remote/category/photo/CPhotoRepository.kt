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

package com.goforer.grabph.repository.interactor.remote.category.photo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.goforer.grabph.domain.usecase.Parameters
import com.goforer.grabph.repository.interactor.remote.Repository
import com.goforer.grabph.repository.model.cache.data.entity.category.CPhoto
import com.goforer.grabph.repository.model.cache.data.entity.cphotog.CPhotog
import com.goforer.grabph.repository.model.dao.remote.category.photo.CPhotoDao
import com.goforer.grabph.repository.network.resource.NetworkBoundResource
import com.goforer.grabph.repository.network.response.Resource
import com.goforer.grabph.repository.interactor.remote.paging.boundarycallback.PageListCPhotoBoundaryCallback
import com.goforer.grabph.repository.model.cache.data.entity.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CPhotoRepository
@Inject
constructor(private val dao: CPhotoDao): Repository<Query>() {
    companion object {
        const val METHOD = "searp.category.getPhotos"
    }

    override suspend fun load(liveData: MutableLiveData<Query>, parameters: Parameters): LiveData<Resource> {
        return object: NetworkBoundResource<MutableList<CPhoto>, PagedList<CPhoto>, CPhotog>(parameters.loadType, parameters.boundType) {
            override suspend fun saveToCache(item: MutableList<CPhoto>) = dao.insert(item)

            // This function had been blocked at this time but it might be used in the future
            /*
            override fun shouldFetch(): Boolean {
                return true
            }
            */

            override suspend fun loadFromCache(isLatest: Boolean, itemCount: Int,
                                               pages: Int): LiveData<PagedList<CPhoto>> {
                val config = PagedList.Config.Builder()
                        .setInitialLoadSizeHint(20)
                        .setPageSize(itemCount)
                        .setPrefetchDistance(10)
                        .setEnablePlaceholders(true)
                        .build()

                return withContext(Dispatchers.IO) {
                    LivePagedListBuilder(dao.getCPhotos(), /* PageList Config */ config)
                            .setBoundaryCallback(PageListCPhotoBoundaryCallback<CPhoto>(
                                liveData, parameters.query1 as String, pages)).build()
                }
            }

            override suspend fun loadFromNetwork() = searpService.getCategoryPhotos(KEY, parameters.query1 as String, METHOD, FORMAT_JSON, parameters.query2 as Int, PER_PAGE, INDEX)

            override fun onNetworkError(errorMessage: String?, errorCode: Int) {}

            override fun onFetchFailed(failedMessage: String?) = repoRateLimit.reset(parameters.query1 as String)

            override suspend fun clearCache() = dao.clearAll()
        }.getAsLiveData()
    }

    internal  fun deleteByPhotoId(id: String) = dao.deleteByCPhotoId(id)

    internal suspend fun update(photo: CPhoto) = dao.update(photo)

    internal suspend fun removePhotos() = dao.clearAll()
}