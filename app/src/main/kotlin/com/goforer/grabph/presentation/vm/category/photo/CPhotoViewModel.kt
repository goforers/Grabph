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

package com.goforer.grabph.presentation.vm.category.photo

import androidx.lifecycle.*
import com.goforer.grabph.domain.usecase.Parameters
import com.goforer.grabph.domain.usecase.category.photo.LoadCPhotoUseCase
import com.goforer.grabph.presentation.vm.BaseViewModel
import com.goforer.grabph.repository.model.cache.data.entity.category.CPhoto
import com.goforer.grabph.repository.network.response.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CPhotoViewModel
@Inject
constructor(private val useCase: LoadCPhotoUseCase): BaseViewModel<Parameters>() {
    internal lateinit var categoryPhoto: LiveData<Resource>

    internal var calledFrom: Int = 0

    override fun setParameters(parameters: Parameters, type: Int) {
        categoryPhoto = useCase.execute(viewModelScope, parameters)
    }

    internal fun updatePhoto(photo: CPhoto) = viewModelScope.launch { useCase.updatePhoto(photo) }

    internal fun removePhotos() = viewModelScope.launch { useCase.removePhotos() }
}