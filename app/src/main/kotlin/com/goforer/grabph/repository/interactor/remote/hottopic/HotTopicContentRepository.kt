package com.goforer.grabph.repository.interactor.remote.hottopic

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.liveData
import com.goforer.base.annotation.MockData
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.repository.interactor.remote.Repository
import com.goforer.grabph.repository.model.cache.data.entity.hottopic.HotTopicContent
import com.goforer.grabph.repository.model.cache.data.entity.hottopic.HotTopicContentg
import com.goforer.grabph.repository.model.dao.remote.hottopic.HotTopicContentDao
import com.goforer.grabph.repository.network.resource.NetworkBoundResource
import com.goforer.grabph.repository.network.response.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HotTopicContentRepository
@Inject
constructor(private val dao: HotTopicContentDao): Repository() {
    companion object {
        const val METHOD = "searp.home.getMain"
    }

    override suspend fun load(viewModel: BaseViewModel, query1: String, query2: Int, loadType: Int,
                              boundType: Int, calledFrom: Int): LiveData<Resource> {
        return object: NetworkBoundResource<HotTopicContent, HotTopicContent, HotTopicContentg>(loadType, boundType) {
            override suspend fun saveToCache(item: HotTopicContent) = dao.insert(item)

            // This function had been blocked at this time but it might be used in the future
            /*
            override fun shouldFetch(): Boolean {
                return true
            }
            */

            override suspend fun loadFromCache(isLatest: Boolean, itemCount: Int, pages: Int) = dao.getHotTopicContent()

            override suspend fun loadFromNetwork() = searpService.getHotTopicContent(KEY, query1, METHOD, FORMAT_JSON, INDEX)

            override fun onNetworkError(errorMessage: String?, errorCode: Int) {}

            override fun onFetchFailed(failedMessage: String?) = repoRateLimit.reset(query1)

            override suspend fun clearCache() = dao.clearAll()
        }.getAsLiveData()
    }

    @MockData
    internal fun loadHotTopicContent(): LiveData<HotTopicContent> = liveData {
        emitSource(Transformations.map(dao.getHotTopicContent()) { it })
    }

    @WorkerThread
    @MockData
    internal suspend fun setHotTopicContent(hotTopicContent: HotTopicContent) = insert(hotTopicContent)

    @WorkerThread
    internal fun deleteHotTopicContent() =  delete()

    @MockData
    internal suspend fun insert(hotTopicContent: HotTopicContent) =  dao.insert(hotTopicContent)

    internal fun delete() = dao.clearAll()
}