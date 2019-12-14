package com.goforer.grabph.data.repository.remote.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.goforer.grabph.data.datasource.model.cache.data.entity.Query
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.MyGallery
import com.goforer.grabph.data.datasource.model.cache.data.entity.photog.MyGalleryg
import com.goforer.grabph.data.datasource.model.dao.remote.feed.photo.MyGalleryDao
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.data.repository.paging.datasource.MyGalleryDataFactory
import com.goforer.grabph.data.repository.remote.Repository
import com.goforer.grabph.data.repository.remote.paging.boundarycallback.PagedListMyGalleryBoundaryCallback
import com.goforer.grabph.domain.Parameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyGalleryRepository
@Inject
constructor(private val dao: MyGalleryDao): Repository<Query>() {
    private val executor: Executor = Executors.newFixedThreadPool(5)

    companion object {
        const val METHOD = "flickr.people.getphotos"
        const val PREFETCH_DISTANCE = 10
        const val EXTRA_QUERIES = "media, url_m, url_z"
    }

    override suspend fun load(liveData: MutableLiveData<Query>, parameters: Parameters): LiveData<Resource> {
        return object: NetworkBoundResource<MutableList<MyGallery>, PagedList<MyGallery>, MyGalleryg>(parameters.loadType, parameters.boundType) {
            override fun onNetworkError(errorMessage: String?, errorCode: Int) {}
            override fun onFetchFailed(failedMessage: String?) = repoRateLimit.reset(parameters.query1 as String)
            override suspend fun saveToCache(item: MutableList<MyGallery>) = dao.insert(item)
            override suspend fun loadFromCache(isLatest: Boolean, itemCount: Int, pages: Int): LiveData<PagedList<MyGallery>> {
                val config = PagedList.Config.Builder()
                    .setInitialLoadSizeHint(20)
                    .setPageSize(itemCount)
                    .setPrefetchDistance(PREFETCH_DISTANCE)
                    .setEnablePlaceholders(true)
                    .build()

                return withContext(Dispatchers.IO) {
                    LivePagedListBuilder(dao.getPhotos(parameters.query1 as String), config)
                        .setBoundaryCallback(PagedListMyGalleryBoundaryCallback<MyGallery>(
                                liveData, parameters.query1, pages)).build()
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

    internal fun loadGallery(parameters: Parameters): LiveData<PagedList<MyGallery>> {
        val dataSourceFactory = MyGalleryDataFactory(searpService, parameters, KEY, METHOD, FORMAT_JSON, PER_PAGE)

        val config = PagedList.Config.Builder()
            .setPageSize(20)
            .setInitialLoadSizeHint(20)
            .setPrefetchDistance(PREFETCH_DISTANCE)
            .setEnablePlaceholders(true)
            .build()

        val data: LiveData<PagedList<MyGallery>> = LivePagedListBuilder(dataSourceFactory, config)
            .setFetchExecutor(executor)
            .build()

        return data
    }

    internal fun deleteByPhotoId(id: String) = dao.deleteByPhotoId(id)

    internal suspend fun update(gallery: MyGallery) = dao.update(gallery)

    internal suspend fun removeCache() = dao.clearAll()

}