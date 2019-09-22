package com.goforer.grabph.repository.interactor.remote.ranking

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.liveData
import com.goforer.base.annotation.MockData
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.repository.interactor.remote.Repository
import com.goforer.grabph.repository.model.cache.data.entity.ranking.Ranking
import com.goforer.grabph.repository.model.dao.remote.ranking.RankingDao
import com.goforer.grabph.repository.network.resource.NetworkBoundResource
import com.goforer.grabph.repository.network.response.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RankingRepository
@Inject
constructor(private val rankingDao: RankingDao): Repository() {

    companion object {
        const val METHOD = "searp.ranking.getRanking"
    }

    override suspend fun load(viewModel: BaseViewModel, query1: String, query2: Int, loadType: Int,
                              boundType: Int, calledFrom: Int): LiveData<Resource> {
        return object : NetworkBoundResource<Ranking, Ranking, Ranking>(loadType, boundType) {
            override fun onNetworkError(errorMessage: String?, errorCode: Int) { }

            override fun onFetchFailed(failedMessage: String?) { repoRateLimit.reset(query1) }

            override suspend fun saveToCache(item: Ranking) = rankingDao.insert(item)

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

    internal fun removeCache() = rankingDao.clearAll()
}