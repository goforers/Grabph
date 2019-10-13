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

package com.goforer.grabph.presentation.vm.home

import androidx.lifecycle.*
import com.goforer.base.annotation.MockData
import com.goforer.grabph.domain.usecase.Parameters
import com.goforer.grabph.domain.usecase.home.LoadHomeUseCase
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.repository.model.cache.data.entity.home.Home
import com.goforer.grabph.repository.network.response.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeViewModel
@Inject
constructor(private val useCase: LoadHomeUseCase) : BaseViewModel<Parameters>() {
    internal lateinit var home: LiveData<Resource>

    internal var calledFrom: Int = 0

    override fun setParameters(parameters: Parameters, type: Int) {
        home = useCase.execute(viewModelScope, parameters)
    }

    @MockData
    internal fun loadHome(): LiveData<Home>? = liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) { useCase.loadHome()?.let {
        emitSource(it)
    } }

    @MockData
    internal fun setHome(Home: Home) {
        viewModelScope.launch {
            useCase.setHome(Home)
        }
    }

    internal fun updateHome(Home: Home) {
        viewModelScope.launch {
            useCase.updateHome(Home)
        }
    }

    internal fun deleteHome() {
        viewModelScope.launch {
            useCase.deleteHome()
        }
    }
}