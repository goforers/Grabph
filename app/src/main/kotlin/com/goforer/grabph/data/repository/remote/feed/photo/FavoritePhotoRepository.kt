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

package com.goforer.grabph.data.repository.remote.feed.photo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.data.repository.remote.Repository
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.Photo
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.Photog
import com.goforer.grabph.data.datasource.model.dao.remote.feed.photo.PhotoDao
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource
import com.goforer.grabph.data.repository.remote.paging.boundarycallback.PageListFavoritePhotoBoundaryCallback
import com.goforer.grabph.data.datasource.model.cache.data.entity.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritePhotoRepository
@Inject
constructor(private val dao: PhotoDao): Repository<Query>() {
    companion object {
        const val METHOD = "flickr.favorites.getList"
    }

    override suspend fun load(liveData: MutableLiveData<Query>, parameters: Parameters): LiveData<Resource> {
        return object: NetworkBoundResource<MutableList<Photo>, PagedList<Photo>, Photog>(parameters.loadType, parameters.boundType) {
            override suspend fun handleToCache(item: MutableList<Photo>) = dao.insert(item)

            // This function had been blocked at this time but it might be used in the future
            /*
            override fun shouldFetch(): Boolean {
                return true
            }
            */

            override suspend fun loadFromCache(isLatest: Boolean, itemCount: Int,
                                               pages: Int): LiveData<PagedList<Photo>> {
                val config = PagedList.Config.Builder()
                        .setInitialLoadSizeHint(itemCount * 2)
                        .setPageSize(itemCount)
                        .setPrefetchDistance(itemCount - 2)
                        .setEnablePlaceholders(true)
                        .build()

                return withContext(Dispatchers.IO) {
                    LivePagedListBuilder(dao.getPhotos(parameters.query1 as String), /* PageList Config */ config)
                            .setBoundaryCallback(PageListFavoritePhotoBoundaryCallback<Photo>(
                                liveData, parameters.query1 as String, pages)).build()
                }
            }

            override suspend fun loadFromNetwork() = searpService.getFavoritePhotos(KEY, parameters.query1 as String, METHOD, FORMAT_JSON, parameters.query2 as Int, PER_PAGE, INDEX)

            override fun onNetworkError(errorMessage: String?, errorCode: Int) {}

            override fun onFetchFailed(failedMessage: String?) = repoRateLimit.reset(parameters.query1 as String)

            override suspend fun clearCache() = dao.clearAll()
        }.getAsLiveData()
    }

    internal fun deleteByPhotoId(id: String) = dao.deleteByPhotoId(id)

    internal suspend fun update(photo: Photo) = dao.update(photo)

    internal suspend fun removePhotos() = dao.clearAll()
}