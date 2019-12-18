package com.goforer.grabph.data.repository.remote.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.goforer.grabph.data.datasource.model.cache.data.entity.Query
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.MyGallery
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.MyGalleryg
import com.goforer.grabph.data.datasource.model.dao.remote.feed.photo.MyGalleryDao
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.data.repository.paging.datasource.MyGalleryDataSource
import com.goforer.grabph.data.repository.remote.Repository
import com.goforer.grabph.data.repository.remote.paging.boundarycallback.PagedListMyGalleryBoundaryCallback
import com.goforer.grabph.domain.Parameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyGalleryRepository
@Inject
constructor(private val dao: MyGalleryDao): Repository<Query>() {
    companion object {
        const val METHOD = "flickr.people.getphotos"
        const val EXTRA_QUERIES = "media, url_m, url_z"
    }

    override suspend fun load(liveData: MutableLiveData<Query>, parameters: Parameters): LiveData<Resource> {
        return object: NetworkBoundResource<MutableList<MyGallery>, PagedList<MyGallery>, MyGalleryg>(parameters.loadType, parameters.boundType) {
            override fun onNetworkError(errorMessage: String?, errorCode: Int) {}

            override fun onFetchFailed(failedMessage: String?) = repoRateLimit.reset(parameters.query1 as String)

            override suspend fun handleToCache(item: MutableList<MyGallery>) = dao.insert(item)

            override suspend fun loadFromCache(isLatest: Boolean, itemCount: Int, pages: Int): LiveData<PagedList<MyGallery>> {
                val config = PagedList.Config.Builder()
                    .setInitialLoadSizeHint(itemCount * 2)
                    .setPageSize(itemCount)
                    .setPrefetchDistance(itemCount - 2)
                    .setEnablePlaceholders(true)
                    .build()

                return withContext(Dispatchers.IO) {
                    val userId = parameters.query1 as String
                    val gallery = dao.getPhotoList(userId)

                    LivePagedListBuilder(object : DataSource.Factory<Int, MyGallery>() {
                        override fun create(): DataSource<Int, MyGallery> {
                            return MyGalleryDataSource(gallery)
                        }
                    }, config)
                        .setBoundaryCallback(PagedListMyGalleryBoundaryCallback(liveData, userId, pages))
                        .build()
                }
            }
            
            override suspend fun loadFromNetwork() = searpService.getMyGallery(
                    KEY,
                    parameters.query1 as String,
                    METHOD,
                    FORMAT_JSON,
                    parameters.query2 as Int,
                    PER_PAGE,
                    INDEX,
                    EXTRA_QUERIES
                )

            override suspend fun clearCache() = dao.clearAll()
        }.getAsLiveData()
    }

    internal fun deleteByPhotoId(id: String) = dao.deleteByPhotoId(id)

    internal suspend fun update(gallery: MyGallery) = dao.update(gallery)

    internal suspend fun removeCache() = dao.clearAll()

}