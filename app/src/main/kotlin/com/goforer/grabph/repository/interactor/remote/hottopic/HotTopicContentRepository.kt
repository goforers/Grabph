package com.goforer.grabph.repository.interactor.remote.hottopic

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.liveData
import com.goforer.base.annotation.MockData
import com.goforer.grabph.domain.usecase.Parameters
import com.goforer.grabph.repository.interactor.remote.Repository
import com.goforer.grabph.repository.model.cache.data.entity.Query
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
constructor(private val dao: HotTopicContentDao): Repository<Query>() {
    companion object {
        const val METHOD = "searp.home.getMain"
    }

    override suspend fun load(liveData: MutableLiveData<Query>, parameters: Parameters): LiveData<Resource> {
        return object: NetworkBoundResource<HotTopicContent, HotTopicContent, HotTopicContentg>(parameters.loadType, parameters.boundType) {
            override suspend fun saveToCache(item: HotTopicContent) = dao.insert(item)

            // This function had been blocked at this time but it might be used in the future
            /*
            override fun shouldFetch(): Boolean {
                return true
            }
            */

            override suspend fun loadFromCache(isLatest: Boolean, itemCount: Int, pages: Int) = dao.getHotTopicContent()

            override suspend fun loadFromNetwork() = searpService.getHotTopicContent(KEY, parameters.query1 as String, METHOD, FORMAT_JSON, INDEX)

            override fun onNetworkError(errorMessage: String?, errorCode: Int) {}

            override fun onFetchFailed(failedMessage: String?) = repoRateLimit.reset(parameters.query1 as String)

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
    internal suspend fun deleteHotTopicContent() =  delete()

    @MockData
    internal suspend fun insert(hotTopicContent: HotTopicContent) =  dao.insert(hotTopicContent)

    internal suspend fun delete() = dao.clearAll()
}