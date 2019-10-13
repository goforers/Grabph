package com.goforer.grabph.repository.interactor.remote.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.goforer.grabph.domain.usecase.Parameters
import com.goforer.grabph.repository.interactor.remote.Repository
import com.goforer.grabph.repository.interactor.remote.paging.boundarycallback.PageListProfilePhotoBoundaryCallback
import com.goforer.grabph.repository.model.cache.data.entity.Query
import com.goforer.grabph.repository.model.cache.data.entity.photog.Photo
import com.goforer.grabph.repository.model.cache.data.entity.photog.Photog
import com.goforer.grabph.repository.model.dao.remote.feed.photo.PhotoDao
import com.goforer.grabph.repository.model.dao.remote.profile.OthersPhotosDao
import com.goforer.grabph.repository.network.resource.NetworkBoundResource
import com.goforer.grabph.repository.network.response.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OthersPhotosRepository
@Inject
constructor(private val dao: OthersPhotosDao, private val photoDao: PhotoDao): Repository<Query>() {
    companion object {
        const val METHOD = "flickr.people.getphotos"
        const val PREFETCH_DISTANCE = 10
    }

    override suspend fun load(liveData: MutableLiveData<Query>, parameters: Parameters): LiveData<Resource> {
        return object: NetworkBoundResource<MutableList<Photo>, PagedList<Photo>, Photog>(parameters.loadType, parameters.boundType) {
            override fun onNetworkError(errorMessage: String?, errorCode: Int) {}
            override fun onFetchFailed(failedMessage: String?) = repoRateLimit.reset(parameters.query1 as String)
            override suspend fun saveToCache(item: MutableList<Photo>) = photoDao.insert(item)
            override suspend fun loadFromCache(isLatest: Boolean, itemCount: Int, pages: Int): LiveData<PagedList<Photo>> {
                val config = PagedList.Config.Builder()
                    .setInitialLoadSizeHint(20)
                    .setPageSize(itemCount)
                    .setPrefetchDistance(PREFETCH_DISTANCE)
                    .setEnablePlaceholders(true)
                    .build()

                return withContext(Dispatchers.IO) {
                    LivePagedListBuilder(photoDao.getPhotos(parameters.query1 as String), config)
                        .setBoundaryCallback(PageListProfilePhotoBoundaryCallback<Photo>(
                            liveData, parameters.query1, pages)).build()
                }
            }

            override suspend fun loadFromNetwork() = searpService.getPhotos(KEY, parameters.query1 as String, METHOD, FORMAT_JSON, parameters.query2 as Int, PER_PAGE, INDEX)

            override suspend fun clearCache() = dao.clearAll()
        }.getAsLiveData()
    }

    internal suspend fun removeCache() = photoDao.clearAll()
}