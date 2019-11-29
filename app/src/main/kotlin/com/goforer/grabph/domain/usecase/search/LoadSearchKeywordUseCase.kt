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

package com.goforer.grabph.domain.usecase.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.liveData
import com.goforer.grabph.domain.usecase.BaseUseCase
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.data.repository.local.SearchKeywordRepository
import com.goforer.grabph.data.datasource.model.cache.data.AbsentLiveData
import com.goforer.grabph.data.datasource.model.cache.data.entity.Query
import com.goforer.grabph.data.datasource.model.cache.data.entity.search.RecentKeyword
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadSearchKeywordUseCase
@Inject
constructor(private val repository: SearchKeywordRepository): BaseUseCase<Parameters, List<RecentKeyword>>() {
    private val liveData by lazy { MutableLiveData<Query>() }

    override fun execute(parameters: Parameters): LiveData<List<RecentKeyword>> {
        setQuery(parameters, Query())

        return Transformations.switchMap(liveData) { keyword ->
            keyword ?: AbsentLiveData.create<List<RecentKeyword>>()
            liveData {
                emitSource( repository.loadSearchKeywords())
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

    internal suspend fun saveSearchKeyword(searchKeyword: String, recentKeyword: RecentKeyword) = repository.saveSearchKeyword(searchKeyword, recentKeyword)

    internal suspend fun deleteSearchKeyword(keyword: String) = repository.deleteSearchKeyword(keyword)
}