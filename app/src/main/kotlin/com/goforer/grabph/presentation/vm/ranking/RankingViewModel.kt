/*
 * Copyright 2019 Lukoh Nam, goForer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.goforer.grabph.presentation.vm.ranking

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import com.goforer.base.annotation.MockData
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.repository.model.cache.data.AbsentLiveData
import com.goforer.grabph.repository.model.cache.data.entity.ranking.Ranking
import com.goforer.grabph.repository.network.response.Resource
import com.goforer.grabph.repository.interactor.remote.ranking.RankingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RankingViewModel
@Inject
constructor(val interactor: RankingRepository): BaseViewModel() {
    @VisibleForTesting
    private val liveData by lazy { MutableLiveData<String>() }

    private val rankingLiveData = MutableLiveData<Ranking>()

    internal val ranking: LiveData<Resource>

    internal var calledFrom: Int = 0

    init {
        ranking = Transformations.switchMap(liveData) { query ->
            query ?: AbsentLiveData.create<Resource>()
            liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                emitSource(interactor.load(this@RankingViewModel, query!!, -1, loadType, boundType, calledFrom))
            }
        }
    }

    internal fun setRankingLiveData(data: Ranking) { rankingLiveData.value = data }

    internal fun getRankingLiveData(): MutableLiveData<Ranking> { return rankingLiveData }

    internal fun setId(id: String) {
        liveData.value = id
        val input = id.toLowerCase(Locale.getDefault()).trim { it <= ' ' }

        if (input == liveData.value) {
            return
        }

        liveData.value = input
    }

    @MockData
    internal fun loadRanking(): LiveData<Ranking>? = liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) { emitSource(interactor.loadRanking()) }

    @MockData
    internal fun setRanking(ranking: Ranking) {
        viewModelScope.launch { interactor.setRanking(ranking) }
    }

    internal fun loadRankingFromCache() {
        viewModelScope.launch { setRankingLiveData(interactor.loadRankingCache()) }
    }
}