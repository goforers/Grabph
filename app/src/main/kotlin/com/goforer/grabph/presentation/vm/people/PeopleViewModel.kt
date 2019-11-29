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

package com.goforer.grabph.presentation.vm.people

import androidx.lifecycle.*
import com.goforer.base.annotation.MockData
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.domain.usecase.people.LoadPeopleUseCase
import com.goforer.grabph.data.datasource.network.response.Resource
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.Searper
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeopleViewModel
@Inject
constructor(private val useCase: LoadPeopleUseCase): BaseViewModel<Parameters>() {
    internal lateinit var people: LiveData<Resource>

    private val peopleLiveData = MutableLiveData<List<Searper>>()

    internal var calledFrom: Int = 0

    override fun setParameters(parameters: Parameters, type: Int) {
        people = useCase.execute(parameters)
    }

    @MockData
    internal fun deleteUser(id: String) {
        viewModelScope.launch {
            useCase.deleteUser(id)
        }
    }

    @MockData
    internal fun setPeople(people: List<Searper>) {
        viewModelScope.launch {
            useCase.setPeople(people as MutableList)
        }
    }

    internal fun setPeopleLiveData(data: List<Searper>) { peopleLiveData.value = data }

    internal fun getPeopleLiveData(): LiveData<List<Searper>> { return peopleLiveData }

    internal fun loadPeopleFromCache() {
        viewModelScope.launch {
            setPeopleLiveData(useCase.loadPeopleFromCache())
        }
    }
}