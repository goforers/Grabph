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

package com.goforer.grabph.presentation.vm.search

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.repository.model.cache.data.AbsentLiveData
import com.goforer.grabph.repository.model.cache.data.entity.search.RecentKeyword
import com.goforer.grabph.repository.interactor.local.SearchKeywordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchKeywordViewModel
@Inject
constructor(private val repository: SearchKeywordRepository): BaseViewModel() {
    @VisibleForTesting
    private val liveData by lazy {
        MutableLiveData<String>()
    }

    internal val searchKeywords: LiveData<List<RecentKeyword>?>

    init {
        searchKeywords = Transformations.switchMap(liveData) { keyword ->
            keyword ?: AbsentLiveData.create<List<RecentKeyword>>()
            liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                emitSource( repository.loadSearchKeywords())
            }
        }
    }

    internal fun setKeyword(keyword: String) {
        liveData.value = keyword

        val input = keyword.trim { it <= ' ' }

        if (input == liveData.value) {
            return
        }

        liveData.value = input
    }

    internal fun setSearchKeyword(searchKeyword: String, recentKeyword: RecentKeyword): LiveData<RecentKeyword> {
        var keyword = MediatorLiveData<RecentKeyword>()

        viewModelScope.launch {
            keyword = repository.saveSearchKeyword(searchKeyword, recentKeyword)
        }

        return keyword
    }

    internal fun deleteSearchKeyword(keyword: String) {
        viewModelScope.launch {
            repository.deleteSearchKeyword(keyword)

        }
    }
}