package com.goforer.grabph.data.repository.remote.feed.photo

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.goforer.grabph.data.datasource.model.cache.data.entity.Query
import com.goforer.grabph.data.datasource.model.cache.data.entity.photosizes.PhotoSize
import com.goforer.grabph.data.datasource.model.cache.data.entity.photosizes.PhotoSizeg
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource
import com.goforer.grabph.data.datasource.network.response.ApiResponse
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.data.repository.remote.Repository
import com.goforer.grabph.domain.Parameters
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoSizeRepository
@Inject
constructor(): Repository<Query>() {
    companion object {
        const val METHOD = "flickr.photos.getsizes"
    }

    /* Not using this method */
    override suspend fun load(liveData: MutableLiveData<Query>, parameters: Parameters): LiveData<Resource> {
        return object : NetworkBoundResource<MutableList<PhotoSize>, PagedList<PhotoSize>, PhotoSizeg>(parameters.loadType, parameters.boundType) {
            override fun onNetworkError(errorMessage: String?, errorCode: Int) {}

            override fun onFetchFailed(failedMessage: String?) {}

            override suspend fun saveToCache(item: MutableList<PhotoSize>) {}

            override suspend fun loadFromCache(isLatest: Boolean, itemCount: Int, pages: Int): LiveData<PagedList<PhotoSize>> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override suspend fun loadFromNetwork(): LiveData<ApiResponse<PhotoSizeg>> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override suspend fun clearCache() { }
        }.getAsLiveData()
    }

    @WorkerThread
    internal suspend fun loadPhotoSizes(photoId: String): Response<PhotoSizeg> {
        return searpService.getPhotoSizes(KEY, photoId, METHOD, FORMAT_JSON, 1)
    }
}