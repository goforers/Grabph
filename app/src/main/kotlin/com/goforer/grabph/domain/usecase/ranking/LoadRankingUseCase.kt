package com.goforer.grabph.domain.usecase.ranking

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.liveData
import com.goforer.base.annotation.MockData
import com.goforer.grabph.domain.usecase.BaseUseCase
import com.goforer.grabph.domain.usecase.Parameters
import com.goforer.grabph.repository.interactor.remote.ranking.RankingRepository
import com.goforer.grabph.repository.model.cache.data.AbsentLiveData
import com.goforer.grabph.repository.model.cache.data.entity.Query
import com.goforer.grabph.repository.model.cache.data.entity.ranking.Ranking
import com.goforer.grabph.repository.network.response.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadRankingUseCase
@Inject
constructor(private val repository: RankingRepository): BaseUseCase<Parameters, Resource>() {
    @VisibleForTesting
    private val liveData by lazy { MutableLiveData<Query>() }

    private val rankingLiveData by lazy { MutableLiveData<Ranking>() }

    override fun execute(viewModelScope: CoroutineScope, parameters: Parameters): LiveData<Resource> {
        setQuery(parameters, Query())

        return Transformations.switchMap(liveData) { query ->
            query ?: AbsentLiveData.create<Resource>()
            liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                emitSource(repository.load(liveData, Parameters(query.query, -1, query.loadType, liveData.value?.boundType!!)))
            }
        }
    }

    private fun setQuery(parameters: Parameters, query: Query) {
        query.query = parameters.query1 as String
        query.pages = parameters.query2 as Int
        query.boundType = parameters.boundType
        query.loadType = parameters.loadType
        liveData.value = query

        val input = query.pages
        if (input == liveData.value?.pages) return

        liveData.value = query
    }

    @MockData
    internal fun loadRanking(): LiveData<Ranking>? = repository.loadRanking()

    @MockData
    internal suspend fun setRanking(ranking: Ranking) = repository.setRanking(ranking)

    internal fun getRankingLiveData(): MutableLiveData<Ranking> { return rankingLiveData }

    internal fun setRankingLiveData(data: Ranking) { rankingLiveData.value = data }

    internal suspend fun loadRankingFromCache() = setRankingLiveData(repository.loadRankingCache())
}