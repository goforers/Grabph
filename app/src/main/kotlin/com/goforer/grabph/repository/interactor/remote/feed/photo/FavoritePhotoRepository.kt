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
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.presentation.vm.feed.photo.FavoritePhotoViewModel
import com.goforer.grabph.repository.interactor.remote.Repository
import com.goforer.grabph.repository.model.cache.data.entity.photog.Photo
import com.goforer.grabph.repository.model.cache.data.entity.photog.Photog
import com.goforer.grabph.repository.model.dao.remote.feed.photo.PhotoDao
import com.goforer.grabph.repository.network.response.Resource
import com.goforer.grabph.repository.network.resource.NetworkBoundResource
import com.goforer.grabph.repository.interactor.remote.paging.boundarycallback.PageListFavoritePhotoBoundaryCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritePhotoRepository
@Inject
constructor(private val dao: PhotoDao): Repository() {
    companion object {
        const val METHOD = "flickr.favorites.getList"
    }

    override suspend fun load(viewModel: BaseViewModel, query1: String, query2: Int, loadType: Int, boundType: Int,
                              calledFrom: Int): LiveData<Resource> {
        return object: NetworkBoundResource<MutableList<Photo>, PagedList<Photo>, Photog>(loadType, boundType) {
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
                        .setPrefetchDistance(10)
                        .setEnablePlaceholders(true)
                        .build()

                return withContext(Dispatchers.IO) {
                    LivePagedListBuilder(dao.getPhotos(query1), /* PageList Config */ config)
                            .setBoundaryCallback(PageListFavoritePhotoBoundaryCallback<Photo>(
                                    viewModel as FavoritePhotoViewModel, query1, pages, boundType)).build()
                }
            }

            override suspend fun loadFromNetwork() = searpService.getFavoritePhotos(KEY, query1, METHOD, FORMAT_JSON, query2, PER_PAGE, INDEX)

            override fun onNetworkError(errorMessage: String?, errorCode: Int) {}

            override fun onFetchFailed(failedMessage: String?) = repoRateLimit.reset(query1)

            override suspend fun clearCache() = dao.clearAll()
        }.getAsLiveData()
    }

    internal fun deleteByPhotoId(id: String) = dao.deleteByPhotoId(id)

    internal suspend fun update(photo: Photo) = dao.update(photo)

    internal fun removePhotos() = dao.clearAll()
}