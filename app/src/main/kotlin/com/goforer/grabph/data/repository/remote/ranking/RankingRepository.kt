package com.goforer.grabph.data.repository.remote.ranking

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.liveData
import com.goforer.base.annotation.MockData
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.data.repository.remote.Repository
import com.goforer.grabph.data.datasource.model.cache.data.entity.Query
import com.goforer.grabph.data.datasource.model.cache.data.entity.ranking.Ranking
import com.goforer.grabph.data.datasource.model.dao.remote.ranking.RankingDao
import com.goforer.grabph.data.datasource.network.resource.NetworkBoundResource
import com.goforer.grabph.data.datasource.network.response.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RankingRepository
@Inject
constructor(private val rankingDao: RankingDao): Repository<Query>() {

    companion object {
        const val METHOD = "searp.ranking.getRanking"
    }

    override suspend fun load(liveData: MutableLiveData<Query>, parameters: Parameters): LiveData<Resource> {
        return object : NetworkBoundResource<Ranking, Ranking, Ranking>(parameters.loadType, parameters.boundType) {
            override fun onNetworkError(errorMessage: String?, errorCode: Int) { }

            override fun onFetchFailed(failedMessage: String?) { repoRateLimit.reset(parameters.query1 as String) }

            override suspend fun handleToCache(item: Ranking) = rankingDao.insert(item)

            override suspend fun loadFromCache(isLatest: Boolean, itemCount: Int, pages: Int) = rankingDao.getRanking()

            override suspend fun loadFromNetwork() = searpService.getRanking(KEY, METHOD, FORMAT_JSON, INDEX)

            override suspend fun clearCache() = rankingDao.clearAll()
        }.getAsLiveData()
    }

    @MockData
    internal fun loadRanking(): LiveData<Ranking> = liveData {
        emitSource(Transformations.map(rankingDao.getRanking()) { it })
    }

    @WorkerThread
    internal suspend fun loadRankingCache() = rankingDao.loadRanking()

    @WorkerThread
    @MockData
    internal suspend fun setRanking(ranking: Ranking) = insert(ranking)

    @MockData
    private suspend fun insert(ranking: Ranking) = rankingDao.insert(ranking)

    internal suspend fun removeCache() = rankingDao.clearAll()
}