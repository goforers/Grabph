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

package com.goforer.grabph.domain.usecase.people

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.liveData
import com.goforer.base.annotation.MockData
import com.goforer.grabph.domain.usecase.BaseUseCase
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.data.repository.remote.people.PeopleRepository
import com.goforer.grabph.data.datasource.model.cache.data.AbsentLiveData
import com.goforer.grabph.data.datasource.model.cache.data.entity.Query
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.Searper
import com.goforer.grabph.data.datasource.network.response.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadPeopleUseCase
@Inject
constructor(private val repository: PeopleRepository):  BaseUseCase<Parameters, Resource>() {
    private val liveData by lazy { MutableLiveData<Query>() }

    override fun execute(viewModelScope: CoroutineScope, parameters: Parameters): LiveData<Resource> {
        setQuery(parameters, Query())

        return Transformations.switchMap(liveData) { query ->
            query ?: AbsentLiveData.create<Resource>()
            liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                emitSource(repository.load(liveData,
                    Parameters(
                        query.query,
                        liveData.value?.pages!!,
                        liveData.value?.loadType!!,
                        liveData.value?.boundType!!
                    )
                ))
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
    internal fun deleteUser(id: String) = repository.deleteUser(id)

    @MockData
    internal suspend fun setPeople(people: List<Searper>) = repository.setPeople(people as MutableList)

    internal suspend fun loadPeopleFromCache() = repository.loadPeopleCache()
}