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

package com.goforer.grabph.presentation.vm.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.goforer.grabph.domain.usecase.Parameters
import com.goforer.grabph.domain.usecase.profile.LoadOthersProfileUseCase
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.repository.network.response.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OthersProfileViewModel
@Inject
constructor(private val useCase: LoadOthersProfileUseCase): BaseViewModel<Parameters>() {
    internal lateinit var person: LiveData<Resource>

    override fun setParameters(parameters: Parameters, type: Int) {
        person = useCase.execute(viewModelScope, parameters)
    }

    internal suspend fun removePerson() = useCase.removePerson()
}