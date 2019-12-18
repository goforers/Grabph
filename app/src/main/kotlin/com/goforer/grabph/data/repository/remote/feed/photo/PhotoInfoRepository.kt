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
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.data.repository.remote.Repository
import com.goforer.grabph.data.datasource.model.cache.data.entity.Query
import com.goforer.grabph.data.datasource.model.cache.data.entity.photoinfo.PhotoInfo
import com.goforer.grabph.data.datasource.model.cache.data.entity.photoinfo.Picture
import com.goforer.grabph.data.datasource.model.dao.remote.feed.photo.PhotoInfoDao
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoInfoRepository
@Inject
constructor(private val dao: PhotoInfoDao): Repository<Query>() {
    companion object {
        const val METHOD = "flickr.photos.getInfo"
    }

    override suspend fun load(liveData: MutableLiveData<Query>, parameters: Parameters): LiveData<Resource> {
        return object: NetworkBoundResource<Picture, Picture, PhotoInfo>(parameters.loadType, parameters.boundType) {
            override suspend fun handleToCache(item: Picture) = dao.insert(item)

            // This function had been blocked at this time but it might be used in the future
            /*
            override fun shouldFetch(): Boolean {
                return true
            }
            */

            override suspend fun loadFromCache(isLatest: Boolean, itemCount: Int, pages: Int) = dao.getPhotoInfo()

            override suspend fun loadFromNetwork() = searpService.getPhotoInfo(KEY, parameters.query1 as String, METHOD, FORMAT_JSON, INDEX)

            override fun onNetworkError(errorMessage: String?, errorCode: Int) {}

            override fun onFetchFailed(failedMessage: String?) = repoRateLimit.reset(parameters.query1 as String)

            override suspend fun clearCache() = dao.clearAll()
        }.getAsLiveData()
    }

    internal fun loadPhotoInfo(id: String) = dao.getPhotoInfo(id)

    internal suspend fun removePhotoInfo() = dao.clearAll()
}