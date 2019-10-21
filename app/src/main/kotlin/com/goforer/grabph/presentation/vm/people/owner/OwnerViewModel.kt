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

package com.goforer.grabph.presentation.vm.people.owner

import androidx.lifecycle.*
import com.goforer.base.annotation.MockData
import com.goforer.grabph.domain.Parameters
import com.goforer.grabph.domain.usecase.people.owner.LoadOwnerUseCase
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.data.datasource.model.cache.data.entity.profile.Owner
import com.goforer.grabph.data.datasource.network.response.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OwnerViewModel
@Inject
constructor(private val useCase: LoadOwnerUseCase) : BaseViewModel<Parameters>() {
    internal lateinit var owner: LiveData<Resource>

    override fun setParameters(parameters: Parameters, type: Int) {
        owner = useCase.execute(viewModelScope, parameters)
    }

    internal fun loadOwner(): LiveData<Owner> = liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) { emitSource(useCase.loadOwner()) }

    @MockData
    internal fun setOwner(owner: Owner) {
        viewModelScope.launch {
            useCase.setOwner(owner)
        }
    }

    internal fun deleteOwner() {
        viewModelScope.launch {
            useCase.deleteOwner()
        }
    }
}