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

package com.goforer.grabph.repository.interactor.remote.feed.photo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.goforer.grabph.domain.usecase.Parameters
import com.goforer.grabph.repository.interactor.remote.Repository
import com.goforer.grabph.repository.model.cache.data.entity.photog.Photo
import com.goforer.grabph.repository.model.cache.data.entity.photog.Photog
import com.goforer.grabph.repository.model.dao.remote.feed.photo.PhotoDao
import com.goforer.grabph.repository.network.response.Resource
import com.goforer.grabph.repository.network.resource.NetworkBoundResource
import com.goforer.grabph.repository.interactor.remote.paging.boundarycallback.PageListPhotoBoundaryCallback
import com.goforer.grabph.repository.model.cache.data.entity.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepository
@Inject
constructor(private val dao: PhotoDao): Repository<Query>() {
    companion object {
        const val METHOD = "flickr.people.getphotos"

        const val PREFETCH_DISTANCE = 10
    }

    override suspend fun load(liveData: MutableLiveData<Query>, parameters: Parameters): LiveData<Resource> {
        return object: NetworkBoundResource<MutableList<Photo>, PagedList<Photo>, Photog>(parameters.loadType, parameters.boundType) {
            override suspend fun saveToCache(item: MutableList<Photo>) = dao.insert(item)

            // This function had been blocked at this time but it might be used in the future
            /*
            override fun shouldFetch(): Boolean {
                return true
            }
            */

            override suspend fun loadFromCache(isLatest: Boolean, itemCount: Int,
                                               pages: Int): LiveData<PagedList<Photo>> {
                val config = PagedList.Config.Builder()
                        .setInitialLoadSizeHint(20)
                        .setPageSize(itemCount)
                        .setPrefetchDistance(PREFETCH_DISTANCE)
                        .setEnablePlaceholders(true)
                        .build()

                return withContext(Dispatchers.IO) {
                    LivePagedListBuilder(dao.getPhotos(parameters.query1 as String), /* PageList Config */ config)
                            .setBoundaryCallback(PageListPhotoBoundaryCallback<Photo>(
                                liveData, parameters.query1 as String, pages)).build()
                        }
            }

            override suspend fun loadFromNetwork() = searpService.getPhotos(KEY, parameters.query1 as String, METHOD, FORMAT_JSON, parameters.query2 as Int, PER_PAGE, INDEX)

            override fun onNetworkError(errorMessage: String?, errorCode: Int) {}

            override fun onFetchFailed(failedMessage: String?) = repoRateLimit.reset(parameters.query1 as String)

            override suspend fun clearCache() =  dao.clearAll()
        }.getAsLiveData()
    }

    internal fun deleteByPhotoId(id: String) = dao.deleteByPhotoId(id)

    internal suspend fun update(photo: Photo) = dao.update(photo)

    internal suspend fun removePhotos() = dao.clearAll()

    /*
    private fun inputCurrentTime(photos: MutableList<Photo>): MutableList<Photo> {
        for (photo in photos) {
            photo.createdAt = CommonUtils.getCurrentDateTime()
        }

        return photos
    }
    */
}