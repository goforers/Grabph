package com.goforer.grabph.repository.interactor.remote.profile

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.presentation.vm.profile.OthersPhotosViewModel
import com.goforer.grabph.repository.interactor.remote.Repository
import com.goforer.grabph.repository.interactor.remote.paging.boundarycallback.PageListProfilePhotoBoundaryCallback
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
constructor(private val dao: OthersPhotosDao, private val photoDao: PhotoDao): Repository() {

    companion object {
        const val METHOD = "flickr.people.getphotos"
        const val PREFETCH_DISTANCE = 10
    }

    override suspend fun load(viewModel: BaseViewModel, query1: String, query2: Int, loadType: Int,
                              boundType: Int, calledFrom: Int): LiveData<Resource> {

        return object: NetworkBoundResource<MutableList<Photo>, PagedList<Photo>, Photog>(loadType, boundType) {

            override fun onNetworkError(errorMessage: String?, errorCode: Int) {}

            override fun onFetchFailed(failedMessage: String?) = repoRateLimit.reset(query1)

            override suspend fun saveToCache(item: MutableList<Photo>) = photoDao.insert(item)

            override suspend fun loadFromCache(isLatest: Boolean, itemCount: Int, pages: Int): LiveData<PagedList<Photo>> {
                val config = PagedList.Config.Builder()
                    .setInitialLoadSizeHint(20)
                    .setPageSize(itemCount)
                    .setPrefetchDistance(PREFETCH_DISTANCE)
                    .setEnablePlaceholders(true)
                    .build()

                return withContext(Dispatchers.IO) {
                    LivePagedListBuilder(photoDao.getPhotos(query1), config)
                        .setBoundaryCallback(PageListProfilePhotoBoundaryCallback<Photo>(
                            viewModel as OthersPhotosViewModel, query1, pages, calledFrom
                        )).build()
                }
            }

            override suspend fun loadFromNetwork() = searpService.getPhotos(KEY, query1, METHOD, FORMAT_JSON, query2, PER_PAGE, INDEX)

            override suspend fun clearCache() = dao.clearAll()
        }.getAsLiveData()
    }

    internal fun removeCache() = photoDao.clearAll()
}