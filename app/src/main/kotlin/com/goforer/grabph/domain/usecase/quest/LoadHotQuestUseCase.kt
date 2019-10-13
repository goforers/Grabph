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

package com.goforer.grabph.domain.usecase.quest

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.liveData
import com.goforer.base.annotation.MockData
import com.goforer.grabph.domain.usecase.BaseUseCase
import com.goforer.grabph.domain.usecase.Parameters
import com.goforer.grabph.repository.interactor.remote.quest.TopPortionQuestRepository
import com.goforer.grabph.repository.model.cache.data.AbsentLiveData
import com.goforer.grabph.repository.model.cache.data.entity.Query
import com.goforer.grabph.repository.model.cache.data.entity.quest.TopPortionQuest
import com.goforer.grabph.repository.network.response.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadHotQuestUseCase
@Inject
constructor(private val repository: TopPortionQuestRepository): BaseUseCase<Parameters, Resource>() {
    @VisibleForTesting
    private val liveData by lazy { MutableLiveData<Query>() }

    override fun execute(viewModelScope: CoroutineScope, parameters: Parameters): LiveData<Resource> {
        setQuery(parameters, Query())

        return Transformations.switchMap(liveData) { query ->
            query ?: AbsentLiveData.create<Resource>()
            liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                emitSource(repository.load(liveData, Parameters(query.query, liveData.value?.pages!!, liveData.value?.loadType!!, liveData.value?.boundType!!)))
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
    internal fun loadHotQuest(): LiveData<TopPortionQuest>? = repository.loadTopPortionQuest()

    @MockData
    internal suspend fun setTopPortionQuest(topPortionQuest: TopPortionQuest) = repository.setTopPortionQuest(topPortionQuest)
}