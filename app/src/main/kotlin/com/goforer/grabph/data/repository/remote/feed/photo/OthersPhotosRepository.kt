package com.goforer.grabph.data.repository.remote.feed.photo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.data.repository.remote.Repository
import com.goforer.grabph.data.repository.remote.paging.boundarycallback.PageListProfilePhotoBoundaryCallback
import com.goforer.grabph.data.datasource.model.cache.data.entity.Query
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.Photo
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.Photog
import com.goforer.grabph.data.datasource.model.dao.remote.feed.photo.PhotoDao
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.data.repository.paging.datasource.OthersProfileDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OthersPhotosRepository
@Inject
constructor(private val photoDao: PhotoDao): Repository<Query>() {
    companion object {
        const val METHOD = "flickr.people.getphotos"
        const val EXTRA_QUERIES = "media, url_m, url_z"
    }

    override suspend fun load(liveData: MutableLiveData<Query>, parameters: Parameters): LiveData<Resource> {
        return object: NetworkBoundResource<MutableList<Photo>, PagedList<Photo>, Photog>(parameters.loadType, parameters.boundType) {
            override fun onNetworkError(errorMessage: String?, errorCode: Int) {}

            override fun onFetchFailed(failedMessage: String?) = repoRateLimit.reset(parameters.query1 as String)

            override suspend fun handleToCache(item: MutableList<Photo>) = photoDao.insert(item)

            override suspend fun loadFromCache(isLatest: Boolean, itemCount: Int, pages: Int): LiveData<PagedList<Photo>> {
                val config = PagedList.Config.Builder()
                    .setInitialLoadSizeHint(itemCount * 2)
                    .setPageSize(itemCount)
                    .setPrefetchDistance(itemCount - 2)
                    .setEnablePlaceholders(true)
                    .build()

                return withContext(Dispatchers.IO) {
                    LivePagedListBuilder(object : DataSource.Factory<Int, Photo>() {
                        override fun create(): DataSource<Int, Photo> {
                            return OthersProfileDataSource(
                                photoDao.getPhotoList(parameters.query1 as String)
                            )
                        }
                    }, config)
                        .setBoundaryCallback(PageListProfilePhotoBoundaryCallback<Photo>(liveData, parameters.query1 as String, pages))
                        .build()
                }
            }

            override suspend fun loadFromNetwork() = searpService.getPhotos(
                KEY,
                parameters.query1 as String, // userId
                METHOD,
                FORMAT_JSON,
                parameters.query2 as Int, // page
                PER_PAGE,
                INDEX,
                EXTRA_QUERIES
            )

            override suspend fun clearCache() = photoDao.clearAll()
        }.getAsLiveData()
    }

    internal suspend fun removeCache() = photoDao.clearAll()
}