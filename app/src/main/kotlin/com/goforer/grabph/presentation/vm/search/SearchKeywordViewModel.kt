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

import androidx.lifecycle.*
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.domain.usecase.search.LoadSearchKeywordUseCase
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.search.RecentKeyword
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchKeywordViewModel
@Inject
constructor(private val useCase: LoadSearchKeywordUseCase): BaseViewModel<Parameters>() {
    internal lateinit var searchKeywords: LiveData<List<RecentKeyword>>

    override fun setParameters(parameters: Parameters, type: Int) {
        searchKeywords = useCase.execute(parameters)
    }

    internal fun setSearchKeyword(searchKeyword: String, recentKeyword: RecentKeyword): LiveData<RecentKeyword> {
        var keyword = MediatorLiveData<RecentKeyword>()

        viewModelScope.launch {
            keyword = useCase.saveSearchKeyword(searchKeyword, recentKeyword)
        }

        return keyword
    }

    internal fun deleteSearchKeyword(keyword: String) {
        viewModelScope.launch {
            useCase.deleteSearchKeyword(keyword)
        }
    }
}