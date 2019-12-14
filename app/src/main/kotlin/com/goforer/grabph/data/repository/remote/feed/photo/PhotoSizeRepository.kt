package com.goforer.grabph.data.repository.remote.feed.photo

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.goforer.grabph.data.datasource.model.cache.data.entity.Query
import com.goforer.grabph.data.datasource.model.cache.data.entity.photosizes.PhotoSizeg
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
        return MutableLiveData<Resource>()
    }

    @WorkerThread
    internal suspend fun loadPhotoSizes(photoId: String): Response<PhotoSizeg> {
        return searpService.getPhotoSizes(KEY, photoId, METHOD, FORMAT_JSON, 1)
    }
}